package com.frame.zero.feature.auth.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.frame.zero.feature.auth.AuthComponent
import com.frame.zero.feature.auth.ui.register.RegisterScreen
import com.frame.zero.feature.auth.ui.signin.SignInScreen

@Composable
fun AuthScreen(component: AuthComponent, modifier: Modifier = Modifier) {
  Children(stack = component.stack, modifier = modifier) { child ->
    when (val instance = child.instance) {
      is AuthComponent.Child.SignIn -> SignInScreen(instance.component)
      is AuthComponent.Child.Register -> RegisterScreen(instance.component)
    }
  }
}
