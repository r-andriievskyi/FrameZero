package com.frame.zero.feature.account

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.getOrCreate
import kotlinx.coroutines.flow.StateFlow

class AccountComponent(
  componentContext: ComponentContext,
  val onBack: () -> Unit,
  val onEditProfile: () -> Unit,
  val onEmailSettings: () -> Unit,
  val onPasswordSecurity: () -> Unit,
  val onNotifications: () -> Unit,
  val onDeveloperOptions: () -> Unit,
  viewModelFactory: () -> AccountViewModel
) : ComponentContext by componentContext {
  private val viewModel: AccountViewModel = instanceKeeper.getOrCreate { viewModelFactory() }

  val state: StateFlow<AccountState>
    get() = viewModel.state

  fun onIntent(intent: AccountIntent) = viewModel.onIntent(intent)
}
