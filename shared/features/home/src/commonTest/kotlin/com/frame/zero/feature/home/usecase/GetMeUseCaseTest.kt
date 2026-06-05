package com.frame.zero.feature.home.usecase

import com.frame.zero.auth.dto.UserDto
import com.frame.zero.domain.DomainError
import com.frame.zero.domain.Outcome
import com.frame.zero.domain.User
import com.frame.zero.feature.home.testing.FakeUserRepository
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
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
  fun `IOException maps to Network failure`() =
    runTest {
      val repo = FakeUserRepository(throws = IOException("offline"))

      val outcome = GetMeUseCase(repo)()

      val failure = assertIs<Outcome.Failure>(outcome)
      assertEquals(DomainError.Network("offline"), failure.error)
    }
}
