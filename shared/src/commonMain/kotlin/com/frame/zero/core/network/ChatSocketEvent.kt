package com.frame.zero.core.network

import com.frame.zero.dto.chat.ChatMessageDto

sealed interface ChatSocketEvent {
  data class MessageReceived(
    val message: ChatMessageDto
  ) : ChatSocketEvent

  /** The user's read cursor advanced on another device. */
  data class ReadUpdated(
    val conversationId: String,
    val lastReadOrdinal: Long
  ) : ChatSocketEvent

  data object Connected : ChatSocketEvent
}
