package com.frame.zero.repository.chat.outbox

import com.frame.zero.database.ChatOutboxDao
import com.frame.zero.database.PendingMessageEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** In-memory [ChatOutboxDao] for tests; mirrors the Room DAO semantics, ordering included. */
internal class FakeChatOutboxDao : ChatOutboxDao {
  val rows = MutableStateFlow<List<PendingMessageEntity>>(emptyList())

  override fun observeByConversation(conversationId: String): Flow<List<PendingMessageEntity>> =
    rows.map { all -> all.filter { it.conversationId == conversationId }.sortedBy { it.createdAtEpochMs } }

  override suspend fun nextQueued(
    conversationId: String,
    status: String
  ): PendingMessageEntity? =
    rows.value
      .filter { it.conversationId == conversationId && it.status == status }
      .minByOrNull { it.createdAtEpochMs }

  override suspend fun conversationsWithQueued(status: String): List<String> =
    rows.value.filter { it.status == status }.map { it.conversationId }.distinct()

  override suspend fun get(clientMessageId: String): PendingMessageEntity? =
    rows.value.firstOrNull { it.clientMessageId == clientMessageId }

  override suspend fun upsert(entity: PendingMessageEntity) {
    rows.value = rows.value.filterNot { it.clientMessageId == entity.clientMessageId } + entity
  }

  override suspend fun updateStatus(
    clientMessageId: String,
    status: String
  ) {
    rows.value = rows.value.map { if (it.clientMessageId == clientMessageId) it.copy(status = status) else it }
  }

  override suspend fun updateStatusAndCountAttempt(
    clientMessageId: String,
    status: String
  ) {
    rows.value = rows.value.map {
      if (it.clientMessageId == clientMessageId) it.copy(status = status, attemptCount = it.attemptCount + 1) else it
    }
  }

  override suspend fun delete(clientMessageId: String) {
    rows.value = rows.value.filterNot { it.clientMessageId == clientMessageId }
  }
}
