package com.frame.zero.core.security

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSError
import platform.LocalAuthentication.LAContext
import platform.LocalAuthentication.LAErrorAppCancel
import platform.LocalAuthentication.LAErrorSystemCancel
import platform.LocalAuthentication.LAErrorUserCancel
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthenticationWithBiometrics
import kotlin.coroutines.resume

/**
 * [BiometricAuthenticator] backed by `LAContext` (Face ID / Touch ID). A fresh context
 * is used per evaluation because `LAContext` caches a successful result for its lifetime.
 *
 * Requires `NSFaceIDUsageDescription` in the iOS app's Info.plist — without it the system
 * kills the app when the Face ID prompt is requested.
 */
@OptIn(ExperimentalForeignApi::class)
class IosBiometricAuthenticator : BiometricAuthenticator {
  override fun availability(): BiometricAvailability {
    val context = LAContext()
    val canEvaluate = context.canEvaluatePolicy(
      LAPolicyDeviceOwnerAuthenticationWithBiometrics,
      error = null
    )
    if (canEvaluate) return BiometricAvailability.Available
    // canEvaluatePolicy failed — we can't read the NSError out-param ergonomically from
    // Kotlin/Native here, so collapse the failure reasons to Unavailable.
    return BiometricAvailability.Unavailable
  }

  override suspend fun authenticate(prompt: BiometricPromptText): BiometricResult =
    suspendCancellableCoroutine { continuation ->
      val context = LAContext()
      context.localizedCancelTitle = prompt.negativeButton
      context.evaluatePolicy(
        LAPolicyDeviceOwnerAuthenticationWithBiometrics,
        localizedReason = prompt.subtitle.ifBlank { prompt.title }
      ) { success, error ->
        if (!continuation.isActive) return@evaluatePolicy
        continuation.resume(
          when {
            success -> BiometricResult.Success
            error.isUserCancellation() -> BiometricResult.Cancelled
            else -> BiometricResult.Error(error?.localizedDescription ?: "Biometric authentication failed")
          }
        )
      }
    }

  private fun NSError?.isUserCancellation(): Boolean {
    val code = this?.code ?: return false
    return code == LAErrorUserCancel ||
      code == LAErrorAppCancel ||
      code == LAErrorSystemCancel
  }
}
