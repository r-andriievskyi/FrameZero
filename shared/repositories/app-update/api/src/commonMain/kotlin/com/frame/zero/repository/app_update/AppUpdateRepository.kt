package com.frame.zero.repository.app_update

/**
 * The swap seam for the force-update feature. The only contract everything above it (use case,
 * ViewModel, UI) depends on — so the config source can change from Firebase Remote Config to our
 * own backend by adding a second impl and flipping the Koin binding, with nothing above this
 * interface touched.
 *
 * Speaks domain types only; the impl resolves the running platform and maps its raw config
 * (RC keys, or a DTO later) into a domain [UpdatePolicy].
 */
interface AppUpdateRepository {
  /**
   * Returns the release policy for the running platform, or throws on a network/config failure —
   * the check use case wraps this in `Outcome` (via the `UseCase` base) and treats a failure as
   * "no update", so a transient error can never produce a false lockout.
   */
  suspend fun fetchPolicy(): UpdatePolicy
}
