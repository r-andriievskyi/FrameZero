package com.frame.zero.feature.account.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.frame.zero.feature.account.AccountComponent
import com.frame.zero.feature.account.AccountState
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.widgets.TopToolbar
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import framezero.composeapp.features.account.generated.resources.Res
import framezero.composeapp.features.account.generated.resources.account_toolbar_title
import framezero.composeapp.features.account.generated.resources.ic_bell
import framezero.composeapp.features.account.generated.resources.ic_lock
import framezero.composeapp.features.account.generated.resources.ic_mail
import framezero.composeapp.features.account.generated.resources.ic_user
import framezero.composeapp.features.account.generated.resources.section_account
import framezero.composeapp.features.account.generated.resources.section_workspace
import framezero.composeapp.features.account.generated.resources.settings_edit_profile
import framezero.composeapp.features.account.generated.resources.settings_email_address
import framezero.composeapp.features.account.generated.resources.settings_notifications
import framezero.composeapp.features.account.generated.resources.settings_notifications_subtitle
import framezero.composeapp.features.account.generated.resources.settings_password_last_changed
import framezero.composeapp.features.account.generated.resources.settings_password_security
import org.jetbrains.compose.resources.stringResource

@Composable
fun AccountScreen(
  component: AccountComponent,
  modifier: Modifier = Modifier
) {
  val state by component.state.collectAsState()
  AccountContent(
    state = state,
    onBack = component.onBack,
    onEditProfileClick = component::onEditProfileClick,
    onEmailClick = component::onEmailClick,
    onPasswordSecurityClick = component::onPasswordSecurityClick,
    onNotificationsClick = component::onNotificationsClick,
    onSignOutClick = component::onSignOutClick,
    modifier = modifier
  )
}

@Composable
fun AccountContent(
  state: AccountState,
  onBack: () -> Unit,
  onEditProfileClick: () -> Unit,
  onEmailClick: () -> Unit,
  onPasswordSecurityClick: () -> Unit,
  onNotificationsClick: () -> Unit,
  onSignOutClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .background(AppTheme.colorSystem.background)
      .systemBarsPadding()
  ) {
    TopToolbar(title = stringResource(Res.string.account_toolbar_title), onBack = onBack)
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = AppTheme.spacingSystem.space16)
    ) {
      VerticalSpacer(AppTheme.spacingSystem.space16)
      SettingsSection(title = stringResource(Res.string.section_account)) {
        SettingsRow(
          icon = Res.drawable.ic_user,
          title = stringResource(Res.string.settings_edit_profile),
          subtitle = state.userName,
          onClick = onEditProfileClick
        )
        SettingsDivider()
        SettingsRow(
          icon = Res.drawable.ic_mail,
          title = stringResource(Res.string.settings_email_address),
          subtitle = state.email,
          onClick = onEmailClick
        )
        SettingsDivider()
        SettingsRow(
          icon = Res.drawable.ic_lock,
          title = stringResource(Res.string.settings_password_security),
          subtitle = stringResource(Res.string.settings_password_last_changed),
          onClick = onPasswordSecurityClick
        )
      }
      VerticalSpacer(AppTheme.spacingSystem.space24)
      SettingsSection(title = stringResource(Res.string.section_workspace)) {
        SettingsRow(
          icon = Res.drawable.ic_bell,
          title = stringResource(Res.string.settings_notifications),
          subtitle = stringResource(Res.string.settings_notifications_subtitle),
          onClick = onNotificationsClick
        )
      }
      VerticalSpacer(AppTheme.spacingSystem.space24)
      SignOutButton(onClick = onSignOutClick)
      VerticalSpacer(AppTheme.spacingSystem.space24)
    }
  }
}

@LightDarkPreview
@Composable
private fun AccountContentPreview() {
  AppTheme {
    AccountContent(
      state = AccountState(
        userName = "Maya Rivera",
        email = "maya@studiozero.co"
      ),
      onBack = {},
      onEditProfileClick = {},
      onEmailClick = {},
      onPasswordSecurityClick = {},
      onNotificationsClick = {},
      onSignOutClick = {}
    )
  }
}
