package com.frame.zero.feature.chat.domain

import com.frame.zero.domain.chat.PendingChatMessage
import com.frame.zero.repository.chat.ChatRepository
import kotlinx.coroutines.flow.Flow

/**
 * Live view of the conversation's outbox — messages composed but not yet confirmed — oldest first.
 * Emits an empty list once everything is delivered.
 */
class ObservePendingMessagesUseCase(
  private val chatRepository: ChatRepository
) {
  operator fun invoke(conversationId: String): Flow<List<PendingChatMessage>> =
    chatRepository.observePending(conversationId)
}
