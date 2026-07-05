package com.frame.zero.feature.chat.domain

import com.frame.zero.domain.UseCase
import com.frame.zero.repository.chat.ChatRepository

/** Advances the caller's read cursor for a conversation. Server clamps it forward-only. */
class MarkReadUseCase(
  private val chatRepository: ChatRepository
) : UseCase<MarkReadUseCase.Params, Unit>() {
  data class Params(
    val conversationId: String,
    val lastReadOrdinal: Long
  )

  override suspend fun execute(params: Params) =
    chatRepository.markRead(params.conversationId, params.lastReadOrdinal)
}
