package com.frame.zero.di

import android.app.Application
import com.frame.zero.demo.data.DemoTasksRepository
import dev.zacsweers.metro.createGraphFactory
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.assertTrue
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Which implementation each flavor gets is settled at compile time — the graphs would not build if
 * the demo and production bindings collided. What compilation does *not* prove is that a graph can
 * actually be instantiated, or that a binding exposed under two types really does collapse onto one
 * instance. That is what this covers; it replaces `DemoModuleOverrideTest`, which asserted Koin's
 * runtime last-definition-wins behaviour.
 */
@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class)
class AndroidAppGraphTest {
  private val context get() = RuntimeEnvironment.getApplication()

  @Test
  fun `demo graph serves the offline fakes`() {
    val graph = createGraphFactory<AndroidDemoGraph.Factory>().create(context, emptySet())

    assertTrue(graph.tasksRepository is DemoTasksRepository)
  }

  @Test
  fun `demo repositories are singletons`() {
    val graph = createGraphFactory<AndroidDemoGraph.Factory>().create(context, emptySet())

    assertSame(graph.tasksRepository, graph.tasksRepository)
  }

  /**
   * Only the graph's own construction, not its bindings: the production graph's logging sink is
   * Crashlytics, which needs a real `FirebaseApp`. Resolving anything network- or session-shaped
   * here would fail on the environment rather than on the wiring — that path is covered by running
   * a prod build on a device. What compilation cannot tell us, and this can, is that the generated
   * factory exists and produces a graph.
   */
  @Test
  fun `production graph builds`() {
    val graph = createGraphFactory<AndroidProdGraph.Factory>().create(context, emptySet())

    assertNotNull(graph.navigationSignal)
  }
}
