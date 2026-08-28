package com.frame.zero.feature.force_update

import app.cash.turbine.test
import com.frame.zero.core.appupdate.StoreLauncher
import com.frame.zero.core.config.AppVersion
import com.frame.zero.repository.force_update.UpdatePolicy
import com.frame.zero.testing.FakeForceUpdateRepository
import com.frame.zero.testing.FakeAppVersionProvider
import com.frame.zero.testing.FakeConnectivityObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

private class RecordingStoreLauncher : StoreLauncher {
  var opened: String? = null

  override fun open(url: String) {
    opened = url
  }
}

@OptIn(ExperimentalCoroutinesApi::class)
class ForceUpdateControllerTest {
  // The controller's metered collector runs on an unconfined test dispatcher so it collects
  // eagerly and connectivity changes propagate synchronously — no manual scheduler advancing.
  // A Turbine collector observing controller.state from the test body still shares runTest's own
  // StandardTestDispatcher though, so advanceUntilIdle() is still needed before each check.
  private fun TestScope.controller(
    policy: UpdatePolicy,
    currentBuild: Int,
    launcher: StoreLauncher = RecordingStoreLauncher(),
    connectivity: FakeConnectivityObserver = FakeConnectivityObserver()
  ): ForceUpdateController {
    val useCase = CheckForceUpdateUseCase(
      repository = FakeForceUpdateRepository(policy = policy),
      appVersionProvider = FakeAppVersionProvider(AppVersion(buildNumber = currentBuild, name = "x"))
    )
    val scope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
    return ForceUpdateController(useCase, launcher, connectivity, scope)
  }

  private fun policy(
    min: Int,
    latest: Int,
    critical: Boolean = false
  ) = UpdatePolicy(
    minSupportedBuild = min,
    latestBuild = latest,
    storeUrl = "store://app",
    message = "m",
    critical = critical
  )

  @Test
  fun refresh_publishes_hard_state() =
    runTest {
      val controller = controller(policy(min = 5, latest = 8), currentBuild = 2)

      controller.state.test {
        controller.refresh()
        advanceUntilIdle()
        assertIs<ForceUpdateState.Hard>(expectMostRecentItem())
      }
    }

  @Test
  fun dismiss_hides_soft_prompt() =
    runTest {
      val controller = controller(policy(min = 5, latest = 8), currentBuild = 6)

      controller.state.test {
        controller.refresh()
        advanceUntilIdle()
        assertIs<ForceUpdateState.Soft>(expectMostRecentItem())

        controller.dismissSoft()
        advanceUntilIdle()
        assertEquals(ForceUpdateState.None, expectMostRecentItem())
      }
    }

  @Test
  fun dismissed_soft_stays_hidden_across_refresh() =
    runTest {
      val controller = controller(policy(min = 5, latest = 8), currentBuild = 6)

      controller.state.test {
        controller.refresh()
        controller.dismissSoft()
        controller.refresh()
        advanceUntilIdle()
        assertEquals(ForceUpdateState.None, expectMostRecentItem())
      }
    }

  @Test
  fun dismiss_does_not_hide_hard_gate() =
    runTest {
      val controller = controller(policy(min = 5, latest = 8), currentBuild = 2)

      controller.state.test {
        controller.refresh()
        controller.dismissSoft()
        advanceUntilIdle()
        assertIs<ForceUpdateState.Hard>(expectMostRecentItem())
      }
    }

  @Test
  fun repository_failure_is_fail_open_none() =
    runTest {
      val useCase = CheckForceUpdateUseCase(
        repository = FakeForceUpdateRepository(error = IllegalStateException("boom")),
        appVersionProvider = FakeAppVersionProvider(AppVersion(buildNumber = 1, name = "x"))
      )
      val controller = ForceUpdateController(
        useCase,
        RecordingStoreLauncher(),
        FakeConnectivityObserver(),
        CoroutineScope(UnconfinedTestDispatcher(testScheduler))
      )

      controller.state.test {
        controller.refresh()
        advanceUntilIdle()
        assertEquals(ForceUpdateState.None, expectMostRecentItem())
      }
    }

  @Test
  fun open_store_launches_active_url() =
    runTest {
      val launcher = RecordingStoreLauncher()
      val controller = controller(policy(min = 5, latest = 8), currentBuild = 2, launcher = launcher)
      controller.refresh()
      controller.openStore()
      assertEquals("store://app", launcher.opened)
    }

  @Test
  fun open_store_noop_when_no_update() =
    runTest {
      val launcher = RecordingStoreLauncher()
      val controller = controller(policy(min = 0, latest = 0), currentBuild = 5, launcher = launcher)
      controller.refresh()
      controller.openStore()
      assertNull(launcher.opened)
    }

  @Test
  fun soft_deferred_on_metered_connection() =
    runTest {
      val controller = controller(
        policy(min = 5, latest = 8),
        currentBuild = 6,
        connectivity = FakeConnectivityObserver(initiallyMetered = true)
      )

      controller.state.test {
        controller.refresh()
        advanceUntilIdle()
        assertEquals(ForceUpdateState.None, expectMostRecentItem())
      }
    }

  @Test
  fun critical_soft_shows_on_metered_connection() =
    runTest {
      val controller = controller(
        policy(min = 5, latest = 8, critical = true),
        currentBuild = 6,
        connectivity = FakeConnectivityObserver(initiallyMetered = true)
      )

      controller.state.test {
        controller.refresh()
        advanceUntilIdle()
        assertIs<ForceUpdateState.Soft>(expectMostRecentItem())
      }
    }

  @Test
  fun deferred_soft_resurfaces_when_leaving_metered() =
    runTest {
      val connectivity = FakeConnectivityObserver(initiallyMetered = true)
      val controller = controller(policy(min = 5, latest = 8), currentBuild = 6, connectivity = connectivity)

      controller.state.test {
        controller.refresh()
        advanceUntilIdle()
        assertEquals(ForceUpdateState.None, expectMostRecentItem())

        connectivity.metered.value = false
        advanceUntilIdle()
        assertIs<ForceUpdateState.Soft>(expectMostRecentItem())
      }
    }

  @Test
  fun dismissed_soft_stays_hidden_when_leaving_metered() =
    runTest {
      val connectivity = FakeConnectivityObserver(initiallyMetered = true)
      val controller = controller(policy(min = 5, latest = 8), currentBuild = 6, connectivity = connectivity)

      controller.state.test {
        controller.refresh()
        advanceUntilIdle()
        assertEquals(ForceUpdateState.None, expectMostRecentItem())

        // Leave metered so the prompt resurfaces, dismiss it, then it must stay hidden even as the
        // metered state keeps flipping.
        connectivity.metered.value = false
        advanceUntilIdle()
        assertIs<ForceUpdateState.Soft>(expectMostRecentItem())

        controller.dismissSoft()
        advanceUntilIdle()
        assertEquals(ForceUpdateState.None, expectMostRecentItem())

        connectivity.metered.value = true
        connectivity.metered.value = false
        advanceUntilIdle()
        expectNoEvents()
      }
      assertEquals(ForceUpdateState.None, controller.state.value)
    }

  @Test
  fun hard_gate_shows_on_metered_connection() =
    runTest {
      val controller = controller(
        policy(min = 5, latest = 8),
        currentBuild = 2,
        connectivity = FakeConnectivityObserver(initiallyMetered = true)
      )

      controller.state.test {
        controller.refresh()
        advanceUntilIdle()
        assertIs<ForceUpdateState.Hard>(expectMostRecentItem())
      }
    }
}
