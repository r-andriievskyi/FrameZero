package com.frame.zero.repository.chat

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.frame.zero.core.network.ChatSocketClient
import com.frame.zero.core.network.ChatSocketEvent
import com.frame.zero.core.network.connectivity.ConnectivityObserver
import com.frame.zero.database.FrameZeroDatabase
import com.frame.zero.domain.chat.ChatMessage
import com.frame.zero.domain.chat.Conversation
import com.frame.zero.domain.chat.PendingChatMessage
import com.frame.zero.repository.chat.local.toDomain
import com.frame.zero.repository.chat.local.toEntity
import com.frame.zero.repository.chat.network.ChatApi
import com.frame.zero.repository.chat.outbox.ChatOutbox
import com.frame.zero.repository.chat.outbox.ChatOutboxScheduler
import com.frame.zero.repository.chat.outbox.ChatOutboxStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

import kotlin.time.Duration.Companion.seconds

private const val PageSize = 30
private val INITIAL_RETRY_DELAY = 2.seconds
private val MAX_RETRY_DELAY = 30.seconds

internal class ChatRepositoryImpl(
  private val api: ChatApi,
  private val database: FrameZeroDatabase,
  private val socketClient: ChatSocketClient,
  private val outbox: ChatOutbox,
  private val outboxStore: ChatOutboxStore,
  private val outboxScheduler: ChatOutboxScheduler,
  private val connectivityObserver: ConnectivityObserver,
  private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) : ChatRepository {
  private val dao get() = database.chatDao()

  private val mutex = Mutex()
  private val tracked = mutableSetOf<String>()

  init {
    scope.launch {
      socketClient.events.collect { event ->
        when (event) {
          // Clears the matching outbox row in the same transaction, so a message confirmed over the
          // socket before its own REST response retires its optimistic bubble exactly once.
          is ChatSocketEvent.MessageReceived ->
            dao.upsertMessagesAndClearPending(listOf(event.message.toEntity()))
          is ChatSocketEvent.ReadUpdated ->
            dao.advanceLastReadOrdinal(event.conversationId, event.lastReadOrdinal)
          ChatSocketEvent.Connected ->
            scope.launch {
              syncTracked()
              outbox.drainAll()
            }
        }
      }
    }
    scope.launch {
      // Regaining connectivity is the other flush trigger: the socket may be down for reasons of
      // its own, and a queued message shouldn't wait on it.
      connectivityObserver.isOnline.filter { it }.collect { outbox.drainAll() }
    }
  }

  @OptIn(ExperimentalPagingApi::class)
  override fun messages(conversationId: String): Flow<PagingData<ChatMessage>> =
    Pager(
      config = PagingConfig(pageSize = PageSize, enablePlaceholders = false),
      remoteMediator = ChatMessagesRemoteMediator(conversationId, api, dao),
      pagingSourceFactory = { dao.messagesPagingSource(conversationId) }
    ).flow.map { pagingData -> pagingData.map { entity -> entity.toDomain() } }

  override suspend fun getOrCreateConversation(taskId: String): Conversation {
    val conversation = api.getOrCreateConversation(taskId).toDomain()
    dao.upsertConversation(conversation.toEntity())
    return conversation
  }

  override suspend fun cachedConversation(taskId: String): Conversation? = dao.conversationByTaskId(taskId)?.toDomain()

  override fun observeConversation(taskId: String): Flow<Conversation?> =
    dao.observeConversationByTaskId(taskId).map { it?.toDomain() }

  override suspend fun subscribe(conversationId: String) {
    mutex.withLock { tracked.add(conversationId) }
    // Initial history comes from the pager's REFRESH; the socket's Connected event drives the
    // gap-recovery sync (which spans multiple pages), so we don't backfill again here.
    socketClient.subscribe(conversationId)
  }

  override suspend fun enqueue(
    conversationId: String,
    clientMessageId: String,
    body: String
  ) {
    outboxStore.enqueue(conversationId, clientMessageId, body)
    // Drain in the background so composing never blocks on the network, and ask the platform for a
    // durable retry in case this process dies before the drain finishes.
    scope.launch { drainWithRetry(conversationId) }
    outboxScheduler.schedule(conversationId)
  }

  override fun observePending(conversationId: String): Flow<List<PendingChatMessage>> =
    outboxStore.observe(conversationId)

  override suspend fun retryPending(
    conversationId: String,
    clientMessageId: String
  ) {
    outboxStore.retry(conversationId, clientMessageId)
    scope.launch { drainWithRetry(conversationId) }
    outboxScheduler.schedule(conversationId)
  }

  override suspend fun discardPending(
    conversationId: String,
    clientMessageId: String
  ) = outboxStore.remove(conversationId, clientMessageId)

  override suspend fun flushOutbox() = outbox.drainAll()

  /**
   * Drains a conversation and, on a server-side transient failure while still online, retries in
   * process with backoff. Without this an iOS send (no WorkManager backstop there) would sit on a
   * 5xx until the next app launch, since neither the connectivity nor the socket-reconnect trigger
   * fires while the network stays up. An offline stop returns here immediately — the connectivity
   * trigger owns that re-kick — and the drain itself parks a message once its retry budget is spent,
   * so this loop always terminates.
   */
  private suspend fun drainWithRetry(conversationId: String) {
    var backoff = INITIAL_RETRY_DELAY
    while (true) {
      if (outbox.drain(conversationId)) return
      if (!connectivityObserver.isCurrentlyOnline()) return
      delay(backoff)
      backoff = (backoff * 2).coerceAtMost(MAX_RETRY_DELAY)
    }
  }

  override suspend fun markRead(
    conversationId: String,
    lastReadOrdinal: Long
  ) {
    val applied = api.markRead(conversationId, lastReadOrdinal)
    dao.advanceLastReadOrdinal(conversationId, applied)
  }

  private suspend fun syncTracked() {
    val ids = mutex.withLock { tracked.toList() }
    ids.forEach { runCatching { syncLatest(it) } }
  }

  /**
   * Walks newest-first pages from the top until reaching the highest `ordinal` already in Room, so
   * every message newer than the local tail is filled with no gap. Upserts dedupe by id, so a
   * message that also arrives on the socket collapses to one row. Ordinals are contiguous per
   * conversation, so each page after the first is sized to the remaining gap — never
   * re-downloading a full page of already-cached history below the local tail.
   */
  private suspend fun syncLatest(conversationId: String) {
    val since = dao.maxOrdinal(conversationId)
    var before: Long? = null
    while (true) {
      val limit = if (since == null || before == null) {
        PageSize
      } else {
        (before - since).coerceIn(1L, PageSize.toLong()).toInt()
      }
      val page = api.listMessages(conversationId, before, limit)
      if (page.items.isEmpty()) break
      dao.upsertMessagesAndAdvanceLatest(page.items.map { it.toEntity() })
      val oldestInPage = page.items.last().ordinal
      // Fresh cache: the newest page is enough; older history loads via paging.
      if (since == null) break
      // Reached (or overlapped) the known tail — the gap is closed.
      if (oldestInPage <= since) break
      if (page.nextCursor == null) break
      before = oldestInPage
    }
  }
}
