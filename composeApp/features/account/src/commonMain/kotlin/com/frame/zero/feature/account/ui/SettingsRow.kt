package com.frame.zero.feature.account.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.generated.resources.ic_chevron_right
import com.frame.zero.shared.design_system.modifier.clickableWithRipple
import framezero.composeapp.features.account.generated.resources.Res
import framezero.composeapp.features.account.generated.resources.ic_user
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import com.frame.zero.shared.design_system.generated.resources.Res as DesignSystemRes

private val IconContainerSize = 40.dp
private val IconSize = 20.dp
private val ChevronSize = 20.dp

@Composable
internal fun SettingsRow(
  icon: DrawableResource,
  title: String,
  subtitle: String,
  modifier: Modifier = Modifier,
  onClick: () -> Unit = {}
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .clickableWithRipple(
        color = AppTheme.colorSystem.accentDim,
        bounded = true,
        onClick = onClick
      )
      .padding(
        horizontal = AppTheme.spacingSystem.space16,
        vertical = AppTheme.spacingSystem.space16
      ),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(IconContainerSize)
        .clip(RoundedCornerShape(AppTheme.radiusSystem.radius8))
        .background(AppTheme.colorSystem.inputBackground),
      contentAlignment = Alignment.Center
    ) {
      Image(
        painter = painterResource(icon),
        contentDescription = title,
        colorFilter = ColorFilter.tint(AppTheme.colorSystem.accent),
        modifier = Modifier.size(IconSize)
      )
    }
    Spacer(modifier = Modifier.width(AppTheme.spacingSystem.space16))
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        style = AppTheme.typographySystem.bodyMedium,
        color = AppTheme.colorSystem.textPrimary,
        fontWeight = FontWeight.Medium
      )
      Spacer(modifier = Modifier.height(AppTheme.spacingSystem.space2))
      Text(
        text = subtitle,
        style = AppTheme.typographySystem.bodySmall,
        color = AppTheme.colorSystem.textMuted
      )
    }
    Image(
      painter = painterResource(DesignSystemRes.drawable.ic_chevron_right),
      contentDescription = null,
      colorFilter = ColorFilter.tint(AppTheme.colorSystem.textMuted),
      modifier = Modifier.size(ChevronSize)
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
