package com.frame.zero.core.config

/**
 * The running app's version, read from the platform at runtime.
 *
 * @param buildNumber the monotonic store build — Android `versionCode` / iOS `CFBundleVersion` —
 *   compared numerically against the release policy's thresholds.
 * @param name the human-facing version string (Android `versionName` / iOS
 *   `CFBundleShortVersionString`), for display only.
 */
data class AppVersion(
  val buildNumber: Int,
  val name: String
)
