package com.frame.zero.feature.account.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.widgets.HorizontalSpacer
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import framezero.composeapp.features.account.generated.resources.Res
import framezero.composeapp.features.account.generated.resources.ic_lock
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

private val IconContainerSize = 40.dp

/**
 * Settings row with a trailing [Switch] instead of a navigation chevron. The whole row is
 * inert except the switch — toggling is the only action.
 */
@Composable
internal fun SettingsToggleRow(
  icon: DrawableResource,
  title: String,
  subtitle: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier
) {
  val colorSystem = AppTheme.colorSystem
  val spacingSystem = AppTheme.spacingSystem
  val typographySystem = AppTheme.typographySystem
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(
        horizontal = spacingSystem.space16,
        vertical = spacingSystem.space16
      ),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(IconContainerSize)
        .clip(RoundedCornerShape(AppTheme.radiusSystem.radius8))
        .background(colorSystem.inputBackground),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        painter = painterResource(icon),
        contentDescription = null,
        tint = colorSystem.accent
      )
    }
    HorizontalSpacer(spacingSystem.space16)
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        style = typographySystem.bodyMedium,
        color = colorSystem.textPrimary,
        fontWeight = FontWeight.Medium
      )
      VerticalSpacer(spacingSystem.space4)
      Text(
        text = subtitle,
        style = typographySystem.bodySmall,
        color = colorSystem.textMuted
      )
    }
    HorizontalSpacer(spacingSystem.space12)
    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      colors = SwitchDefaults.colors(
        checkedThumbColor = colorSystem.textOnAccent,
        checkedTrackColor = colorSystem.accent,
        uncheckedThumbColor = colorSystem.textMuted,
        uncheckedTrackColor = colorSystem.inputBackground,
        uncheckedBorderColor = colorSystem.border
      )
    )
  }
}

@LightDarkPreview
@Composable
private fun SettingsToggleRowPreview() {
  AppTheme {
    SettingsToggleRow(
      icon = Res.drawable.ic_lock,
      title = "Biometric app lock",
      subtitle = "Require Face ID / fingerprint to open the app",
      checked = true,
      onCheckedChange = {}
    )
  }
}
