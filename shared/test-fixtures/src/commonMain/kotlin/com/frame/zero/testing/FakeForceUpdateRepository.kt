package com.frame.zero.testing

import com.frame.zero.repository.force_update.ForceUpdateRepository
import com.frame.zero.repository.force_update.UpdatePolicy

class FakeForceUpdateRepository(
  var policy: UpdatePolicy = UpdatePolicy(
    minSupportedBuild = 0,
    latestBuild = 0,
    storeUrl = "",
    message = null,
    critical = false
  ),
  var error: Throwable? = null
) : ForceUpdateRepository {
  override suspend fun fetchPolicy(): UpdatePolicy = error?.let { throw it } ?: policy
}
