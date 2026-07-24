package com.frame.zero.feature.chat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.frame.zero.feature.chat.PendingMessageUi
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import kotlinx.datetime.LocalDate

/**
 * One unconfirmed message in the list, with an optional day separator above it — the same shape as
 * [MessageRow], so a pending bubble sits in the flow exactly where its confirmed version will.
 */
@Composable
internal fun PendingMessageRow(
  message: PendingMessageUi,
  showDaySeparator: Boolean,
  today: LocalDate,
  onRetry: () -> Unit,
  onDiscard: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(modifier = modifier) {
    if (showDaySeparator) {
      DaySeparator(day = message.day, today = today)
      VerticalSpacer(AppTheme.spacingSystem.space4)
    }
    PendingMessageBubble(message = message, onRetry = onRetry, onDiscard = onDiscard)
  }
}

@LightDarkPreview
@Composable
private fun PendingMessageRowPreview() {
  AppTheme {
    Column(
      modifier = Modifier
        .background(AppTheme.colorSystem.background)
        .padding(AppTheme.spacingSystem.space16)
    ) {
      PendingMessageRow(
        message = PendingMessageUi(
          clientMessageId = "1",
          body = "Queued until the shuttle gets back in range.",
          timeLabel = "2:05 PM",
          day = LocalDate(2026, 7, 4),
          isFailed = false
        ),
        showDaySeparator = true,
        today = LocalDate(2026, 7, 4),
        onRetry = {},
        onDiscard = {}
      )
    }
  }
}
