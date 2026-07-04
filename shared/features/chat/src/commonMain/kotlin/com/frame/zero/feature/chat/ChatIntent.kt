package com.frame.zero.feature.chat

sealed interface ChatIntent {
  data class MessageChanged(
    val text: String
  ) : ChatIntent

  data object SendClicked : ChatIntent

  data object Retry : ChatIntent

  data object SendErrorDismissed : ChatIntent
}
