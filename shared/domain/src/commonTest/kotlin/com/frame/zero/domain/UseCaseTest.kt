package com.frame.zero.domain

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class UseCaseTest {
  private class BoomUseCase(
    private val throwable: Throwable
  ) : UseCase<Unit, Int>() {
    override suspend fun execute(params: Unit): Int = throw throwable
  }

  private class BoomNoParamsUseCase(
    private val throwable: Throwable
  ) : NoParamsUseCase<Int>() {
    override suspend fun execute(): Int = throw throwable
  }

  @Test
  fun `maps a thrown exception to a Failure via toDomainError`() =
    runTest {
      val outcome = BoomUseCase(RuntimeException("kaboom"))(Unit)

      val failure = assertIs<Outcome.Failure>(outcome)
      assertEquals(DomainError.Unknown, failure.error)
    }

  @Test
  fun `rethrows CancellationException instead of mapping it`() =
    runTest {
      assertFailsWith<CancellationException> {
        BoomUseCase(CancellationException("cancelled"))(Unit)
      }
    }

  @Test
  fun `NoParamsUseCase also rethrows CancellationException`() =
    runTest {
      assertFailsWith<CancellationException> {
        BoomNoParamsUseCase(CancellationException("cancelled"))()
      }
    }
}
