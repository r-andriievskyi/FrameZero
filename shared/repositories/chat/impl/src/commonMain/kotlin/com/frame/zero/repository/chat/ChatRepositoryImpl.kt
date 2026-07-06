package com.frame.zero.repository.chat

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.frame.zero.core.network.ChatSocketClient
import com.frame.zero.core.network.ChatSocketEvent
import com.frame.zero.database.FrameZeroDatabase
import com.frame.zero.domain.chat.ChatMessage
import com.frame.zero.domain.chat.Conversation
import com.frame.zero.dto.chat.SendMessageRequest
import com.frame.zero.repository.chat.local.toDomain
import com.frame.zero.repository.chat.local.toEntity
import com.frame.zero.repository.chat.network.ChatApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private const val PageSize = 30

class ChatRepositoryImpl(
  private val api: ChatApi,
  private val database: FrameZeroDatabase,
  private val socketClient: ChatSocketClient,
  private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) : ChatRepository {
  private val dao get() = database.chatDao()

  private val mutex = Mutex()
  private val tracked = mutableSetOf<String>()

  init {
    scope.launch {
      socketClient.events.collect { event ->
        when (event) {
          is ChatSocketEvent.MessageReceived ->
            dao.upsertMessagesAndAdvanceLatest(listOf(event.message.toEntity()))
          is ChatSocketEvent.ReadUpdated ->
            dao.advanceLastReadOrdinal(event.conversationId, event.lastReadOrdinal)
          ChatSocketEvent.Connected -> scope.launch { syncTracked() }
        }
      }
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

  override suspend fun send(
    conversationId: String,
    clientMessageId: String,
    body: String
  ) {
    // The caller owns [clientMessageId] and reuses it across retries, so a resend after a lost
    // response is deduped by the server rather than posting a duplicate.
    val request = SendMessageRequest(clientMessageId = clientMessageId, body = body)
    val message = api.send(conversationId, request)
    dao.upsertMessagesAndAdvanceLatest(listOf(message.toEntity()))
    // The sender has by definition read their own message: advance the local read cursor so
    // it never shows as unread on the badge. The server cursor is advanced separately by the
    // screen's mark-read; READ then syncs the user's other devices.
    dao.advanceLastReadOrdinal(conversationId, message.ordinal)
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
