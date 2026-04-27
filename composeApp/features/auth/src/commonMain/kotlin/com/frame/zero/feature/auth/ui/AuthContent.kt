package com.frame.zero.feature.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.frame.zero.feature.auth.AuthComponent
import com.frame.zero.feature.auth.AuthIntent
import com.frame.zero.feature.auth.AuthMode

@Composable
fun AuthContent(component: AuthComponent) {
  val state by component.state.collectAsState()
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }

  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Text(text = "FrameZero", style = MaterialTheme.typography.headlineLarge)
      Spacer(Modifier.height(8.dp))

      val tabIndex = if (state.mode == AuthMode.Login) 0 else 1
      PrimaryTabRow(selectedTabIndex = tabIndex, modifier = Modifier.fillMaxWidth()) {
        Tab(
          selected = tabIndex == 0,
          onClick = { if (state.mode != AuthMode.Login) component.onIntent(AuthIntent.SwitchMode) },
          text = { Text("Log in") },
        )
        Tab(
          selected = tabIndex == 1,
          onClick = {
            if (state.mode != AuthMode.Register) component.onIntent(AuthIntent.SwitchMode)
          },
          text = { Text("Register") },
        )
      }

      OutlinedTextField(
        value = email,
        onValueChange = { email = it },
        label = { Text("Email") },
        singleLine = true,
        enabled = !state.isLoading,
        modifier = Modifier.fillMaxWidth(),
      )

      OutlinedTextField(
        value = password,
        onValueChange = { password = it },
        label = { Text("Password") },
        singleLine = true,
        enabled = !state.isLoading,
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth(),
      )

      state.error?.let { error ->
        Text(
          text = error,
          color = MaterialTheme.colorScheme.error,
          style = MaterialTheme.typography.bodySmall,
        )
      }

      Button(
        onClick = {
          val intent =
            when (state.mode) {
              AuthMode.Login -> AuthIntent.Login(email.trim(), password)
              AuthMode.Register -> AuthIntent.Register(email.trim(), password)
            }
          component.onIntent(intent)
        },
        enabled = !state.isLoading,
        modifier = Modifier.fillMaxWidth(),
      ) {
        if (state.isLoading) {
          CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        } else {
          Text(if (state.mode == AuthMode.Login) "Log in" else "Register")
        }
      }
    }
  }
}
