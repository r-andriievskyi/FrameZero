package com.frame.zero.feature.account.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.generated.resources.ic_chevron_right
import com.frame.zero.shared.design_system.modifier.clickableWithRipple
import com.frame.zero.shared.design_system.widgets.HorizontalSpacer
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import framezero.composeapp.features.account.generated.resources.Res
import framezero.composeapp.features.account.generated.resources.ic_user
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import com.frame.zero.shared.design_system.generated.resources.Res as DesignSystemRes

private val IconContainerSize = 40.dp

@Composable
internal fun SettingsRow(
  icon: DrawableResource,
  title: String,
  subtitle: String,
  modifier: Modifier = Modifier,
  onClick: () -> Unit = {}
) {
  val colorSystem = AppTheme.colorSystem
  val spacingSystem = AppTheme.spacingSystem
  val typographySystem = AppTheme.typographySystem
  Row(
    modifier = modifier
      .fillMaxWidth()
      .clickableWithRipple(
        color = colorSystem.accentDim,
        bounded = true,
        role = Role.Button,
        onClick = onClick
      )
      .semantics(mergeDescendants = true) {}
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
      VerticalSpacer(spacingSystem.space2)
      Text(
        text = subtitle,
        style = typographySystem.bodySmall,
        color = colorSystem.textMuted
      )
    }
    Image(
      painter = painterResource(DesignSystemRes.drawable.ic_chevron_right),
      contentDescription = null,
      colorFilter = ColorFilter.tint(colorSystem.textMuted)
    )
  }
}

@LightDarkPreview
@Composable
private fun SettingsRowPreview() {
  AppTheme {
    SettingsRow(
      icon = Res.drawable.ic_user,
      title = "Edit profile",
      subtitle = "Maya Rivera"
    )
  }
}
