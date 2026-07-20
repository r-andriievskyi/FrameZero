package com.frame.zero.demo.data

import com.frame.zero.repository.app_update.AppUpdateRepository
import com.frame.zero.repository.app_update.UpdatePolicy

class DemoAppUpdateRepository : AppUpdateRepository {
  override suspend fun fetchPolicy(): UpdatePolicy =
    UpdatePolicy(
      minSupportedBuild = 0,
      latestBuild = 0,
      storeUrl = "",
      message = null,
      critical = false
    )
}
