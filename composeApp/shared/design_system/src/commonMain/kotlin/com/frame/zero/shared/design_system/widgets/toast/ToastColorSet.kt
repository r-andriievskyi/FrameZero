package com.frame.zero.shared.design_system.widgets.toast

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.frame.zero.shared.design_system.AppTheme

private const val BorderAlpha = 0.22f

internal data class ToastColorSet(
  val surface: Color,
  val text: Color,
  val borderColor: Color
)

@Composable
internal fun toastColors(severity: ToastSeverity): ToastColorSet {
  val colorSystem = AppTheme.colorSystem
  return when (severity) {
    ToastSeverity.Success -> ToastColorSet(
      surface = colorSystem.successSurface,
      text = colorSystem.successText,
      borderColor = colorSystem.successText.copy(alpha = BorderAlpha)
    )

    ToastSeverity.Warning -> ToastColorSet(
      surface = colorSystem.warningSurface,
      text = colorSystem.warningText,
      borderColor = colorSystem.warningText.copy(alpha = BorderAlpha)
    )

    ToastSeverity.Error -> ToastColorSet(
      surface = colorSystem.errorSurface,
      text = colorSystem.errorText,
      borderColor = colorSystem.errorText.copy(alpha = BorderAlpha)
    )
  }
}
