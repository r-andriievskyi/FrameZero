package com.frame.zero.feature.chat

import com.frame.zero.ui.UiText
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class ChatState(
  val draft: String = "",
  val isLoadingConversation: Boolean = true,
  val isReady: Boolean = false,
  val conversationError: UiText? = null,
  /** The chat socket is down and retrying with backoff — sends still queue locally, but live
   *  delivery/read updates are paused until it reconnects. */
  val isDisconnected: Boolean = false,
  // Composed but unconfirmed messages, newest first to match the reversed message list. They render
  // after every confirmed message; a send never fails outright any more, it just stays here.
  val pending: ImmutableList<PendingMessageUi> = persistentListOf(),
  // The read cursor captured when the conversation opened. A "New messages" divider renders
  // just above the first message with a higher ordinal. Null when there was nothing unread
  // at open; kept for the whole session so the marker doesn't jump as you read.
  val newMessagesDividerOrdinal: Long? = null
) {
  val canSend: Boolean
    get() = draft.isNotBlank() && isReady
}
