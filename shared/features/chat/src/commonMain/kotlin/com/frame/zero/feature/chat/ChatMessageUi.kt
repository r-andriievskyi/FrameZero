package com.frame.zero.feature.chat

import kotlinx.datetime.LocalDate

/**
 * A message rendered in the list. The day-separator label is resolved in the UI (it needs
 * localized "Today"/"Yesterday" strings), so the model only carries the raw [day].
 */
data class ChatMessageUi(
  val id: String,
  val ordinal: Long,
  val body: String,
  val isOwn: Boolean,
  val timeLabel: String,
  val day: LocalDate
)

/**
 * A message the user has composed that the server hasn't confirmed yet. Always the user's own, and
 * always newer than every confirmed message, so it renders after them with no ordinal of its own —
 * the server assigns that on arrival.
 */
data class PendingMessageUi(
  val clientMessageId: String,
  val body: String,
  val timeLabel: String,
  val day: LocalDate,
  /** Delivery gave up; the row now offers retry and discard instead of a progress hint. */
  val isFailed: Boolean
)
