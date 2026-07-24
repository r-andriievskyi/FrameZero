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
import androidx.compose.ui.unit.dp
import com.frame.zero.feature.chat.PendingMessageUi
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.modifier.clickableWithRipple
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import framezero.composeapp.features.chat.generated.resources.Res
import framezero.composeapp.features.chat.generated.resources.chat_discard
import framezero.composeapp.features.chat.generated.resources.chat_not_sent
import framezero.composeapp.features.chat.generated.resources.chat_retry
import framezero.composeapp.features.chat.generated.resources.chat_sending
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

private val MaxBubbleWidth = 300.dp

/**
 * A message the user has sent that the server hasn't confirmed. Always own-side, and dimmed against
 * a confirmed own bubble so "on its way" reads at a glance. When delivery has been given up on, the
 * footer turns into retry/discard actions — the only way back for a parked message.
 */
@Composable
internal fun PendingMessageBubble(
  message: PendingMessageUi,
  onRetry: () -> Unit,
  onDiscard: () -> Unit,
  modifier: Modifier = Modifier
) {
  val colorSystem = AppTheme.colorSystem
  val spacingSystem = AppTheme.spacingSystem
  val radiusSystem = AppTheme.radiusSystem

  val shape = RoundedCornerShape(
    topStart = radiusSystem.radius16,
    topEnd = radiusSystem.radius16,
    bottomStart = radiusSystem.radius16,
    bottomEnd = radiusSystem.radius4
  )

  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.End
  ) {
    Column(horizontalAlignment = Alignment.End) {
      Box(
        modifier = Modifier
          .widthIn(max = MaxBubbleWidth)
          .clip(shape)
          .background(colorSystem.accentDim)
          .padding(horizontal = spacingSystem.space12, vertical = spacingSystem.space8)
      ) {
        Text(
          text = message.body,
          style = AppTheme.typographySystem.bodyLarge,
          color = colorSystem.textOnAccent
        )
      }
      VerticalSpacer(spacingSystem.space4)
      if (message.isFailed) {
        FailedFooter(message = message, onRetry = onRetry, onDiscard = onDiscard)
      } else {
        // Same timestamp a confirmed bubble carries, so a message doesn't visibly gain one the
        // moment the server acknowledges it.
        Text(
          text = "${message.timeLabel} · ${stringResource(Res.string.chat_sending)}",
          style = AppTheme.typographySystem.caption,
          color = colorSystem.textMuted,
          modifier = Modifier.padding(horizontal = spacingSystem.space4)
        )
      }
    }
  }
}

@Composable
private fun FailedFooter(
  message: PendingMessageUi,
  onRetry: () -> Unit,
  onDiscard: () -> Unit,
  modifier: Modifier = Modifier
) {
  val colorSystem = AppTheme.colorSystem
  val spacingSystem = AppTheme.spacingSystem
  val captionStyle = AppTheme.typographySystem.caption

  Row(
    modifier = modifier.padding(horizontal = spacingSystem.space4),
    horizontalArrangement = Arrangement.spacedBy(spacingSystem.space8),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = "${message.timeLabel} · ${stringResource(Res.string.chat_not_sent)}",
      style = captionStyle,
      color = colorSystem.errorText
    )
    Text(
      text = stringResource(Res.string.chat_retry),
      style = captionStyle,
      color = colorSystem.accentText,
      modifier = Modifier
        .clip(RoundedCornerShape(AppTheme.radiusSystem.radius4))
        .clickableWithRipple(color = colorSystem.accentText, bounded = true, onClick = onRetry)
        .padding(horizontal = spacingSystem.space4, vertical = spacingSystem.space2)
    )
    Text(
      text = stringResource(Res.string.chat_discard),
      style = captionStyle,
      color = colorSystem.textMuted,
      modifier = Modifier
        .clip(RoundedCornerShape(AppTheme.radiusSystem.radius4))
        .clickableWithRipple(color = colorSystem.textMuted, bounded = true, onClick = onDiscard)
        .padding(horizontal = spacingSystem.space4, vertical = spacingSystem.space2)
    )
  }
}

@LightDarkPreview
@Composable
private fun PendingMessageBubblePreview() {
  AppTheme {
    Column(
      modifier = Modifier
        .background(AppTheme.colorSystem.background)
        .padding(AppTheme.spacingSystem.space16)
    ) {
      PendingMessageBubble(
        message = PendingMessageUi(
          clientMessageId = "1",
          body = "Sending this one while the lift has no signal.",
          timeLabel = "14:05",
          day = LocalDate(2026, 7, 4),
          isFailed = false
        ),
        onRetry = {},
        onDiscard = {}
      )
      VerticalSpacer(AppTheme.spacingSystem.space8)
      PendingMessageBubble(
        message = PendingMessageUi(
          clientMessageId = "2",
          body = "This one the server refused.",
          timeLabel = "14:06",
          day = LocalDate(2026, 7, 4),
          isFailed = true
        ),
        onRetry = {},
        onDiscard = {}
      )
    }
  }
}
