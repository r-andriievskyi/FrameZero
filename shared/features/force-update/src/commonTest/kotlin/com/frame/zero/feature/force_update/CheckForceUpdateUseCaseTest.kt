package com.frame.zero.feature.force_update

import com.frame.zero.domain.Outcome
import com.frame.zero.repository.force_update.UpdatePolicy
import com.frame.zero.testing.FakeForceUpdateRepository
import com.frame.zero.testing.FakeAppVersionProvider
import com.frame.zero.core.config.AppVersion
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class CheckForceUpdateUseCaseTest {
  private fun useCase(
    policy: UpdatePolicy,
    currentBuild: Int
  ) = CheckForceUpdateUseCase(
    repository = FakeForceUpdateRepository(policy = policy),
    appVersionProvider = FakeAppVersionProvider(AppVersion(buildNumber = currentBuild, name = "x"))
  )

  private fun policy(
    min: Int,
    latest: Int,
    url: String = "store://app",
    message: String? = "update"
  ) = UpdatePolicy(minSupportedBuild = min, latestBuild = latest, storeUrl = url, message = message, critical = false)

  @Test
  fun hard_state_carries_message_and_url() =
    runTest {
      val outcome = useCase(policy(min = 5, latest = 8), currentBuild = 3)()
      val state = assertIs<Outcome.Success<ForceUpdateState>>(outcome).data
      val hard = assertIs<ForceUpdateState.Hard>(state)
      assertEquals("update", hard.message)
      assertEquals("store://app", hard.storeUrl)
    }

  @Test
  fun soft_state_for_newer_build_available() =
    runTest {
      val outcome = useCase(policy(min = 5, latest = 8), currentBuild = 6)()
      val state = assertIs<Outcome.Success<ForceUpdateState>>(outcome).data
      assertIs<ForceUpdateState.Soft>(state)
    }

  @Test
  fun none_when_current_is_latest() =
    runTest {
      val outcome = useCase(policy(min = 5, latest = 8), currentBuild = 8)()
      val state = assertIs<Outcome.Success<ForceUpdateState>>(outcome).data
      assertEquals(ForceUpdateState.None, state)
    }

  @Test
  fun repository_failure_surfaces_as_outcome_failure() =
    runTest {
      val useCase = CheckForceUpdateUseCase(
        repository = FakeForceUpdateRepository(error = IllegalStateException("boom")),
        appVersionProvider = FakeAppVersionProvider(AppVersion(buildNumber = 1, name = "x"))
      )
      assertTrue(useCase() is Outcome.Failure)
    }
}
