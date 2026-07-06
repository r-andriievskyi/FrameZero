package com.frame.zero.feature.home.usecase

import com.frame.zero.auth.dto.UserDto
import com.frame.zero.domain.OfflineException
import com.frame.zero.domain.DomainError
import com.frame.zero.domain.Outcome
import com.frame.zero.domain.User
import com.frame.zero.testing.FakeUserRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GetMeUseCaseTest {
  @Test
  fun `success maps user dto to domain user`() =
    runTest {
      val repo = FakeUserRepository(
        userDto = UserDto(id = "u1", email = "u@x.com", firstName = "Ada", lastName = "Lovelace")
      )

      val outcome = GetMeUseCase(repo)()

      val success = assertIs<Outcome.Success<User>>(outcome)
      assertEquals(User(id = "u1", email = "u@x.com", firstName = "Ada", lastName = "Lovelace"), success.data)
      assertEquals(1, repo.getMeCalls)
    }

  @Test
  fun `OfflineException maps to Offline failure`() =
    runTest {
      val repo = FakeUserRepository(throws = OfflineException("offline"))

      val outcome = GetMeUseCase(repo)()

      val failure = assertIs<Outcome.Failure>(outcome)
      assertEquals(DomainError.Offline("offline"), failure.error)
    }

  @Test
  fun `generic exception maps to Unknown failure`() =
    runTest {
      val repo = FakeUserRepository(throws = RuntimeException("boom"))

      val outcome = GetMeUseCase(repo)()

      val failure = assertIs<Outcome.Failure>(outcome)
      assertEquals(DomainError.Unknown("boom"), failure.error)
    }
}
