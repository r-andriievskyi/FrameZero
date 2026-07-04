package com.frame.zero.repository.chat.local

import com.frame.zero.database.ConversationEntity
import com.frame.zero.database.MessageEntity
import com.frame.zero.domain.chat.ChatMessage
import com.frame.zero.domain.chat.Conversation
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

internal fun ConversationDto.toDomain(): Conversation =
  when (this) {
    is ConversationDto.Task ->
      Conversation(
        id = id,
        taskId = taskId,
        productionId = productionId,
        createdAt = createdAt
      )
  }

internal fun Conversation.toEntity(): ConversationEntity =
  ConversationEntity(
    id = id,
    taskId = taskId,
    productionId = productionId,
    createdAtEpochMs = createdAt.toEpochMilliseconds()
  )

internal fun ConversationEntity.toDomain(): Conversation =
  Conversation(
    id = id,
    taskId = taskId,
    productionId = productionId,
    createdAt = Instant.fromEpochMilliseconds(createdAtEpochMs)
  )
