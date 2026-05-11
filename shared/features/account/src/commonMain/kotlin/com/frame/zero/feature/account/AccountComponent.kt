package com.frame.zero.feature.account

import com.arkivanov.decompose.ComponentContext

class AccountComponent(
  componentContext: ComponentContext,
  val onBack: () -> Unit
) : ComponentContext by componentContext
