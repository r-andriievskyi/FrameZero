package com.frame.zero.feature.chat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.frame.zero.feature.chat.ChatMessageUi
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import kotlinx.datetime.LocalDate

private val MaxBubbleWidth = 300.dp

@Composable
internal fun MessageBubble(
  message: ChatMessageUi,
  modifier: Modifier = Modifier
) {
  val colorSystem = AppTheme.colorSystem
  val spacingSystem = AppTheme.spacingSystem
  val radiusSystem = AppTheme.radiusSystem

  val bubbleColor = if (message.isOwn) colorSystem.accent else colorSystem.cardBackground
  val bodyColor = if (message.isOwn) colorSystem.textOnAccent else colorSystem.textPrimary

  val shape = RoundedCornerShape(
    topStart = radiusSystem.radius16,
    topEnd = radiusSystem.radius16,
    bottomStart = if (message.isOwn) radiusSystem.radius16 else radiusSystem.radius4,
    bottomEnd = if (message.isOwn) radiusSystem.radius4 else radiusSystem.radius16
  )

  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = if (message.isOwn) Arrangement.End else Arrangement.Start
  ) {
    // The timestamp sits below the bubble (outside it), aligned to the bubble's outer edge.
    Column(horizontalAlignment = if (message.isOwn) Alignment.End else Alignment.Start) {
      Box(
        modifier = Modifier
          .widthIn(max = MaxBubbleWidth)
          .clip(shape)
          .background(bubbleColor)
          .padding(horizontal = spacingSystem.space12, vertical = spacingSystem.space8)
      ) {
        Text(
          text = message.body,
          style = AppTheme.typographySystem.bodyLarge,
          color = bodyColor
        )
      }
      VerticalSpacer(spacingSystem.space4)
      Text(
        text = message.timeLabel,
        style = AppTheme.typographySystem.caption,
        color = colorSystem.textMuted,
        textAlign = TextAlign.End,
        modifier = Modifier.padding(horizontal = spacingSystem.space4)
      )
    }
  }
}

@LightDarkPreview
@Composable
private fun MessageBubblePreview() {
  AppTheme {
    Column(
      modifier = Modifier
        .background(AppTheme.colorSystem.background)
        .padding(AppTheme.spacingSystem.space16)
    ) {
      MessageBubble(
        ChatMessageUi(
          id = "1",
          ordinal = 1,
          body = "Can you review the Scene 12 revisions before the table read?",
          isOwn = false,
          timeLabel = "14:02",
          day = LocalDate(2026, 7, 4)
        )
      )
      VerticalSpacer(AppTheme.spacingSystem.space8)
      MessageBubble(
        ChatMessageUi(
          id = "2",
          ordinal = 2,
          body = "On it — sending notes tonight.",
          isOwn = true,
          timeLabel = "14:05",
          day = LocalDate(2026, 7, 4)
        )
      )
    }
  }
}
