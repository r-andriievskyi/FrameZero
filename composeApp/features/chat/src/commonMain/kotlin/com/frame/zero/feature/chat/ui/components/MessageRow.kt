package com.frame.zero.feature.chat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.frame.zero.feature.chat.ChatMessageUi
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import kotlinx.datetime.LocalDate

/**
 * One row in the message list: an optional day separator above the bubble. The list decides
 * [showDaySeparator] (true for the oldest message of each day); rendering it inside the row —
 * above the bubble — puts the label at the top of that day's group under `reverseLayout`.
 */
@Composable
internal fun MessageRow(
  message: ChatMessageUi,
  showDaySeparator: Boolean,
  today: LocalDate,
  modifier: Modifier = Modifier
) {
  Column(modifier = modifier) {
    if (showDaySeparator) {
      DaySeparator(day = message.day, today = today)
      VerticalSpacer(AppTheme.spacingSystem.space4)
    }
    MessageBubble(message)
  }
}

@LightDarkPreview
@Composable
private fun MessageRowPreview() {
  AppTheme {
    Column(
      modifier = Modifier
        .background(AppTheme.colorSystem.background)
        .padding(AppTheme.spacingSystem.space16)
    ) {
      MessageRow(
        message = ChatMessageUi(
          id = "1",
          ordinal = 1,
          body = "Can you review the Scene 12 revisions before the table read?",
          isOwn = false,
          timeLabel = "2:02 PM",
          day = LocalDate(2026, 7, 4)
        ),
        showDaySeparator = true,
        today = LocalDate(2026, 7, 4)
      )
      MessageRow(
        message = ChatMessageUi(
          id = "2",
          ordinal = 2,
          body = "On it — sending notes tonight.",
          isOwn = true,
          timeLabel = "2:05 PM",
          day = LocalDate(2026, 7, 4)
        ),
        showDaySeparator = false,
        today = LocalDate(2026, 7, 4)
      )
    }
  }
}
