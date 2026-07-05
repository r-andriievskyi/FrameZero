package com.frame.zero.feature.task.details.usecase

import com.frame.zero.repository.chat.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Live unread-message count for the task's chat, driving the badge on the chat entry point.
 * Emits 0 until the chat is first opened (no participant row yet), so a thread the user has
 * never joined stays quiet by default.
 */
class ObserveTaskChatUnreadUseCase(
  private val chatRepository: ChatRepository
) {
  operator fun invoke(taskId: String): Flow<Int> =
    chatRepository.observeConversation(taskId).map { conversation ->
      conversation?.unreadCount?.toInt() ?: 0
    }
}
