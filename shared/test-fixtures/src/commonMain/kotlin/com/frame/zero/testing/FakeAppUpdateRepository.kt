package com.frame.zero.testing

import com.frame.zero.repository.app_update.AppUpdateRepository
import com.frame.zero.repository.app_update.UpdatePolicy

class FakeAppUpdateRepository(
  var policy: UpdatePolicy = UpdatePolicy(
    minSupportedBuild = 0,
    latestBuild = 0,
    storeUrl = "",
    message = null,
    critical = false
  ),
  var error: Throwable? = null
) : AppUpdateRepository {
  override suspend fun fetchPolicy(): UpdatePolicy = error?.let { throw it } ?: policy
}
