package com.frame.zero.feature.appupdate

import com.frame.zero.domain.Outcome
import com.frame.zero.repository.app_update.UpdatePolicy
import com.frame.zero.testing.FakeAppUpdateRepository
import com.frame.zero.testing.FakeAppVersionProvider
import com.frame.zero.core.config.AppVersion
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class CheckAppUpdateUseCaseTest {

  private fun useCase(policy: UpdatePolicy, currentBuild: Int) = CheckAppUpdateUseCase(
    repository = FakeAppUpdateRepository(policy = policy),
    appVersionProvider = FakeAppVersionProvider(AppVersion(buildNumber = currentBuild, name = "x"))
  )

  private fun policy(min: Int, latest: Int, url: String = "store://app", message: String? = "update") =
    UpdatePolicy(minSupportedBuild = min, latestBuild = latest, storeUrl = url, message = message, critical = false)

  @Test
  fun hard_state_carries_message_and_url() = runTest {
    val outcome = useCase(policy(min = 5, latest = 8), currentBuild = 3)()
    val state = assertIs<Outcome.Success<AppUpdateState>>(outcome).data
    val hard = assertIs<AppUpdateState.Hard>(state)
    assertEquals("update", hard.message)
    assertEquals("store://app", hard.storeUrl)
  }

  @Test
  fun soft_state_for_newer_build_available() = runTest {
    val outcome = useCase(policy(min = 5, latest = 8), currentBuild = 6)()
    val state = assertIs<Outcome.Success<AppUpdateState>>(outcome).data
    assertIs<AppUpdateState.Soft>(state)
  }

  @Test
  fun none_when_current_is_latest() = runTest {
    val outcome = useCase(policy(min = 5, latest = 8), currentBuild = 8)()
    val state = assertIs<Outcome.Success<AppUpdateState>>(outcome).data
    assertEquals(AppUpdateState.None, state)
  }

  @Test
  fun repository_failure_surfaces_as_outcome_failure() = runTest {
    val useCase = CheckAppUpdateUseCase(
      repository = FakeAppUpdateRepository(error = IllegalStateException("boom")),
      appVersionProvider = FakeAppVersionProvider(AppVersion(buildNumber = 1, name = "x"))
    )
    assertTrue(useCase() is Outcome.Failure)
  }
}
