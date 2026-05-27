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
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.widgets.TopToolbar
import com.frame.zero.feature.account.AccountComponent
import framezero.composeapp.features.account.generated.resources.Res
import framezero.composeapp.features.account.generated.resources.ic_bell
import framezero.composeapp.features.account.generated.resources.ic_info
import framezero.composeapp.features.account.generated.resources.ic_lock
import framezero.composeapp.features.account.generated.resources.ic_mail
import framezero.composeapp.features.account.generated.resources.ic_team
import framezero.composeapp.features.account.generated.resources.ic_user
import framezero.composeapp.features.account.generated.resources.account_toolbar_title
import framezero.composeapp.features.account.generated.resources.section_account
import framezero.composeapp.features.account.generated.resources.section_app
import framezero.composeapp.features.account.generated.resources.section_workspace
import framezero.composeapp.features.account.generated.resources.settings_about
import framezero.composeapp.features.account.generated.resources.settings_edit_profile
import framezero.composeapp.features.account.generated.resources.settings_email_address
import framezero.composeapp.features.account.generated.resources.settings_notifications
import framezero.composeapp.features.account.generated.resources.settings_notifications_subtitle
import framezero.composeapp.features.account.generated.resources.settings_password_last_changed
import framezero.composeapp.features.account.generated.resources.settings_password_security
import framezero.composeapp.features.account.generated.resources.settings_team_members_count
import framezero.composeapp.features.account.generated.resources.settings_team_permissions
import framezero.composeapp.features.account.generated.resources.settings_version
import org.jetbrains.compose.resources.stringResource
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
    TopToolbar(title = stringResource(Res.string.account_toolbar_title), onBack = component.onBack)
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
      SettingsSection(title = stringResource(Res.string.section_account)) {
        SettingsRow(
          icon = Res.drawable.ic_user,
          title = stringResource(Res.string.settings_edit_profile),
          subtitle = "Maya Rivera"
        )
        SettingsDivider()
        SettingsRow(
          icon = Res.drawable.ic_mail,
          title = stringResource(Res.string.settings_email_address),
          subtitle = "maya@studiozero.co"
        )
        SettingsDivider()
        SettingsRow(
          icon = Res.drawable.ic_lock,
          title = stringResource(Res.string.settings_password_security),
          subtitle = stringResource(Res.string.settings_password_last_changed)
        )
      }
      Spacer(modifier = Modifier.height(AppTheme.spacingSystem.space24))
      SettingsSection(title = stringResource(Res.string.section_workspace)) {
        SettingsRow(
          icon = Res.drawable.ic_bell,
          title = stringResource(Res.string.settings_notifications),
          subtitle = stringResource(Res.string.settings_notifications_subtitle)
        )
      }
      Spacer(modifier = Modifier.height(AppTheme.spacingSystem.space24))
      SettingsSection(title = stringResource(Res.string.section_app)) {
        SettingsRow(
          icon = Res.drawable.ic_info,
          title = stringResource(Res.string.settings_about),
          subtitle = stringResource(Res.string.settings_version, "3.0.0")
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
      TopToolbar(title = stringResource(Res.string.account_toolbar_title), onBack = {})
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
        SettingsSection(title = stringResource(Res.string.section_account)) {
          SettingsRow(
            icon = Res.drawable.ic_user,
            title = stringResource(Res.string.settings_edit_profile),
            subtitle = "Maya Rivera"
          )
          SettingsDivider()
          SettingsRow(
            icon = Res.drawable.ic_mail,
            title = stringResource(Res.string.settings_email_address),
            subtitle = "maya@studiozero.co"
          )
          SettingsDivider()
          SettingsRow(
            icon = Res.drawable.ic_lock,
            title = stringResource(Res.string.settings_password_security),
            subtitle = stringResource(Res.string.settings_password_last_changed)
          )
        }
        Spacer(modifier = Modifier.height(AppTheme.spacingSystem.space24))
        SettingsSection(title = stringResource(Res.string.section_workspace)) {
          SettingsRow(
            icon = Res.drawable.ic_bell,
            title = stringResource(Res.string.settings_notifications),
            subtitle = stringResource(Res.string.settings_notifications_subtitle)
          )
          SettingsDivider()
          SettingsRow(
            icon = Res.drawable.ic_team,
            title = stringResource(Res.string.settings_team_permissions),
            subtitle = stringResource(Res.string.settings_team_members_count, 12)
          )
        }
        Spacer(modifier = Modifier.height(AppTheme.spacingSystem.space24))
        SettingsSection(title = stringResource(Res.string.section_app)) {
          SettingsRow(
            icon = Res.drawable.ic_info,
            title = stringResource(Res.string.settings_about),
            subtitle = stringResource(Res.string.settings_version, "3.0.0")
          )
        }
        Spacer(modifier = Modifier.height(AppTheme.spacingSystem.space24))
        SignOutButton(onClick = {})
        Spacer(modifier = Modifier.height(AppTheme.spacingSystem.space24))
      }
    }
  }
}
