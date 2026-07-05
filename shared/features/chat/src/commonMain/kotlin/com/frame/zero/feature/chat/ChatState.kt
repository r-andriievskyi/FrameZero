package com.frame.zero.feature.chat

import com.frame.zero.ui.UiText

data class ChatState(
  val draft: String = "",
  val isLoadingConversation: Boolean = true,
  val isReady: Boolean = false,
  val conversationError: UiText? = null,
  val isSending: Boolean = false,
  val sendError: UiText? = null,
  // The read cursor captured when the conversation opened. A "New messages" divider renders
  // just above the first message with a higher ordinal. Null when there was nothing unread
  // at open; kept for the whole session so the marker doesn't jump as you read.
  val newMessagesDividerOrdinal: Long? = null
) {
  val canSend: Boolean
    get() = draft.isNotBlank() && isReady && !isSending
}
