package com.frame.zero.feature.task.details.usecase

import com.frame.zero.domain.Outcome
import com.frame.zero.dto.task.TaskDetailDto
import com.frame.zero.testing.FakeTasksRepository
import com.frame.zero.testing.taskDetailDto
import com.frame.zero.testing.taskParticipantDto
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class UpdateTaskParticipantsUseCaseTest {
  @Test
  fun `forwards taskId and userIds and returns the updated detail`() =
    runTest {
      val updated = taskDetailDto(id = "t1", participants = listOf(taskParticipantDto(userId = "u1")))
      val repo = FakeTasksRepository(updatedParticipantsTask = updated)

      val outcome = UpdateTaskParticipantsUseCase(repo)(
        UpdateTaskParticipantsUseCase.Params(taskId = "t1", participantUserIds = listOf("u1"))
      )

      val success = assertIs<Outcome.Success<TaskDetailDto>>(outcome)
      assertEquals(listOf("u1"), success.data.participants.map { it.userId })
      assertEquals(listOf("t1" to listOf("u1")), repo.updateParticipantsCalls)
    }
}
