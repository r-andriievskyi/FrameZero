package com.frame.zero.feature.home.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import com.discovery.playground.shared.design_system.AppTheme
import framezero.composeapp.features.home.generated.resources.Res
import framezero.composeapp.features.home.generated.resources.ic_logo
import org.jetbrains.compose.resources.painterResource

@Composable
fun HomeToolbar(
  onNotificationsClick: () -> Unit,
  onSettingsClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .padding(
          horizontal = AppTheme.spacingSystem.space16,
          vertical = AppTheme.spacingSystem.space8,
        ),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Image(
      painter = painterResource(Res.drawable.ic_logo),
      contentDescription = null,
      colorFilter = ColorFilter.tint(AppTheme.colorSystem.accent),
    )
    Spacer(modifier = Modifier.weight(1f))
    /*
    Image(
      painter = painterResource(Res.drawable.ic_bell),
      contentDescription = "Notifications",
      modifier =
        Modifier.clip(CircleShape)
          .clickable(onClick = onNotificationsClick)
          .padding(AppTheme.spacingSystem.space8)
          .size(AppTheme.spacingSystem.space24),
      colorFilter = ColorFilter.tint(AppTheme.colorSystem.textPrimary),
    )
    Image(
      painter = painterResource(Res.drawable.ic_person),
      contentDescription = "Settings",
      modifier =
        Modifier.clip(CircleShape)
          .clickable(onClick = onSettingsClick)
          .padding(AppTheme.spacingSystem.space8)
          .size(AppTheme.spacingSystem.space24),
      colorFilter = ColorFilter.tint(AppTheme.colorSystem.textPrimary),
    )
     */
  }
}
