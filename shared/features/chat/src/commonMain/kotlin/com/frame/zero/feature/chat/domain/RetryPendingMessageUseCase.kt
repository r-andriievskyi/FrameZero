package com.frame.zero.feature.chat.domain

import com.frame.zero.domain.UseCase
import com.frame.zero.repository.chat.ChatRepository

/** Puts a given-up message back in the queue, keeping its id so the resend stays idempotent. */
class RetryPendingMessageUseCase(
  private val chatRepository: ChatRepository
) : UseCase<RetryPendingMessageUseCase.Params, Unit>() {
  data class Params(
    val conversationId: String,
    val clientMessageId: String
  )

  override suspend fun execute(params: Params) =
    chatRepository.retryPending(params.conversationId, params.clientMessageId)
}
