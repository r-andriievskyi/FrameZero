package com.frame.zero.feature.app_update

import com.frame.zero.repository.app_update.UpdatePolicy
import com.frame.zero.repository.app_update.UpdateType
import kotlin.test.Test
import kotlin.test.assertEquals

class DeriveUpdateTypeTest {
  private fun policy(
    min: Int,
    latest: Int
  ) = UpdatePolicy(minSupportedBuild = min, latestBuild = latest, storeUrl = "", message = null, critical = false)

  @Test
  fun below_minimum_is_hard() {
    assertEquals(UpdateType.HARD, deriveUpdateType(currentBuild = 4, policy = policy(min = 5, latest = 8)))
  }

  @Test
  fun at_minimum_but_below_latest_is_soft() {
    assertEquals(UpdateType.SOFT, deriveUpdateType(currentBuild = 5, policy = policy(min = 5, latest = 8)))
  }

  @Test
  fun below_latest_is_soft() {
    assertEquals(UpdateType.SOFT, deriveUpdateType(currentBuild = 7, policy = policy(min = 5, latest = 8)))
  }

  @Test
  fun at_latest_is_none() {
    assertEquals(UpdateType.NONE, deriveUpdateType(currentBuild = 8, policy = policy(min = 5, latest = 8)))
  }

  @Test
  fun above_latest_is_none() {
    assertEquals(UpdateType.NONE, deriveUpdateType(currentBuild = 12, policy = policy(min = 5, latest = 8)))
  }

  @Test
  fun all_zero_policy_is_none_for_any_real_build() {
    assertEquals(UpdateType.NONE, deriveUpdateType(currentBuild = 1, policy = policy(min = 0, latest = 0)))
    assertEquals(UpdateType.NONE, deriveUpdateType(currentBuild = Int.MAX_VALUE, policy = policy(min = 0, latest = 0)))
  }
}
