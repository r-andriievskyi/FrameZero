package com.frame.zero.feature.force_update

import com.frame.zero.core.config.AppVersionProvider
import com.frame.zero.domain.NoParamsUseCase
import com.frame.zero.repository.force_update.ForceUpdateRepository
import com.frame.zero.repository.force_update.UpdatePolicy
import com.frame.zero.repository.force_update.UpdateType

/**
 * Fetches the release policy and compares it against the running build to produce an
 * [ForceUpdateState]. Throws from the repository propagate; the `NoParamsUseCase` base wraps this in
 * `Outcome`, and [ForceUpdateController] treats a failure as [ForceUpdateState.None] (fail-open).
 */
class CheckForceUpdateUseCase(
  private val repository: ForceUpdateRepository,
  private val appVersionProvider: AppVersionProvider
) : NoParamsUseCase<ForceUpdateState>() {
  override suspend fun execute(): ForceUpdateState {
    val policy = repository.fetchPolicy()
    val currentBuild = appVersionProvider.current().buildNumber
    return when (deriveUpdateType(currentBuild, policy)) {
      UpdateType.NONE -> ForceUpdateState.None
      UpdateType.SOFT -> ForceUpdateState.Soft(policy.message, policy.storeUrl, policy.critical)
      UpdateType.HARD -> ForceUpdateState.Hard(policy.message, policy.storeUrl)
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
