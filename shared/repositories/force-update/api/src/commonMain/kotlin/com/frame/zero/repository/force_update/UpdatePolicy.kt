package com.frame.zero.repository.force_update

/**
 * Release policy for the current platform, supplied by an [ForceUpdateRepository] (Remote Config
 * today, a backend endpoint later). Build numbers are integer store builds — Android `versionCode`
 * / iOS `CFBundleVersion` — so the client compares them numerically against the running build.
 *
 * @param minSupportedBuild builds below this are gated (`HARD`).
 * @param latestBuild the newest published build; a running build below it but at/above
 *   [minSupportedBuild] is a `SOFT` update.
 * @param storeUrl deep link to this platform's store listing.
 * @param message optional human-facing copy shown in the prompt/overlay.
 * @param critical when `true`, the update must surface regardless of network state; when `false`,
 *   a non-mandatory prompt may be deferred (e.g. off metered connections).
 */
data class UpdatePolicy(
  val minSupportedBuild: Int,
  val latestBuild: Int,
  val storeUrl: String,
  val message: String?,
  val critical: Boolean
)
