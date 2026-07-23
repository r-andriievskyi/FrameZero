package com.frame.zero.demo.data

import com.frame.zero.repository.force_update.ForceUpdateRepository
import com.frame.zero.repository.force_update.UpdatePolicy

class DemoForceUpdateRepository : ForceUpdateRepository {
  override suspend fun fetchPolicy(): UpdatePolicy =
    UpdatePolicy(
      minSupportedBuild = 0,
      latestBuild = 0,
      storeUrl = "",
      message = null,
      critical = false
    )
}
