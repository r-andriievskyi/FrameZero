package com.frame.zero.feature.account.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.discovery.playground.shared.design_system.AppTheme
import com.discovery.playground.shared.design_system.widgets.TopToolbar
import com.frame.zero.feature.account.AccountComponent
import framezero.composeapp.features.account.generated.resources.Res
import framezero.composeapp.features.account.generated.resources.ic_bell
import framezero.composeapp.features.account.generated.resources.ic_info
import framezero.composeapp.features.account.generated.resources.ic_lock
import framezero.composeapp.features.account.generated.resources.ic_mail
import framezero.composeapp.features.account.generated.resources.ic_team
import framezero.composeapp.features.account.generated.resources.ic_user
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.systemBarsPadding

@Composable
fun AccountContent(
  component: AccountComponent,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .background(AppTheme.colorSystem.background)
      .systemBarsPadding()
  ) {
    TopToolbar(title = "Settings", onBack = component.onBack)
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = AppTheme.spacingSystem.space16)
    ) {
      Spacer(modifier = Modifier.height(AppTheme.spacingSystem.space16))
      ProfileCard(
        name = "Maya Rivera",
        role = "Director · Studio Zero",
        initials = "MR"
      )
      Spacer(modifier = Modifier.height(AppTheme.spacingSystem.space24))
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
      }
      Spacer(modifier = Modifier.height(AppTheme.spacingSystem.space24))
      SettingsSection(title = "WORKSPACE") {
        SettingsRow(
          icon = Res.drawable.ic_bell,
          title = "Notifications",
          subtitle = "All enabled"
        )
      }
      Spacer(modifier = Modifier.height(AppTheme.spacingSystem.space24))
      SettingsSection(title = "APP") {
        SettingsRow(
          icon = Res.drawable.ic_info,
          title = "About FrameZero",
          subtitle = "Version 3.0.0"
        )
      }
      Spacer(modifier = Modifier.height(AppTheme.spacingSystem.space24))
      SignOutButton(onClick = { /* TODO: wire to sign-out action */ })
      Spacer(modifier = Modifier.height(AppTheme.spacingSystem.space24))
    }
  }
}

@Preview
@Composable
private fun AccountContentPreview() {
  AppTheme(darkTheme = true) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .background(AppTheme.colorSystem.background)
    ) {
      TopToolbar(title = "Settings", onBack = {})
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = AppTheme.spacingSystem.space16)
      ) {
        Spacer(modifier = Modifier.height(AppTheme.spacingSystem.space16))
        ProfileCard(
          name = "Maya Rivera",
          role = "Director · Studio Zero",
          initials = "MR"
        )
        Spacer(modifier = Modifier.height(AppTheme.spacingSystem.space24))
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
        }
        Spacer(modifier = Modifier.height(AppTheme.spacingSystem.space24))
        SettingsSection(title = "WORKSPACE") {
          SettingsRow(
            icon = Res.drawable.ic_bell,
            title = "Notifications",
            subtitle = "All enabled"
          )
          SettingsDivider()
          SettingsRow(
            icon = Res.drawable.ic_team,
            title = "Team & permissions",
            subtitle = "12 members"
          )
        }
        Spacer(modifier = Modifier.height(AppTheme.spacingSystem.space24))
        SettingsSection(title = "APP") {
          SettingsRow(
            icon = Res.drawable.ic_info,
            title = "About FrameZero",
            subtitle = "Version 3.0.0"
          )
        }
        Spacer(modifier = Modifier.height(AppTheme.spacingSystem.space24))
        SignOutButton(onClick = {})
        Spacer(modifier = Modifier.height(AppTheme.spacingSystem.space24))
      }
    }
  }
}
