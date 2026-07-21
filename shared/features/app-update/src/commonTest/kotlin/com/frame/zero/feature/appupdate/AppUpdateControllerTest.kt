package com.frame.zero.feature.appupdate

import com.frame.zero.core.appupdate.StoreLauncher
import com.frame.zero.core.config.AppVersion
import com.frame.zero.repository.app_update.UpdatePolicy
import com.frame.zero.testing.FakeAppUpdateRepository
import com.frame.zero.testing.FakeAppVersionProvider
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

private class RecordingStoreLauncher : StoreLauncher {
  var opened: String? = null
  override fun open(url: String) { opened = url }
}

class AppUpdateControllerTest {

  private fun controller(
    policy: UpdatePolicy,
    currentBuild: Int,
    launcher: StoreLauncher = RecordingStoreLauncher()
  ): AppUpdateController {
    val useCase = CheckAppUpdateUseCase(
      repository = FakeAppUpdateRepository(policy = policy),
      appVersionProvider = FakeAppVersionProvider(AppVersion(buildNumber = currentBuild, name = "x"))
    )
    return AppUpdateController(useCase, launcher)
  }

  private fun policy(min: Int, latest: Int) =
    UpdatePolicy(minSupportedBuild = min, latestBuild = latest, storeUrl = "store://app", message = "m", critical = false)

  @Test
  fun refresh_publishes_hard_state() = runTest {
    val controller = controller(policy(min = 5, latest = 8), currentBuild = 2)
    controller.refresh()
    assertIs<AppUpdateState.Hard>(controller.state.value)
  }

  @Test
  fun dismiss_hides_soft_prompt() = runTest {
    val controller = controller(policy(min = 5, latest = 8), currentBuild = 6)
    controller.refresh()
    assertIs<AppUpdateState.Soft>(controller.state.value)

    controller.dismissSoft()
    assertEquals(AppUpdateState.None, controller.state.value)
  }

  @Test
  fun dismissed_soft_stays_hidden_across_refresh() = runTest {
    val controller = controller(policy(min = 5, latest = 8), currentBuild = 6)
    controller.refresh()
    controller.dismissSoft()
    controller.refresh()
    assertEquals(AppUpdateState.None, controller.state.value)
  }

  @Test
  fun dismiss_does_not_hide_hard_gate() = runTest {
    val controller = controller(policy(min = 5, latest = 8), currentBuild = 2)
    controller.refresh()
    controller.dismissSoft()
    assertIs<AppUpdateState.Hard>(controller.state.value)
  }

  @Test
  fun repository_failure_is_fail_open_none() = runTest {
    val useCase = CheckAppUpdateUseCase(
      repository = FakeAppUpdateRepository(error = IllegalStateException("boom")),
      appVersionProvider = FakeAppVersionProvider(AppVersion(buildNumber = 1, name = "x"))
    )
    val controller = AppUpdateController(useCase, RecordingStoreLauncher())
    controller.refresh()
    assertEquals(AppUpdateState.None, controller.state.value)
  }

  @Test
  fun open_store_launches_active_url() = runTest {
    val launcher = RecordingStoreLauncher()
    val controller = controller(policy(min = 5, latest = 8), currentBuild = 2, launcher = launcher)
    controller.refresh()
    controller.openStore()
    assertEquals("store://app", launcher.opened)
  }

  @Test
  fun open_store_noop_when_no_update() = runTest {
    val launcher = RecordingStoreLauncher()
    val controller = controller(policy(min = 0, latest = 0), currentBuild = 5, launcher = launcher)
    controller.refresh()
    controller.openStore()
    assertNull(launcher.opened)
  }
}
