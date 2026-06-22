package com.frame.zero.feature.account.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.frame.zero.core.security.BiometricPromptText
import com.frame.zero.feature.account.AccountComponent
import com.frame.zero.feature.account.AccountState
import com.frame.zero.feature.account.ui.components.SettingsRow
import com.frame.zero.feature.account.ui.components.SettingsSection
import com.frame.zero.feature.account.ui.components.SettingsToggleRow
import com.frame.zero.feature.account.ui.components.SignOutButton
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.widgets.TopToolbar
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import framezero.composeapp.features.account.generated.resources.Res
import framezero.composeapp.features.account.generated.resources.account_toolbar_title
import framezero.composeapp.features.account.generated.resources.app_lock_prompt_cancel
import framezero.composeapp.features.account.generated.resources.app_lock_prompt_subtitle
import framezero.composeapp.features.account.generated.resources.app_lock_prompt_title
import framezero.composeapp.features.account.generated.resources.ic_bell
import framezero.composeapp.features.account.generated.resources.ic_lock
import framezero.composeapp.features.account.generated.resources.ic_mail
import framezero.composeapp.features.account.generated.resources.ic_user
import framezero.composeapp.features.account.generated.resources.section_account
import framezero.composeapp.features.account.generated.resources.section_security
import framezero.composeapp.features.account.generated.resources.section_workspace
import framezero.composeapp.features.account.generated.resources.settings_app_lock
import framezero.composeapp.features.account.generated.resources.settings_app_lock_subtitle
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
  val state by component.state.collectAsStateWithLifecycle()
  AccountContent(
    state = state,
    onBack = component.onBack,
    onEditProfileClick = component::onEditProfileClick,
    onEmailClick = component::onEmailClick,
    onPasswordSecurityClick = component::onPasswordSecurityClick,
    onNotificationsClick = component::onNotificationsClick,
    onAppLockToggle = component::onAppLockToggle,
    onSignOutClick = component::onSignOutClick,
    modifier = modifier
  )
}

@Composable
internal fun AccountContent(
  state: AccountState,
  onBack: () -> Unit,
  onEditProfileClick: () -> Unit,
  onEmailClick: () -> Unit,
  onPasswordSecurityClick: () -> Unit,
  onNotificationsClick: () -> Unit,
  onAppLockToggle: (Boolean, BiometricPromptText) -> Unit,
  onSignOutClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val spacingSystem = AppTheme.spacingSystem
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
        .padding(horizontal = spacingSystem.space16)
    ) {
      VerticalSpacer(spacingSystem.space16)
      SettingsSection(title = stringResource(Res.string.section_account)) {
        state.userName?.let { userName ->
          SettingsRow(
            icon = Res.drawable.ic_user,
            title = stringResource(Res.string.settings_edit_profile),
            subtitle = userName,
            onClick = onEditProfileClick
          )
          SettingsDivider()
        }
        state.email?.let { email ->
          SettingsRow(
            icon = Res.drawable.ic_mail,
            title = stringResource(Res.string.settings_email_address),
            subtitle = email,
            onClick = onEmailClick
          )
          SettingsDivider()
        }
        SettingsRow(
          icon = Res.drawable.ic_lock,
          title = stringResource(Res.string.settings_password_security),
          subtitle = stringResource(Res.string.settings_password_last_changed),
          onClick = onPasswordSecurityClick
        )
      }
      VerticalSpacer(spacingSystem.space24)
      SettingsSection(title = stringResource(Res.string.section_workspace)) {
        SettingsRow(
          icon = Res.drawable.ic_bell,
          title = stringResource(Res.string.settings_notifications),
          subtitle = stringResource(Res.string.settings_notifications_subtitle),
          onClick = onNotificationsClick
        )
      }
      if (state.appLockSupported) {
        val promptTitle = stringResource(Res.string.app_lock_prompt_title)
        val promptSubtitle = stringResource(Res.string.app_lock_prompt_subtitle)
        val promptCancel = stringResource(Res.string.app_lock_prompt_cancel)
        val appLockPrompt = remember(promptTitle, promptSubtitle, promptCancel) {
          BiometricPromptText(title = promptTitle, subtitle = promptSubtitle, negativeButton = promptCancel)
        }
        VerticalSpacer(spacingSystem.space24)
        SettingsSection(title = stringResource(Res.string.section_security)) {
          SettingsToggleRow(
            icon = Res.drawable.ic_lock,
            title = stringResource(Res.string.settings_app_lock),
            subtitle = stringResource(Res.string.settings_app_lock_subtitle),
            checked = state.appLockEnabled,
            onCheckedChange = { enabled -> onAppLockToggle(enabled, appLockPrompt) }
          )
        }
      }
      VerticalSpacer(spacingSystem.space24)
      SignOutButton(onClick = onSignOutClick)
      VerticalSpacer(spacingSystem.space24)
    }
  }
}

@Composable
private fun SettingsDivider(modifier: Modifier = Modifier) {
  HorizontalDivider(
    modifier = modifier,
    color = AppTheme.colorSystem.border
  )
}

@LightDarkPreview
@Composable
private fun AccountContentPreview() {
  AppTheme {
    AccountContent(
      state = AccountState(
        userName = "Maya Rivera",
        email = "maya@studiozero.co",
        appLockSupported = true,
        appLockEnabled = true
      ),
      onBack = {},
      onEditProfileClick = {},
      onEmailClick = {},
      onPasswordSecurityClick = {},
      onNotificationsClick = {},
      onAppLockToggle = { _, _ -> },
      onSignOutClick = {}
    )
  }
}
