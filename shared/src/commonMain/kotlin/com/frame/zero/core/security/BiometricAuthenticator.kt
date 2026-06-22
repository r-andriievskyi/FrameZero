package com.frame.zero.core.security

/**
 * Presents the platform biometric prompt (Android `BiometricPrompt`, iOS `LAContext`)
 * and reports whether the device owner authenticated.
 */
interface BiometricAuthenticator {
  /** Whether the device can prompt for biometrics right now. */
  fun availability(): BiometricAvailability

  /**
   * Shows the system biometric prompt and suspends until the owner authenticates,
   * cancels, or the attempt errors. Never throws — failures map to [BiometricResult].
   */
  suspend fun authenticate(prompt: BiometricPromptText): BiometricResult
}

enum class BiometricAvailability {
  /** Hardware present and at least one credential enrolled — a prompt will show. */
  Available,

  /** No biometric hardware on this device. */
  NoHardware,

  /** Hardware present but the user has not enrolled any biometric. */
  NotEnrolled,

  /** Temporarily unavailable (e.g. hardware busy) or status unknown. */
  Unavailable
}

sealed interface BiometricResult {
  /** The owner authenticated successfully. */
  data object Success : BiometricResult

  /** The owner dismissed the prompt or tapped the negative button. */
  data object Cancelled : BiometricResult

  /** The attempt could not run / errored (no hardware, lockout, etc.). */
  data class Error(
    val message: String
  ) : BiometricResult
}

/**
 * Copy shown on the system prompt. Resolved from string resources at the UI layer and
 * passed down so `shared` never depends on Compose resources. [negativeButton] is the
 * Android-only cancel label; iOS supplies its own.
 */
data class BiometricPromptText(
  val title: String,
  val subtitle: String,
  val negativeButton: String
)
