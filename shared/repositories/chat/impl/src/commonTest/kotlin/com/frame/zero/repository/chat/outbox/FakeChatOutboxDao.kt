package com.frame.zero.repository.chat.outbox

import com.frame.zero.database.ChatOutboxDao
import com.frame.zero.database.PendingMessageEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** In-memory [ChatOutboxDao] for tests; mirrors the Room DAO semantics, ordering included. */
internal class FakeChatOutboxDao : ChatOutboxDao {
  val rows = MutableStateFlow<List<PendingMessageEntity>>(emptyList())

  private var nextSequence = 1L

  override fun observeByConversation(conversationId: String): Flow<List<PendingMessageEntity>> =
    rows.map { all -> all.filter { it.conversationId == conversationId }.sortedBy { it.sequence } }

  override suspend fun nextQueued(
    conversationId: String,
    queued: String,
    sending: String
  ): PendingMessageEntity? {
    val inConversation = rows.value.filter { it.conversationId == conversationId }
    if (inConversation.any { it.status == sending }) return null
    return inConversation.filter { it.status == queued }.minByOrNull { it.sequence }
  }

  override suspend fun claim(
    conversationId: String,
    clientMessageId: String,
    queued: String,
    sending: String
  ): Int {
    val claimed = rows.value.filter {
      it.conversationId == conversationId && it.clientMessageId == clientMessageId && it.status == queued
    }
    rows.value = rows.value.map { if (it in claimed) it.copy(status = sending) else it }
    return claimed.size
  }

  override suspend fun resetInFlight(
    conversationId: String,
    queued: String,
    sending: String
  ): Int {
    val stranded = rows.value.filter { it.conversationId == conversationId && it.status == sending }
    rows.value = rows.value.map {
      if (it.conversationId == conversationId && it.status == sending) it.copy(status = queued) else it
    }
    return stranded.size
  }

  override suspend fun conversationsWithPending(
    queued: String,
    sending: String
  ): List<String> =
    rows.value.filter { it.status == queued || it.status == sending }
      .map { it.conversationId }
      .distinct()
      .sorted()

  override suspend fun get(
    conversationId: String,
    clientMessageId: String
  ): PendingMessageEntity? = rows.value.firstOrNull { it.matches(conversationId, clientMessageId) }

  override suspend fun insert(entity: PendingMessageEntity) {
    // Mirrors OnConflictStrategy.IGNORE on the (conversationId, clientMessageId) unique index.
    if (get(entity.conversationId, entity.clientMessageId) != null) return
    rows.value = rows.value + entity.copy(sequence = nextSequence++)
  }

  override suspend fun updateStatus(
    conversationId: String,
    clientMessageId: String,
    status: String
  ) {
    rows.value = rows.value.map {
      if (it.matches(conversationId, clientMessageId)) it.copy(status = status) else it
    }
  }

  override suspend fun updateStatusAndCountAttempt(
    conversationId: String,
    clientMessageId: String,
    status: String
  ) {
    rows.value = rows.value.map {
      if (it.matches(conversationId, clientMessageId)) {
        it.copy(status = status, attemptCount = it.attemptCount + 1)
      } else {
        it
      }
    }
  }

  override suspend fun delete(
    conversationId: String,
    clientMessageId: String
  ) {
    rows.value = rows.value.filterNot { it.matches(conversationId, clientMessageId) }
  }

  private fun PendingMessageEntity.matches(
    conversationId: String,
    clientMessageId: String
  ): Boolean = this.conversationId == conversationId && this.clientMessageId == clientMessageId
}
