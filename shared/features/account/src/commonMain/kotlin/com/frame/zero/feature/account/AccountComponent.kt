package com.frame.zero.feature.account

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.getOrCreate
import kotlinx.coroutines.flow.StateFlow

class AccountComponent(
  componentContext: ComponentContext,
  val onBack: () -> Unit,
  viewModelFactory: () -> AccountViewModel
) : ComponentContext by componentContext {
  private val viewModel: AccountViewModel = instanceKeeper.getOrCreate { viewModelFactory() }

  val state: StateFlow<AccountState>
    get() = viewModel.state

  fun onEditProfileClick() {
    // TODO: navigate to edit profile
  }

  fun onEmailClick() {
    // TODO: navigate to email settings
  }

  fun onPasswordSecurityClick() {
    // TODO: navigate to password & security
  }

  fun onNotificationsClick() {
    // TODO: navigate to notification settings
  }

  fun onAboutClick() {
    // TODO: navigate to about
  }

  fun onSignOutClick() {
    viewModel.signOut()
  }
}
