package com.frame.zero.feature.task.details.usecase

import com.frame.zero.domain.Outcome
import com.frame.zero.domain.task.AssignableMember
import com.frame.zero.testing.FakeProductionsRepository
import com.frame.zero.testing.productionMember
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class GetAssignableMembersUseCaseTest {
  @Test
  fun `maps linked members to assignable members`() =
    runTest {
      val repo = FakeProductionsRepository(
        members = listOf(
          productionMember(id = "m1", userId = "u1", name = "Ada", initials = "AD", avatarColorHex = "#111")
        )
      )

      val outcome = GetAssignableMembersUseCase(repo)(GetAssignableMembersUseCase.Params("p1"))

      val success = assertIs<Outcome.Success<List<AssignableMember>>>(outcome)
      assertEquals(
        listOf(AssignableMember(userId = "u1", name = "Ada", initials = "AD", avatarColorHex = "#111")),
        success.data
      )
    }

  @Test
  fun `filters out members without a user account`() =
    runTest {
      val repo = FakeProductionsRepository(
        members = listOf(
          productionMember(id = "m1", userId = "u1", name = "Ada"),
          productionMember(id = "m2", userId = null, name = "Unlinked crew")
        )
      )

      val outcome = GetAssignableMembersUseCase(repo)(GetAssignableMembersUseCase.Params("p1"))

      val success = assertIs<Outcome.Success<List<AssignableMember>>>(outcome)
      assertEquals(listOf("u1"), success.data.map { it.userId })
    }

  @Test
  fun `deduplicates members by user id`() =
    runTest {
      val repo = FakeProductionsRepository(
        members = listOf(
          productionMember(id = "m1", userId = "u1", name = "Ada"),
          productionMember(id = "m2", userId = "u1", name = "Ada (Producer hat)")
        )
      )

      val outcome = GetAssignableMembersUseCase(repo)(GetAssignableMembersUseCase.Params("p1"))

      val success = assertIs<Outcome.Success<List<AssignableMember>>>(outcome)
      assertEquals(1, success.data.size)
      assertEquals("Ada", success.data.single().name)
    }
}
