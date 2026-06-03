package com.frame.zero.shared.design_system.widgets.toast

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import kotlinx.coroutines.delay

private const val ToastAutoDismissMillis = 4000L

/**
 * Screen-level overlay that animates a single [AppToast] in from the bottom
 * edge and auto-dismisses it after [autoDismissMillis].
 *
 * Drop it as the last child of a screen's root [Box] so it draws above the
 * content:
 * ```
 * Box(Modifier.fillMaxSize()) {
 *   ScreenContent(...)
 *   ToastHost(message = state.errorToast, onDismiss = { onIntent(ToastDismissed) })
 * }
 * ```
 *
 * @param message Toast text. Non-null shows the toast; null hides it.
 * @param onDismiss Invoked when the auto-dismiss timer elapses.
 * @param severity Color treatment for the toast.
 * @param autoDismissMillis How long the toast stays visible before dismissing.
 */
@Composable
fun ToastHost(
  message: String?,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier,
  severity: ToastSeverity = ToastSeverity.Error,
  autoDismissMillis: Long = ToastAutoDismissMillis
) {
  // Retain the last text so it keeps rendering through the exit animation
  // after [message] flips back to null.
  var lastMessage by remember { mutableStateOf(message) }
  if (message != null) lastMessage = message

  LaunchedEffect(message) {
    if (message != null) {
      delay(autoDismissMillis)
      onDismiss()
    }
  }

  Box(modifier = modifier.fillMaxSize()) {
    AnimatedVisibility(
      visible = message != null,
      enter = fadeIn() + slideInVertically { height -> height },
      exit = fadeOut() + slideOutVertically { height -> height },
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .systemBarsPadding()
        .padding(AppTheme.spacingSystem.space16)
    ) {
      lastMessage?.let { text ->
        AppToast(title = text, severity = severity)
      }
    }
  }
}

@LightDarkPreview
@Composable
private fun ToastHostPreview() {
  AppTheme {
    Box(modifier = Modifier.fillMaxSize().background(AppTheme.colorSystem.background)) {
      // Render the toast directly so the preview is static (no timers).
      AppToast(
        title = "Network error: couldn't reach the server",
        severity = ToastSeverity.Error,
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .systemBarsPadding()
          .padding(AppTheme.spacingSystem.space16)
      )
    }
  }
}
