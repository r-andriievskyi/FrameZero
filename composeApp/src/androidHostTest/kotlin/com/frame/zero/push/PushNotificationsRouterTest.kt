package com.frame.zero.push

import android.app.Application
import android.content.Intent
import app.cash.turbine.test
import com.frame.zero.core.navigation.DeepLink
import com.frame.zero.core.navigation.NavigationSignal
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class)
class PushNotificationsRouterTest {
  private val signal = NavigationSignal()
  private val router = PushNotificationsRouter(signal)

  @Test
  fun `routes the task id extra to a TaskDetails deep link`() =
    runTest {
      router.route(intentWithTaskId("task-42"))

      signal.events.test {
        assertEquals(DeepLink.TaskDetails("task-42"), awaitItem())
      }
    }

  @Test
  fun `consumes the extra so a re-delivered intent does not navigate twice`() {
    val intent = intentWithTaskId("task-42")

    router.route(intent)

    // The id is stripped after the first read; routing the same intent again is a no-op.
    assertNull(intent.getStringExtra(PushNotifications.EXTRA_TASK_ID))
  }

  @Test
  fun `ignores a null intent`() =
    runTest {
      router.route(null)

      assertNoDeepLink()
    }

  @Test
  fun `ignores an intent without the task id extra`() =
    runTest {
      router.route(Intent())

      assertNoDeepLink()
    }

  private fun intentWithTaskId(taskId: String): Intent = Intent().putExtra(PushNotifications.EXTRA_TASK_ID, taskId)

  private suspend fun assertNoDeepLink() =
    signal.events.test {
      expectNoEvents()
    }
}
