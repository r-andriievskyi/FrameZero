package com.frame.zero.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class OutcomeTest {
  @Test
  fun `map transforms data on Success`() {
    val outcome: Outcome<Int> = Outcome.Success(2)

    val mapped = outcome.map { it * 5 }

    assertEquals(Outcome.Success(10), mapped)
  }

  @Test
  fun `map preserves Failure unchanged and does not call transform`() {
    val outcome: Outcome<Int> = Outcome.Failure(DomainError.InvalidCredentials)
    var called = false

    val mapped =
      outcome.map {
        called = true
        it * 2
      }

    assertFalse(called)
    val failure = assertIs<Outcome.Failure>(mapped)
    assertEquals(DomainError.InvalidCredentials, failure.error)
  }

  @Test
  fun `onSuccess invokes action with data on Success`() {
    var captured: Int? = null

    Outcome.Success(42).onSuccess { captured = it }

    assertEquals(42, captured)
  }

  @Test
  fun `onSuccess does not invoke action on Failure`() {
    val outcome: Outcome<Int> = Outcome.Failure(DomainError.InvalidCredentials)
    var called = false

    outcome.onSuccess { called = true }

    assertFalse(called)
  }

  @Test
  fun `onSuccess returns the same outcome for chaining`() {
    val outcome: Outcome<Int> = Outcome.Success(1)

    val returned = outcome.onSuccess { /* no-op */ }

    assertTrue(returned === outcome)
  }

  @Test
  fun `onFailure invokes action with error on Failure`() {
    val outcome: Outcome<Int> = Outcome.Failure(DomainError.Network("offline"))
    var captured: DomainError? = null

    outcome.onFailure { captured = it }

    assertEquals(DomainError.Network("offline"), captured)
  }

  @Test
  fun `onFailure does not invoke action on Success`() {
    var called = false

    Outcome.Success(1).onFailure { called = true }

    assertFalse(called)
  }
}
