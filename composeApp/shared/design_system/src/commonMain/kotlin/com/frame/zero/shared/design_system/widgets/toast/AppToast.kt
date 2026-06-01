package com.frame.zero.shared.design_system.widgets.toast

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.generated.resources.Res
import com.frame.zero.shared.design_system.generated.resources.ic_circle_alert
import com.frame.zero.shared.design_system.generated.resources.ic_circle_check
import com.frame.zero.shared.design_system.generated.resources.ic_circle_x
import com.frame.zero.shared.design_system.modifier.clickableWithRipple
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import org.jetbrains.compose.resources.vectorResource

private val ToastIconSize = 20.dp
private const val BorderAlpha = 0.22f

/**
 * Soft toast — title with optional description and optional trailing action.
 *
 * Single-line:
 * ```
 * ┌─[✓]─ Scene 12 saved to cloud ────────── View ─┐
 * ```
 *
 * Two-line:
 * ```
 * ┌─[✓]─ Dailies uploaded ──────────── View ─┐
 * │      28 clips from Scene 12 …             │
 * └───────────────────────────────────────────┘
 * ```
 *
 * @param title Primary text content.
 * @param severity Controls background/text/border color selection.
 * @param description Optional secondary body text beneath the title.
 * @param actionLabel Optional text for the trailing action button.
 * @param onAction Callback when the action label is tapped.
 */
@Composable
fun AppToast(
  title: String,
  severity: ToastSeverity,
  description: String? = null,
  actionLabel: String? = null,
  onAction: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val colors = toastColors(severity)
  val radius = AppTheme.radiusSystem.radius14
  val shape = remember(radius) { RoundedCornerShape(radius) }

  val spacingSystem = AppTheme.spacingSystem
  val typographySystem = AppTheme.typographySystem

  Row(
    modifier = modifier
      .fillMaxWidth()
      .clip(shape)
      .border(
        width = AppTheme.borderSystem.hairline,
        color = colors.borderColor,
        shape = shape
      )
      .background(colors.surface)
      .padding(horizontal = spacingSystem.space16, vertical = spacingSystem.space12),
    verticalAlignment = if (description != null) Alignment.Top else Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(spacingSystem.space12)
  ) {
    Icon(
      imageVector = vectorResource(
        when (severity) {
          ToastSeverity.Success -> Res.drawable.ic_circle_check
          ToastSeverity.Warning -> Res.drawable.ic_circle_alert
          ToastSeverity.Error -> Res.drawable.ic_circle_x
        }
      ),
      contentDescription = null,
      tint = colors.text,
      modifier = Modifier.size(ToastIconSize)
    )
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        style = typographySystem.titleMedium,
        color = colors.text
      )
      if (description != null) {
        VerticalSpacer(spacingSystem.space2)
        Text(
          text = description,
          style = typographySystem.bodySmall,
          color = colors.text
        )
      }
    }
    if (actionLabel != null && onAction != null) {
      val actionRadius = AppTheme.radiusSystem.radius8
      val actionShape = remember(actionRadius) { RoundedCornerShape(actionRadius) }
      Text(
        text = actionLabel,
        style = typographySystem.labelMedium,
        color = colors.text,
        modifier = Modifier
          .align(Alignment.CenterVertically)
          .clip(actionShape)
          .clickableWithRipple(color = colors.text) { onAction() }
          .padding(spacingSystem.space4)
      )
    }
  }
}

private data class ToastColorSet(
  val surface: Color,
  val text: Color,
  val borderColor: Color
)

@Composable
private fun toastColors(severity: ToastSeverity): ToastColorSet {
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

@LightDarkPreview
@Composable
private fun AppToastCompactPreview() {
  AppTheme {
    Column(
      modifier = Modifier.background(AppTheme.colorSystem.background).padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(AppTheme.spacingSystem.space8)
    ) {
      AppToast(
        title = "Scene 12 saved to cloud",
        severity = ToastSeverity.Success
      )
      AppToast(
        title = "3 shots are missing a takes list",
        severity = ToastSeverity.Warning,
        actionLabel = "Fix",
        onAction = {}
      )
      AppToast(
        title = "Upload failed — check connection",
        severity = ToastSeverity.Error,
        actionLabel = "Retry",
        onAction = {}
      )
    }
  }
}

@LightDarkPreview
@Composable
private fun AppToastTwoLinePreview() {
  AppTheme {
    Column(
      modifier = Modifier.background(AppTheme.colorSystem.background).padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(AppTheme.spacingSystem.space8)
    ) {
      AppToast(
        title = "Dailies uploaded",
        description = "28 clips from Scene 12 are now available to the editorial team.",
        severity = ToastSeverity.Success,
        actionLabel = "View",
        onAction = {}
      )
      AppToast(
        title = "Call sheet not sent",
        description = "Tomorrow's call sheet hasn't been distributed to crew. Send before 9pm.",
        severity = ToastSeverity.Warning,
        actionLabel = "Send",
        onAction = {}
      )
      AppToast(
        title = "Sync failed",
        description = "Couldn't connect to Frame.io. Your changes are saved locally and will retry.",
        severity = ToastSeverity.Error,
        actionLabel = "Retry",
        onAction = {}
      )
    }
  }
}

