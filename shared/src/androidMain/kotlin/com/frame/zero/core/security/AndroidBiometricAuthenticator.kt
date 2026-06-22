package com.frame.zero.core.security

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.resume

class AndroidBiometricAuthenticator(
  private val activityHolder: ActivityHolder
) : BiometricAuthenticator {
  override fun availability(): BiometricAvailability {
    val activity = activityHolder.activity ?: return BiometricAvailability.Unavailable
    return when (BiometricManager.from(activity).canAuthenticate(BIOMETRIC_STRONG)) {
      BiometricManager.BIOMETRIC_SUCCESS -> BiometricAvailability.Available
      BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE,
      BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricAvailability.NoHardware
      BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricAvailability.NotEnrolled
      else -> BiometricAvailability.Unavailable
    }
  }

  override suspend fun authenticate(prompt: BiometricPromptText): BiometricResult {
    val activity = activityHolder.activity
      ?: return BiometricResult.Error("No foreground activity to host the biometric prompt")

    return withContext(Dispatchers.Main) {
      suspendCancellableCoroutine { continuation ->
        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt = BiometricPrompt(
          activity,
          executor,
          object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
              if (continuation.isActive) continuation.resume(BiometricResult.Success)
            }

            override fun onAuthenticationError(
              errorCode: Int,
              errString: CharSequence
            ) {
              if (!continuation.isActive) return
              continuation.resume(
                if (errorCode.isUserCancellation()) {
                  BiometricResult.Cancelled
                } else {
                  BiometricResult.Error(errString.toString())
                }
              )
            }

            // fired on a non-fatal failed match; the prompt stays open for a retry, so
            // we deliberately do not resume the continuation here.
            override fun onAuthenticationFailed() = Unit
          }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
          .setTitle(prompt.title)
          .setSubtitle(prompt.subtitle)
          .setNegativeButtonText(prompt.negativeButton)
          .setAllowedAuthenticators(BIOMETRIC_STRONG)
          .build()

        biometricPrompt.authenticate(promptInfo)
        continuation.invokeOnCancellation { biometricPrompt.cancelAuthentication() }
      }
    }
  }

  private fun Int.isUserCancellation(): Boolean =
    this == BiometricPrompt.ERROR_USER_CANCELED ||
      this == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
      this == BiometricPrompt.ERROR_CANCELED
}
