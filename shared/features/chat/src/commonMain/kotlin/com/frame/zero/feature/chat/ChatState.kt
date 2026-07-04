package com.frame.zero.feature.chat

import com.frame.zero.ui.UiText

data class ChatState(
  val draft: String = "",
  val isLoadingConversation: Boolean = true,
  val isReady: Boolean = false,
  val conversationError: UiText? = null,
  val isSending: Boolean = false,
  val sendError: UiText? = null
) {
  val canSend: Boolean
    get() = draft.isNotBlank() && isReady && !isSending
}
