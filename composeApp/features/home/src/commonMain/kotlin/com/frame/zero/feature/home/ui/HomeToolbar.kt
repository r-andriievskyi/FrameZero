package com.frame.zero.feature.home.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.tooling.preview.Preview
import com.discovery.playground.shared.design_system.AppTheme
import com.discovery.playground.shared.design_system.widgets.HorizontalSpacer
import framezero.composeapp.features.home.generated.resources.Res
import framezero.composeapp.features.home.generated.resources.ic_bell
import framezero.composeapp.features.home.generated.resources.ic_logo
import framezero.composeapp.features.home.generated.resources.ic_user
import framezero.composeapp.features.home.generated.resources.toolbar_logo_cd
import framezero.composeapp.features.home.generated.resources.toolbar_notifications_cd
import framezero.composeapp.features.home.generated.resources.toolbar_settings_cd
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomeToolbar(
  onNotificationsClick: () -> Unit,
  onAccountClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier.fillMaxWidth().padding(
      horizontal = AppTheme.spacingSystem.space16,
      vertical = AppTheme.spacingSystem.space8
    ),
    verticalAlignment = Alignment.CenterVertically
  ) {
    val buttonsShape = RoundedCornerShape(AppTheme.spacingSystem.space8)
    Image(
      painter = painterResource(Res.drawable.ic_logo),
      contentDescription = stringResource(Res.string.toolbar_logo_cd),
      colorFilter = ColorFilter.tint(AppTheme.colorSystem.accent)
    )
    Spacer(modifier = Modifier.weight(1f))
    Image(
      painter = painterResource(Res.drawable.ic_bell),
      contentDescription = stringResource(Res.string.toolbar_notifications_cd),
      modifier = Modifier.clip(buttonsShape).clickable(onClick = onNotificationsClick)
        .padding(AppTheme.spacingSystem.space8),
      colorFilter = ColorFilter.tint(AppTheme.colorSystem.textPrimary)
    )
    HorizontalSpacer(AppTheme.spacingSystem.space8)
    Image(
      painter = painterResource(Res.drawable.ic_user),
      contentDescription = stringResource(Res.string.toolbar_settings_cd),
      modifier = Modifier.background(
        color = AppTheme.colorSystem.accent,
        shape = buttonsShape
      ).clickable(onClick = onAccountClick).padding(AppTheme.spacingSystem.space8),
      colorFilter = ColorFilter.tint(AppTheme.colorSystem.textPrimary)
    )
  }
}

@Preview
@Composable
private fun HomeToolbarPreview() {
  AppTheme(darkTheme = true) {
    HomeToolbar(onNotificationsClick = {}, onAccountClick = {})
  }
}
