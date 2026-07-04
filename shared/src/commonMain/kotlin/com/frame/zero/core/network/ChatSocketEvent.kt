package com.frame.zero.core.network

import com.frame.zero.dto.chat.ChatMessageDto

sealed interface ChatSocketEvent {
  data class MessageReceived(
    val message: ChatMessageDto
  ) : ChatSocketEvent

  data object Connected : ChatSocketEvent
}
