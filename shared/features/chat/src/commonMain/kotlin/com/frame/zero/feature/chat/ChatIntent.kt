package com.frame.zero.feature.chat

sealed interface ChatIntent {
  data class MessageChanged(
    val text: String
  ) : ChatIntent

  data object SendClicked : ChatIntent

  data object Retry : ChatIntent

  /** Re-queue a message whose delivery was given up on. */
  data class RetryPending(
    val clientMessageId: String
  ) : ChatIntent

  /**
   * Drop a pending message. Best-effort: one already handed to the network still lands, the same
   * way a sent message can't be recalled.
   */
  data class DiscardPending(
    val clientMessageId: String
  ) : ChatIntent

  /**
   * The list is resumed and scrolled to the newest message [ordinal]; advance the read
   * cursor up to it. The VM ignores non-advancing values, so repeated emissions are cheap.
   */
  data class MarkRead(
    val ordinal: Long
  ) : ChatIntent
}
