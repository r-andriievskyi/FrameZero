package com.frame.zero.feature.auth

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.value.Value
import com.frame.zero.feature.auth.register.RegisterComponent
import com.frame.zero.feature.auth.register.RegisterViewModel
import com.frame.zero.feature.auth.signin.SignInComponent
import com.frame.zero.feature.auth.signin.SignInViewModel

class AuthComponent(
  componentContext: ComponentContext,
  private val signInViewModelFactory: () -> SignInViewModel,
  private val registerViewModelFactory: () -> RegisterViewModel,
) : ComponentContext by componentContext {

  private val navigation = StackNavigation<Config>()

  val stack: Value<ChildStack<Config, Child>> =
    childStack(
      source = navigation,
      serializer = null,
      initialConfiguration = Config.SignIn,
      handleBackButton = true,
      childFactory = ::createChild,
    )

  private fun createChild(config: Config, context: ComponentContext): Child =
    when (config) {
      Config.SignIn ->
        Child.SignIn(
          SignInComponent(
            componentContext = context,
            onNavigateToRegister = { navigation.bringToFront(Config.Register) },
            viewModelFactory = signInViewModelFactory,
          ))
      Config.Register ->
        Child.Register(
          RegisterComponent(
            componentContext = context,
            onNavigateToSignIn = { navigation.pop() },
            viewModelFactory = registerViewModelFactory,
          ))
    }

  sealed interface Config {
    data object SignIn : Config

    data object Register : Config
  }

  sealed interface Child {
    data class SignIn(val component: SignInComponent) : Child

    data class Register(val component: RegisterComponent) : Child
  }
}
