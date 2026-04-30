package com.frame.zero.feature.auth.ui

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.frame.zero.feature.auth.AuthComponent
import com.frame.zero.feature.auth.ui.register.RegisterContent
import com.frame.zero.feature.auth.ui.signin.SignInContent

@Composable
fun AuthContent(component: AuthComponent) {
  Children(stack = component.stack) { child ->
    when (val instance = child.instance) {
      is AuthComponent.Child.SignIn -> SignInContent(instance.component)
      is AuthComponent.Child.Register -> RegisterContent(instance.component)
    }
  }
}
