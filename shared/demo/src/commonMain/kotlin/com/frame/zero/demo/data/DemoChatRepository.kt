package com.frame.zero.demo.data

import androidx.paging.PagingData
import com.frame.zero.demo.DemoData
import com.frame.zero.demo.DemoDataStore
import com.frame.zero.domain.chat.ChatMessage
import com.frame.zero.domain.chat.Conversation
import com.frame.zero.repository.chat.ChatRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

/**
 * Local chat over [DemoDataStore]. Sending a message appends it immediately and, to make the demo
 * feel alive, schedules one canned crew reply shortly after — still entirely offline.
 */
internal class DemoChatRepository(
  private val store: DemoDataStore,
  private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) : ChatRepository {
  override suspend fun getOrCreateConversation(taskId: String): Conversation = store.getOrCreateConversation(taskId)

  override suspend fun cachedConversation(taskId: String): Conversation? = store.conversationFor(taskId)

  override fun observeConversation(taskId: String): Flow<Conversation?> = store.conversations.map { it[taskId] }

  override fun messages(conversationId: String): Flow<PagingData<ChatMessage>> =
    store.messages.map { byConversation ->
      PagingData.from(byConversation[conversationId].orEmpty())
    }

  override suspend fun subscribe(conversationId: String) = Unit

  override suspend fun send(
    conversationId: String,
    clientMessageId: String,
    body: String
  ) {
    store.appendMessage(conversationId, DemoData.USER_ID, body, clientMessageId)
    val replySender = store.replySenderFor(conversationId) ?: return
    scope.launch {
      delay(1_500.milliseconds)
      store.appendMessage(
        conversationId = conversationId,
        senderUserId = replySender,
        body = cannedReplies.random(Random),
        clientMessageId = "demo-reply-${Random.nextLong()}"
      )
    }
  }

  override suspend fun markRead(
    conversationId: String,
    lastReadOrdinal: Long
  ) = store.markRead(conversationId, lastReadOrdinal)

  /** Drops in-flight canned replies so a sign-out can't write into the freshly reset store. */
  fun cancelPendingReplies() = scope.coroutineContext.cancelChildren()

  private companion object {
    val cannedReplies = listOf(
      "Got it, thanks!",
      "On it — will update you shortly.",
      "Sounds good to me.",
      "Let me check and get back to you.",
      "Perfect, that works."
    )
  }
}
