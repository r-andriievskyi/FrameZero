package com.frame.zero.feature.chat.domain

import com.frame.zero.domain.UseCase
import com.frame.zero.repository.chat.ChatRepository

/**
 * Removes a pending message so it is never sent. Best-effort: if the drain already handed it to the
 * network it still lands, the same way a delivered message can't be recalled.
 */
class DiscardPendingMessageUseCase(
  private val chatRepository: ChatRepository
) : UseCase<DiscardPendingMessageUseCase.Params, Unit>() {
  data class Params(
    val conversationId: String,
    val clientMessageId: String
  )

  override suspend fun execute(params: Params) =
    chatRepository.discardPending(params.conversationId, params.clientMessageId)
}
