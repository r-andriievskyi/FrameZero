package com.frame.zero.feature.chat.domain

import com.frame.zero.domain.UseCase
import com.frame.zero.domain.chat.Conversation
import com.frame.zero.repository.chat.ChatRepository

/**
 * Resolves the task's conversation and starts the live subscription. Falls back to the cached
 * conversation when the network get-or-create fails, so an offline reopen still works if the
 * chat was opened before.
 */
class OpenConversationUseCase(
  private val chatRepository: ChatRepository
) : UseCase<String, Conversation>() {
  override suspend fun execute(params: String): Conversation {
    val conversation = runCatching { chatRepository.getOrCreateConversation(params) }
      .getOrElse { error ->
        chatRepository.cachedConversation(params) ?: throw error
      }
    chatRepository.subscribe(conversation.id)
    return conversation
  }
}
