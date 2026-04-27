package com.frame.zero.core.session

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class LogoutSignalTest {

  @Test
  fun `emit reaches a subscribed collector`() =
    runTest(UnconfinedTestDispatcher()) {
      val signal = LogoutSignal()
      var count = 0
      val job = launch { signal.events.collect { count++ } }

      signal.emit()

      assertEquals(1, count)
      job.cancel()
    }

  @Test
  fun `emit with no collectors does not throw`() {
    val signal = LogoutSignal()

    signal.emit()
    signal.emit()
    // Absence of an exception is the assertion.
  }
}
