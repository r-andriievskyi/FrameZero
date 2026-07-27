package com.frame.zero.feature.chat.domain

import com.frame.zero.domain.UseCase
import com.frame.zero.repository.chat.ChatRepository

/**
 * Hands a composed message to the outbox. Returns once it is stored, not once it is delivered, so
 * it succeeds offline; the only failure it can report is that queueing itself went wrong.
 */
class EnqueueMessageUseCase(
  private val chatRepository: ChatRepository
) : UseCase<EnqueueMessageUseCase.Params, Unit>() {
  data class Params(
    val conversationId: String,
    val clientMessageId: String,
    val body: String
  )

  override suspend fun execute(params: Params) =
    chatRepository.enqueue(params.conversationId, params.clientMessageId, params.body)
}
