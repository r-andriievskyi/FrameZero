package com.frame.zero

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.arkivanov.decompose.defaultComponentContext
import com.frame.zero.feature.RootComponent
import com.frame.zero.feature.auth.AuthViewModel

class MainActivity : ComponentActivity() {
  private val root by lazy {
    RootComponent(
      componentContext = defaultComponentContext(),
      authViewModelFactory = { application.get<AuthViewModel>() },
    )
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)
    setContent { App(root) }
  }
}
