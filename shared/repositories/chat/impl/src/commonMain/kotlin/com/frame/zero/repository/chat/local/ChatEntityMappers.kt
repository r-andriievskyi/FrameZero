package com.frame.zero.repository.chat.local

import com.frame.zero.database.ConversationEntity
import com.frame.zero.database.MessageEntity
import com.frame.zero.database.PendingMessageEntity
import com.frame.zero.domain.chat.ChatMessage
import com.frame.zero.domain.chat.Conversation
import com.frame.zero.domain.chat.PendingChatMessage
import com.frame.zero.domain.chat.PendingMessageStatus
import com.frame.zero.dto.chat.ChatMessageDto
import com.frame.zero.dto.chat.ConversationDto
import kotlin.time.Instant

internal fun ChatMessageDto.toEntity(): MessageEntity =
  MessageEntity(
    id = id,
    conversationId = conversationId,
    ordinal = ordinal,
    senderUserId = senderUserId,
    body = body,
    clientMessageId = clientMessageId,
    createdAtEpochMs = createdAt.toEpochMilliseconds()
  )

internal fun MessageEntity.toDomain(): ChatMessage =
  ChatMessage(
    id = id,
    conversationId = conversationId,
    ordinal = ordinal,
    senderUserId = senderUserId,
    body = body,
    clientMessageId = clientMessageId,
    createdAt = Instant.fromEpochMilliseconds(createdAtEpochMs)
  )

internal fun PendingMessageEntity.toDomain(): PendingChatMessage =
  PendingChatMessage(
    clientMessageId = clientMessageId,
    conversationId = conversationId,
    body = body,
    // An unknown name (row written by a newer build) degrades to Failed rather than throwing inside
    // the pending-messages Flow; the user can still retry or discard it.
    status = PendingMessageStatus.entries.firstOrNull { it.name == status } ?: PendingMessageStatus.Failed,
    attemptCount = attemptCount,
    createdAt = Instant.fromEpochMilliseconds(createdAtEpochMs)
  )

internal fun ConversationDto.toDomain(): Conversation =
  when (this) {
    is ConversationDto.Task ->
      Conversation(
        id = id,
        taskId = taskId,
        productionId = productionId,
        createdAt = createdAt,
        latestOrdinal = latestOrdinal,
        lastReadOrdinal = lastReadOrdinal
      )
  }

internal fun Conversation.toEntity(): ConversationEntity =
  ConversationEntity(
    id = id,
    taskId = taskId,
    productionId = productionId,
    createdAtEpochMs = createdAt.toEpochMilliseconds(),
    latestOrdinal = latestOrdinal,
    lastReadOrdinal = lastReadOrdinal
  )

internal fun ConversationEntity.toDomain(): Conversation =
  Conversation(
    id = id,
    taskId = taskId,
    productionId = productionId,
    createdAt = Instant.fromEpochMilliseconds(createdAtEpochMs),
    latestOrdinal = latestOrdinal,
    lastReadOrdinal = lastReadOrdinal
  )
