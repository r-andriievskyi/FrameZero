package com.frame.zero.feature.chat

sealed interface ChatEvent {
  /** A message was just sent; the UI scrolls the list to the newest bubble. */
  data object MessageSent : ChatEvent
}
