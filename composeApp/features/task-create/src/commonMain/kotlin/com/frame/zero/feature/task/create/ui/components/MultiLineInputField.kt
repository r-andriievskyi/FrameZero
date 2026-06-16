package com.frame.zero.feature.task.create.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview

private val MinHeight = 96.dp

@Composable
internal fun MultiLineInputField(
  value: String,
  onValueChange: (String) -> Unit,
  placeholder: String,
  modifier: Modifier = Modifier
) {
  val colors = AppTheme.colorSystem
  val spacing = AppTheme.spacingSystem
  val typography = AppTheme.typographySystem
  val shape = RoundedCornerShape(AppTheme.radiusSystem.radius8)

  var fieldValue by remember(value) {
    mutableStateOf(TextFieldValue(value, TextRange(value.length)))
  }

  BasicTextField(
    value = fieldValue,
    onValueChange = {
      fieldValue = it
      onValueChange(it.text)
    },
    modifier = modifier
      .fillMaxWidth()
      .heightIn(min = MinHeight)
      .clip(shape)
      .background(colors.inputBackground, shape)
      .border(width = AppTheme.borderSystem.hairline, color = colors.border, shape = shape)
      .padding(horizontal = spacing.space16, vertical = spacing.space12),
    textStyle = typography.bodyLarge.copy(color = colors.textPrimary),
    cursorBrush = SolidColor(colors.accent),
    decorationBox = { innerTextField ->
      Box {
        if (value.isEmpty()) {
          Text(
            text = placeholder,
            style = typography.bodyLarge,
            color = colors.textMuted
          )
        }
        innerTextField()
      }
    }
  )
}

@LightDarkPreview
@Composable
private fun MultiLineInputFieldPreview() {
  AppTheme {
    MultiLineInputField(
      value = "",
      onValueChange = {},
      placeholder = "Add context, deliverables, or links…",
      modifier = Modifier.padding(AppTheme.spacingSystem.space16)
    )
  }
}
