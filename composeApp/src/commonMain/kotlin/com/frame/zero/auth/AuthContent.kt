package com.frame.zero.auth

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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.frame.zero.feature.auth.AuthComponent
import com.frame.zero.feature.auth.AuthIntent

@Composable
fun AuthContent(component: AuthComponent) {
  val state by component.state.collectAsState()

  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Text(text = "FrameZero", style = MaterialTheme.typography.headlineLarge)
      Spacer(Modifier.height(8.dp))

      OutlinedTextField(
        value = state.email,
        onValueChange = { component.onIntent(AuthIntent.EmailChanged(it)) },
        label = { Text("Email") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
      )

      OutlinedTextField(
        value = state.password,
        onValueChange = { component.onIntent(AuthIntent.PasswordChanged(it)) },
        label = { Text("Password") },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth(),
      )

      if (state.error != null) {
        Text(
          text = state.error!!,
          color = MaterialTheme.colorScheme.error,
          style = MaterialTheme.typography.bodySmall,
        )
      }

      Button(
        onClick = { component.onIntent(AuthIntent.LoginClicked) },
        enabled = !state.isLoading,
        modifier = Modifier.fillMaxWidth(),
      ) {
        if (state.isLoading) {
          CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        } else {
          Text("Log in")
        }
      }

      OutlinedButton(
        onClick = { component.onIntent(AuthIntent.RegisterClicked) },
        enabled = !state.isLoading,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Text("Register")
      }
    }
  }
}
