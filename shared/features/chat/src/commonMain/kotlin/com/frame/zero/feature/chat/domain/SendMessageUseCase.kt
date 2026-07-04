package com.frame.zero.feature.chat.domain

import com.frame.zero.domain.UseCase
import com.frame.zero.repository.chat.ChatRepository

class SendMessageUseCase(
  private val chatRepository: ChatRepository
) : UseCase<SendMessageUseCase.Params, Unit>() {
  data class Params(
    val conversationId: String,
    val clientMessageId: String,
    val body: String
  )

  override suspend fun execute(params: Params) =
    chatRepository.send(params.conversationId, params.clientMessageId, params.body)
}
