package com.discovery.playground.shared.design_system.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.discovery.playground.shared.design_system.AppTheme

private val MinHeight = 48.dp
private val BorderWidth = 1.dp

@Composable
fun SingleLineInputField(
  value: String,
  onValueChange: (String) -> Unit,
  modifier: Modifier = Modifier,
  placeholder: String = "",
  leadingContent: (@Composable () -> Unit)? = null,
  trailingContent: (@Composable () -> Unit)? = null,
  enabled: Boolean = true,
  visualTransformation: VisualTransformation = VisualTransformation.None,
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  keyboardActions: KeyboardActions = KeyboardActions.Default
) {
  val colors = AppTheme.colorSystem
  val spacing = AppTheme.spacingSystem
  val radius = AppTheme.radiusSystem
  val typography = AppTheme.typographySystem

  var textFieldValue by
    remember(value) { mutableStateOf(TextFieldValue(value, TextRange(value.length))) }

  val shape = RoundedCornerShape(radius.radius8)

  BasicTextField(
    value = textFieldValue,
    onValueChange = {
      textFieldValue = it
      onValueChange(it.text)
    },
    modifier = modifier.fillMaxWidth().heightIn(min = MinHeight),
    enabled = enabled,
    singleLine = true,
    textStyle = typography.bodyLarge.copy(color = colors.textPrimary),
    cursorBrush = SolidColor(colors.accent),
    visualTransformation = visualTransformation,
    keyboardOptions = keyboardOptions,
    keyboardActions = keyboardActions,
    decorationBox = { innerTextField ->
      Row(
        modifier =
          Modifier
            .clip(shape)
            .background(colors.inputBackground, shape)
            .border(width = BorderWidth, color = colors.border, shape = shape)
            .padding(horizontal = spacing.space16, vertical = spacing.space8),
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (leadingContent != null) {
          leadingContent()
          Spacer(modifier = Modifier.width(spacing.space8))
        }

        Box(modifier = Modifier.weight(1f)) {
          if (value.isEmpty() && placeholder.isNotEmpty()) {
            Text(
              text = placeholder,
              style = typography.bodyLarge,
              color = colors.textMuted
            )
          }
          innerTextField()
        }

        if (trailingContent != null) {
          Spacer(modifier = Modifier.width(spacing.space8))
          trailingContent()
        }
      }
    }
  )
}

@Preview
@Composable
private fun SingleLineInputFieldPlaceholderPreview() {
  AppTheme {
    Column(modifier = Modifier.padding(AppTheme.spacingSystem.space16)) {
      SingleLineInputField(
        value = "",
        onValueChange = {},
        placeholder = "Enter your email",
        leadingContent = {
          Text(
            text = "@",
            style = AppTheme.typographySystem.bodyLarge,
            color = AppTheme.colorSystem.textMuted
          )
        },
        trailingContent = {
          Text(
            text = "✓",
            style = AppTheme.typographySystem.bodyLarge,
            color = AppTheme.colorSystem.accent
          )
        }
      )
    }
  }
}
