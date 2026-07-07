package com.frame.zero.chat

import com.frame.zero.AppError
import com.frame.zero.AppException
import com.frame.zero.common.ProductionMemberRevocationListener
import com.frame.zero.common.TaskCircleRevocationListener
import com.frame.zero.common.Transactor
import com.frame.zero.dto.chat.ChatMessageDto
import com.frame.zero.dto.chat.ChatSocketFrame
import com.frame.zero.dto.chat.ConversationDto
import java.util.UUID

class ChatService(
  private val conversationRepository: ConversationRepository,
  private val messageRepository: MessageRepository,
  private val taskCircleAccessService: TaskCircleAccessService,
  private val transactor: Transactor,
  private val hub: ChatHub
) {
  suspend fun getOrCreateTaskConversation(
    userId: UUID,
    taskId: UUID
  ): ConversationDto =
    transactor.transaction {
      val task = taskCircleAccessService.requireCircle(userId, taskId)
      val conversation = conversationRepository.getOrCreateTaskConversation(taskId, task.productionId)
      conversationRepository.ensureParticipant(conversation.id, userId)
      conversation.toDto(
        latestOrdinal = messageRepository.maxOrdinal(conversation.id),
        lastReadOrdinal = conversationRepository.lastReadOrdinal(conversation.id, userId)
      )
    }

  suspend fun listMessages(
    userId: UUID,
    conversationId: UUID,
    before: Long?,
    limit: Int
  ): Pair<List<ChatMessageDto>, String?> =
    transactor.transaction {
      authorizeConversation(userId, conversationId)
      val records = messageRepository.findByConversation(conversationId, before, limit)
      val nextCursor = if (records.size == limit) records.last().ordinal.toString() else null
      records.map { it.toDto() } to nextCursor
    }

  /**
   * Persists a message (server-assigned ordinal, idempotent on `clientMessageId`) and
   * broadcasts it to live subscribers **after** the transaction commits, so a
   * failed insert never fans out a phantom message. A replayed `clientMessageId`
   * is not re-broadcast — subscribers already got it the first time.
   */
  suspend fun send(
    userId: UUID,
    conversationId: UUID,
    clientMessageId: String,
    body: String
  ): ChatMessageDto {
    val trimmed = body.trim()
    validateBody(trimmed)
    validateClientMessageId(clientMessageId)

    val result = transactor.transaction {
      authorizeConversation(userId, conversationId)
      conversationRepository.ensureParticipant(conversationId, userId)
      messageRepository.append(conversationId, userId, trimmed, clientMessageId)
    }

    val dto = result.message.toDto()
    if (result.isNew) hub.broadcast(conversationId, ChatSocketFrame.Message(dto))
    return dto
  }

  /**
   * Advances the caller's read cursor in [conversationId], clamped forward-only to
   * `[current, latest]` so a stale or malicious client can never move it backwards or
   * past the newest message. Broadcasts a `READ` frame to the caller's own other
   * connections only when the cursor actually moves. Returns the applied ordinal.
   */
  suspend fun markRead(
    userId: UUID,
    conversationId: UUID,
    requestedOrdinal: Long
  ): Long {
    val (applied, changed) = transactor.transaction {
      authorizeConversation(userId, conversationId)
      conversationRepository.ensureParticipant(conversationId, userId)
      val current = conversationRepository.lastReadOrdinal(conversationId, userId)
      val latest = messageRepository.maxOrdinal(conversationId)
      val clamped = requestedOrdinal.coerceAtMost(latest).coerceAtLeast(current)
      if (clamped != current) {
        conversationRepository.updateLastReadOrdinal(conversationId, userId, clamped)
      }
      clamped to (clamped != current)
    }
    if (changed) {
      hub.sendToUser(userId, ChatSocketFrame.Read(conversationId.toString(), applied))
    }
    return applied
  }

  /** Non-throwing task-circle check for the WebSocket SUBSCRIBE path. */
  suspend fun canAccessConversation(
    userId: UUID,
    conversationId: UUID
  ): Boolean =
    transactor.transaction {
      val conversation = conversationRepository.findById(conversationId) ?: return@transaction false
      val taskId = conversation.taskId ?: return@transaction false
      taskCircleAccessService.isInCircle(userId, taskId)
    }

  // Resolves the conversation and re-checks the task circle from the DB. MVP only
  // has TASK-kind conversations, so a null task_id is treated as not-found.
  private suspend fun authorizeConversation(
    userId: UUID,
    conversationId: UUID
  ): ConversationRecord {
    val conversation = conversationRepository.findById(conversationId) ?: throw AppException(AppError.NotFound)
    val taskId = conversation.taskId ?: throw AppException(AppError.NotFound)
    taskCircleAccessService.requireCircle(userId, taskId)
    return conversation
  }

  private fun validateBody(body: String) {
    if (body.isBlank()) throw AppException(AppError.ValidationError(mapOf("body" to "Required")))
    if (body.length > MAX_BODY_LENGTH) {
      throw AppException(AppError.ValidationError(mapOf("body" to "Max $MAX_BODY_LENGTH characters")))
    }
  }

  private fun validateClientMessageId(clientMessageId: String) {
    if (clientMessageId.isBlank() || clientMessageId.length > MAX_CLIENT_MESSAGE_ID_LENGTH) {
      throw AppException(AppError.ValidationError(mapOf("clientMessageId" to "Invalid")))
    }
  }

  private fun ConversationRecord.toDto(
    latestOrdinal: Long,
    lastReadOrdinal: Long
  ): ConversationDto =
    when (kind) {
      // Exhaustive on purpose: adding DIRECT storage forces a matching DTO subtype
      // here rather than silently falling back to a TASK shape.
      ConversationKind.TASK ->
        ConversationDto.Task(
          id = id.toString(),
          productionId = productionId.toString(),
          createdAt = createdAt,
          taskId = requireNotNull(taskId) { "TASK conversation $id has no taskId" }.toString(),
          latestOrdinal = latestOrdinal,
          lastReadOrdinal = lastReadOrdinal
        )
      ConversationKind.DIRECT -> error("DIRECT conversations are not implemented yet")
    }

  private fun MessageRecord.toDto(): ChatMessageDto =
    ChatMessageDto(
      id = id.toString(),
      conversationId = conversationId.toString(),
      ordinal = ordinal,
      senderUserId = senderUserId.toString(),
      body = body,
      clientMessageId = clientMessageId,
      createdAt = createdAt
    )

  private companion object {
    // Enforced server-side per the abuse-limits posture; matches the client bound.
    const val MAX_BODY_LENGTH = 4_000

    // Column width of messages.client_message_id.
    const val MAX_CLIENT_MESSAGE_ID_LENGTH = 64
  }
}

/**
 * Wires task-circle changes to the hub so a user removed from a task loses their
 * live subscription immediately, not just at their next SUBSCRIBE.
 */
class ChatTaskCircleRevoker(
  private val conversations: ConversationRepository,
  private val hub: ChatHub
) : TaskCircleRevocationListener {
  override suspend fun onTaskCircleChanged(
    taskId: UUID,
    circleUserIds: Set<UUID>
  ) {
    val conversation = conversations.findByTaskId(taskId) ?: return
    hub.retainSubscribers(conversation.id, circleUserIds)
  }
}

/**
 * Wires production-member removal to the hub so a user removed from a production
 * stops receiving live chat for its conversations immediately. Their REST access
 * is already gone at that point; this closes the still-subscribed-socket gap.
 */
class ChatProductionMemberRevoker(
  private val conversations: ConversationRepository,
  private val hub: ChatHub
) : ProductionMemberRevocationListener {
  override suspend fun onProductionMemberRemoved(
    productionId: UUID,
    userId: UUID
  ) {
    hub.dropSubscriptions(userId, conversations.findIdsByProductionId(productionId))
  }
}
