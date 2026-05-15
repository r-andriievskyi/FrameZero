package com.frame.zero.feature.account.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.discovery.playground.shared.design_system.AppTheme
import framezero.composeapp.features.account.generated.resources.Res
import framezero.composeapp.features.account.generated.resources.ic_bell
import framezero.composeapp.features.account.generated.resources.ic_lock
import framezero.composeapp.features.account.generated.resources.ic_mail
import framezero.composeapp.features.account.generated.resources.ic_user

private val SectionBorderWidth = 1.dp

@Composable
internal fun SettingsSection(
  title: String,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit
) {
  Column(modifier = modifier.fillMaxWidth()) {
    Text(
      text = title,
      style = AppTheme.typographySystem.labelSmall,
      color = AppTheme.colorSystem.textMuted,
      fontWeight = FontWeight.SemiBold,
      modifier = Modifier.padding(bottom = AppTheme.spacingSystem.space8)
    )
    val shape = RoundedCornerShape(AppTheme.radiusSystem.radius16)
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .clip(shape)
        .background(AppTheme.colorSystem.cardBackground, shape)
        .border(SectionBorderWidth, AppTheme.colorSystem.cardBorder, shape)
    ) {
      content()
    }
  }
}

@Preview
@Composable
private fun SettingsSectionPreview() {
  AppTheme(darkTheme = true) {
    SettingsSection(title = "ACCOUNT") {
      SettingsRow(
        icon = Res.drawable.ic_user,
        title = "Edit profile",
        subtitle = "Maya Rivera"
      )
      SettingsDivider()
      SettingsRow(
        icon = Res.drawable.ic_mail,
        title = "Email address",
        subtitle = "maya@studiozero.co"
      )
      SettingsDivider()
      SettingsRow(
        icon = Res.drawable.ic_lock,
        title = "Password & security",
        subtitle = "Last changed 3 months ago"
      )
      SettingsDivider()
      SettingsRow(
        icon = Res.drawable.ic_bell,
        title = "Notifications",
        subtitle = "All enabled"
      )
    }
  }
}
