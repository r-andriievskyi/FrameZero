package com.frame.zero.feature.chat

sealed interface ChatIntent {
  data class MessageChanged(
    val text: String
  ) : ChatIntent

  data object SendClicked : ChatIntent

  data object Retry : ChatIntent

  data object SendErrorDismissed : ChatIntent

  /**
   * The list is resumed and scrolled to the newest message [ordinal]; advance the read
   * cursor up to it. The VM ignores non-advancing values, so repeated emissions are cheap.
   */
  data class MarkRead(
    val ordinal: Long
  ) : ChatIntent
}
