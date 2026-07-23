package com.frame.zero.feature.app_update

import com.frame.zero.core.config.AppVersionProvider
import com.frame.zero.domain.NoParamsUseCase
import com.frame.zero.repository.app_update.AppUpdateRepository
import com.frame.zero.repository.app_update.UpdatePolicy
import com.frame.zero.repository.app_update.UpdateType

/**
 * Fetches the release policy and compares it against the running build to produce an
 * [AppUpdateState]. Throws from the repository propagate; the `NoParamsUseCase` base wraps this in
 * `Outcome`, and [AppUpdateController] treats a failure as [AppUpdateState.None] (fail-open).
 */
class CheckAppUpdateUseCase(
  private val repository: AppUpdateRepository,
  private val appVersionProvider: AppVersionProvider
) : NoParamsUseCase<AppUpdateState>() {
  override suspend fun execute(): AppUpdateState {
    val policy = repository.fetchPolicy()
    val currentBuild = appVersionProvider.current().buildNumber
    return when (deriveUpdateType(currentBuild, policy)) {
      UpdateType.NONE -> AppUpdateState.None
      UpdateType.SOFT -> AppUpdateState.Soft(policy.message, policy.storeUrl, policy.critical)
      UpdateType.HARD -> AppUpdateState.Hard(policy.message, policy.storeUrl)
    }
  }
}

/**
 * Pure build-number comparison. A build below [UpdatePolicy.minSupportedBuild] is [UpdateType.HARD];
 * below [UpdatePolicy.latestBuild] but at/above the minimum is [UpdateType.SOFT]; otherwise
 * [UpdateType.NONE]. All-zero policy (the fail-open default) yields [UpdateType.NONE] for any real
 * build.
 */
internal fun deriveUpdateType(
  currentBuild: Int,
  policy: UpdatePolicy
): UpdateType =
  when {
    currentBuild < policy.minSupportedBuild -> UpdateType.HARD
    currentBuild < policy.latestBuild -> UpdateType.SOFT
    else -> UpdateType.NONE
  }
