package com.frame.zero.feature.chat.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.modifier.clickableWithRipple
import com.frame.zero.shared.design_system.widgets.HorizontalSpacer

private val SendButtonSize = 44.dp
private val FieldMinHeight = 44.dp
private val FieldMaxHeight = 120.dp
private val SendIconSize = 20.dp

@Composable
internal fun ChatInputBar(
  value: String,
  onValueChange: (String) -> Unit,
  onSend: () -> Unit,
  canSend: Boolean,
  placeholder: String,
  sendContentDescription: String,
  modifier: Modifier = Modifier
) {
  val colorSystem = AppTheme.colorSystem
  val spacingSystem = AppTheme.spacingSystem
  val fieldShape = RoundedCornerShape(AppTheme.radiusSystem.radius16)

  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(spacingSystem.space12),
    verticalAlignment = Alignment.Bottom
  ) {
    Box(
      modifier = Modifier
        .weight(1f)
        .heightIn(min = FieldMinHeight, max = FieldMaxHeight)
        .clip(fieldShape)
        .background(colorSystem.inputBackground, fieldShape)
        .border(width = AppTheme.borderSystem.hairline, color = colorSystem.border, shape = fieldShape)
        .padding(horizontal = spacingSystem.space16, vertical = spacingSystem.space12),
      contentAlignment = Alignment.CenterStart
    ) {
      if (value.isEmpty()) {
        Text(
          text = placeholder,
          style = AppTheme.typographySystem.bodyLarge,
          color = colorSystem.textMuted
        )
      }
      BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        textStyle = AppTheme.typographySystem.bodyLarge.copy(color = colorSystem.textPrimary),
        cursorBrush = SolidColor(colorSystem.accent),
        maxLines = 5
      )
    }

    HorizontalSpacer(spacingSystem.space8)

    SendButton(
      enabled = canSend,
      onClick = onSend,
      contentDescription = sendContentDescription
    )
  }
}

@Composable
private fun SendButton(
  enabled: Boolean,
  onClick: () -> Unit,
  contentDescription: String
) {
  val colorSystem = AppTheme.colorSystem
  val background = if (enabled) colorSystem.accent else colorSystem.inputBackground
  val iconColor = if (enabled) colorSystem.textOnAccent else colorSystem.textMuted

  Box(
    modifier = Modifier
      .size(SendButtonSize)
      .clip(RoundedCornerShape(AppTheme.radiusSystem.radiusMax))
      .background(background)
      .clickableWithRipple(
        color = colorSystem.textOnAccent,
        bounded = true,
        enabled = enabled,
        role = Role.Button,
        onClickLabel = contentDescription,
        onClick = onClick
      ),
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.size(SendIconSize)) {
      val stroke = Stroke(width = size.minDimension * 0.12f, cap = StrokeCap.Round, join = StrokeJoin.Round)
      val cx = size.width / 2f
      val top = size.height * 0.18f
      val bottom = size.height * 0.82f
      // Upward "send" arrow.
      drawLine(iconColor, Offset(cx, bottom), Offset(cx, top), stroke.width, cap = StrokeCap.Round)
      val head = size.width * 0.28f
      drawLine(iconColor, Offset(cx - head, top + head), Offset(cx, top), stroke.width, cap = StrokeCap.Round)
      drawLine(iconColor, Offset(cx + head, top + head), Offset(cx, top), stroke.width, cap = StrokeCap.Round)
    }
  }
}

@LightDarkPreview
@Composable
private fun ChatInputBarPreview() {
  AppTheme {
    Box(modifier = Modifier.background(AppTheme.colorSystem.background)) {
      ChatInputBar(
        value = "Sounds good",
        onValueChange = {},
        onSend = {},
        canSend = true,
        placeholder = "Message",
        sendContentDescription = "Send"
      )
    }
  }
}
