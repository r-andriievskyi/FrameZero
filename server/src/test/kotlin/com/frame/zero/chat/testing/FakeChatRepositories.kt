package com.frame.zero.chat.testing

import com.frame.zero.chat.AppendResult
import com.frame.zero.chat.ConversationKind
import com.frame.zero.chat.ConversationRecord
import com.frame.zero.chat.ConversationRepository
import com.frame.zero.chat.MessageRecord
import com.frame.zero.chat.MessageRepository
import kotlin.time.Clock
import java.util.UUID

internal class FakeConversationRepository : ConversationRepository {
  val conversations: MutableList<ConversationRecord> = mutableListOf()

  // (conversationId, userId) pairs created lazily on first interaction.
  val participants: MutableSet<Pair<UUID, UUID>> = mutableSetOf()

  override suspend fun findById(id: UUID): ConversationRecord? = conversations.firstOrNull { it.id == id }

  override suspend fun findByTaskId(taskId: UUID): ConversationRecord? =
    conversations.firstOrNull { it.taskId == taskId }

  override suspend fun getOrCreateTaskConversation(
    taskId: UUID,
    productionId: UUID
  ): ConversationRecord =
    conversations.firstOrNull { it.taskId == taskId }
      ?: ConversationRecord(
        id = UUID.randomUUID(),
        kind = ConversationKind.TASK,
        taskId = taskId,
        productionId = productionId,
        createdAt = Clock.System.now()
      ).also { conversations += it }

  override suspend fun ensureParticipant(
    conversationId: UUID,
    userId: UUID
  ) {
    participants += conversationId to userId
  }
}

internal class FakeMessageRepository : MessageRepository {
  val messages: MutableList<MessageRecord> = mutableListOf()

  override suspend fun append(
    conversationId: UUID,
    senderUserId: UUID,
    body: String,
    clientMessageId: String
  ): AppendResult {
    // Idempotent on (conversation, sender, clientMessageId), mirroring the unique index.
    messages.firstOrNull {
      it.conversationId == conversationId && it.senderUserId == senderUserId && it.clientMessageId == clientMessageId
    }?.let { return AppendResult(it, isNew = false) }

    val nextSeq = (messages.filter { it.conversationId == conversationId }.maxOfOrNull { it.seq } ?: 0L) + 1
    val record =
      MessageRecord(
        id = UUID.randomUUID(),
        conversationId = conversationId,
        seq = nextSeq,
        senderUserId = senderUserId,
        body = body,
        clientMessageId = clientMessageId,
        createdAt = Clock.System.now()
      )
    messages += record
    return AppendResult(record, isNew = true)
  }

  override suspend fun findByConversation(
    conversationId: UUID,
    beforeSeq: Long?,
    limit: Int
  ): List<MessageRecord> =
    messages
      .filter { it.conversationId == conversationId && (beforeSeq == null || it.seq < beforeSeq) }
      .sortedByDescending { it.seq }
      .take(limit)
}
