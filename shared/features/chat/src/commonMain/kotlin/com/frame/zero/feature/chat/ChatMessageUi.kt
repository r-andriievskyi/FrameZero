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
