package com.frame.zero.core.session

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LogoutSignalTest {
  @Test
  fun `emit reaches a subscribed collector`() =
    runTest {
      val signal = LogoutSignal()

      signal.events.test {
        signal.emit()

        assertEquals(Unit, awaitItem())
      }
    }

  @Test
  fun `emit with no collectors does not throw`() {
    val signal = LogoutSignal()

    signal.emit()
    signal.emit()
    // Absence of an exception is the assertion.
  }
}
