package com.frame.zero.feature.app_update

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import framezero.composeapp.generated.resources.Res
import framezero.composeapp.generated.resources.ic_app_logo
import framezero.composeapp.generated.resources.ic_update_available
import framezero.composeapp.generated.resources.ic_update_warning
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

private val LogoContainerSize = 80.dp
private val LogoSize = LogoContainerSize / 2
private val BadgeSize = 36.dp
private val BadgeOverhang = BadgeSize / 3

@Composable
internal fun UpdateLogoBadge(
  badgeIcon: DrawableResource,
  badgeContainerColor: Color,
  badgeIconTint: Color,
  modifier: Modifier = Modifier
) {
  val colorSystem = AppTheme.colorSystem
  val cardShape = RoundedCornerShape(AppTheme.radiusSystem.radius16)

  Box(modifier = modifier) {
    Box(
      modifier = Modifier
        .padding(end = BadgeOverhang, bottom = BadgeOverhang)
        .size(LogoContainerSize)
        .clip(cardShape)
        .background(colorSystem.cardBackground)
        .border(AppTheme.borderSystem.hairline, colorSystem.border, cardShape),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        painter = painterResource(Res.drawable.ic_app_logo),
        contentDescription = null,
        tint = colorSystem.textPrimary,
        modifier = Modifier.size(LogoSize)
      )
    }
    Box(
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .size(BadgeSize)
        .clip(CircleShape)
        .background(badgeContainerColor),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        painter = painterResource(badgeIcon),
        contentDescription = null,
        tint = badgeIconTint,
        modifier = Modifier.padding(AppTheme.spacingSystem.space8)
      )
    }
  }
}

@LightDarkPreview
@Composable
private fun UpdateLogoBadgePreview() {
  AppTheme {
    Box(modifier = Modifier.padding(AppTheme.spacingSystem.space16)) {
      UpdateLogoBadge(
        badgeIcon = Res.drawable.ic_update_available,
        badgeContainerColor = AppTheme.colorSystem.accentSurface,
        badgeIconTint = AppTheme.colorSystem.accentText
      )
    }
  }
}

@LightDarkPreview
@Composable
private fun UpdateLogoBadgeWarningPreview() {
  AppTheme {
    Box(modifier = Modifier.padding(AppTheme.spacingSystem.space16)) {
      UpdateLogoBadge(
        badgeIcon = Res.drawable.ic_update_warning,
        badgeContainerColor = AppTheme.colorSystem.warningSurface,
        badgeIconTint = AppTheme.colorSystem.warningText
      )
    }
  }
}
