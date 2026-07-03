package com.frame.zero.task

import com.frame.zero.AppError
import com.frame.zero.AppException
import com.frame.zero.common.testing.TestAppEnv
import com.frame.zero.domain.production.Genre
import com.frame.zero.dto.device.DevicePlatform
import com.frame.zero.dto.production.CreateProductionRequest
import com.frame.zero.dto.task.CreateTaskRequest
import com.frame.zero.dto.task.UpdateTaskParticipantsRequest
import com.frame.zero.dto.task.UpdateTaskRequest
import com.frame.zero.notification.TaskAssignmentNotifier
import com.frame.zero.task.testing.FakeTaskRepository
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TaskServiceTest {
  private val productionRequest =
    CreateProductionRequest(
      title = "Film",
      genre = Genre.DRAMA,
      startDate = LocalDate(2026, 1, 1),
      wrapDate = LocalDate(2026, 12, 31)
    )

  @Test
  fun `assigning a task to someone notifies them and pushes to their devices`() =
    runBlocking {
      val env = TestAppEnv()
      val owner = UUID.randomUUID()
      val assignee = UUID.randomUUID()
      env.deviceTokens.upsert(assignee, "tok-assignee", DevicePlatform.ANDROID)
      val prod = env.productionService.createProduction(owner, productionRequest)

      val task = env.taskService.create(
        owner,
        CreateTaskRequest(productionId = prod.id, title = "Lock script", assigneeUserId = assignee.toString())
      )

      // In-app record written for the assignee.
      assertEquals(1, env.notificationsRepo.notifications.count { it.userId == assignee })
      // Push sent to exactly the assignee's tokens, carrying the task id for deep-linking.
      assertEquals(1, env.pushSender.sent.size)
      val sent = env.pushSender.sent.single()
      assertEquals(listOf("tok-assignee"), sent.tokens)
      assertEquals(task.id, sent.data["taskId"])
      assertEquals(TaskAssignmentNotifier.TASK_ASSIGNED, sent.data["type"])
    }

  @Test
  fun `assigning a task to yourself does not notify or push`() =
    runBlocking {
      val env = TestAppEnv()
      val owner = UUID.randomUUID()
      env.deviceTokens.upsert(owner, "tok-owner", DevicePlatform.ANDROID)
      val prod = env.productionService.createProduction(owner, productionRequest)

      env.taskService.create(
        owner,
        CreateTaskRequest(productionId = prod.id, title = "Mine", assigneeUserId = owner.toString())
      )

      assertTrue(env.notificationsRepo.notifications.isEmpty())
      assertTrue(env.pushSender.sent.isEmpty())
    }

  @Test
  fun `creating an unassigned task does not notify or push`() =
    runBlocking {
      val env = TestAppEnv()
      val owner = UUID.randomUUID()
      val prod = env.productionService.createProduction(owner, productionRequest)

      env.taskService.create(owner, CreateTaskRequest(productionId = prod.id, title = "Open task"))

      assertTrue(env.notificationsRepo.notifications.isEmpty())
      assertTrue(env.pushSender.sent.isEmpty())
    }

  @Test
  fun `an assignee with no registered device still gets the in-app record`() =
    runBlocking {
      val env = TestAppEnv()
      val owner = UUID.randomUUID()
      val assignee = UUID.randomUUID()
      val prod = env.productionService.createProduction(owner, productionRequest)

      env.taskService.create(
        owner,
        CreateTaskRequest(productionId = prod.id, title = "T", assigneeUserId = assignee.toString())
      )

      assertEquals(1, env.notificationsRepo.notifications.count { it.userId == assignee })
      assertTrue(env.pushSender.sent.isEmpty(), "no tokens means nothing to push")
    }

  @Test
  fun `reassigning a task to a different user notifies and pushes to them`() =
    runBlocking {
      val env = TestAppEnv()
      val owner = UUID.randomUUID()
      val assignee = UUID.randomUUID()
      env.deviceTokens.upsert(assignee, "tok-assignee", DevicePlatform.ANDROID)
      val prod = env.productionService.createProduction(owner, productionRequest)
      // Created unassigned, so no notification yet.
      val task = env.taskService.create(owner, CreateTaskRequest(productionId = prod.id, title = "Open"))

      env.taskService.update(
        owner,
        UUID.fromString(task.id),
        UpdateTaskRequest(assigneeUserId = assignee.toString())
      )

      assertEquals(1, env.notificationsRepo.notifications.count { it.userId == assignee })
      assertEquals(1, env.pushSender.sent.size)
      val sent = env.pushSender.sent.single()
      assertEquals(listOf("tok-assignee"), sent.tokens)
      assertEquals(task.id, sent.data["taskId"])
    }

  @Test
  fun `updating a task without changing the assignee does not notify`() =
    runBlocking {
      val env = TestAppEnv()
      val owner = UUID.randomUUID()
      val assignee = UUID.randomUUID()
      env.deviceTokens.upsert(assignee, "tok-assignee", DevicePlatform.ANDROID)
      val prod = env.productionService.createProduction(owner, productionRequest)
      val task = env.taskService.create(
        owner,
        CreateTaskRequest(productionId = prod.id, title = "Open", assigneeUserId = assignee.toString())
      )
      // Assigning at creation already notified once; clear so we observe only the update.
      env.notificationsRepo.notifications.clear()
      env.pushSender.sent.clear()

      // A null assigneeId means "leave assignee unchanged" — no re-notification.
      env.taskService.update(owner, UUID.fromString(task.id), UpdateTaskRequest(title = "Renamed"))

      assertTrue(env.notificationsRepo.notifications.isEmpty())
      assertTrue(env.pushSender.sent.isEmpty())
    }

  @Test
  fun `getAttachment fails with NotFound when the blob is missing on disk`() =
    runBlocking {
      val env = TestAppEnv()
      val owner = UUID.randomUUID()
      val prod = env.productionService.createProduction(owner, productionRequest)
      // Row present but the blob was never written (storage key points at nothing) —
      // simulates metadata/blob drift. getAttachment must reject before any streaming.
      val task = env.tasks.create(
        productionId = UUID.fromString(prod.id),
        title = "Drifted",
        description = null,
        dueDate = null,
        assigneeUserId = null,
        attachment = NewAttachment("f.bin", "application/octet-stream", 3, "missing-key")
      )

      val error = assertFailsWith<AppException> { env.taskService.getAttachment(owner, task.id) }
      assertEquals(AppError.NotFound, error.error)
    }

  @Test
  fun `losing the idempotency insert race returns the winning task and deletes the orphan blob`() =
    runBlocking {
      val env = TestAppEnv()
      val owner = UUID.randomUUID()
      val prod = env.productionService.createProduction(owner, productionRequest)
      val productionId = UUID.fromString(prod.id)

      // Models the race window: the loser's *first* idempotency lookup misses (the
      // winner's row isn't visible yet), so it proceeds to insert and collides on
      // the unique key; the post-collision lookup then sees the winner.
      val racingRepo =
        object : FakeTaskRepository() {
          var lookups = 0

          override suspend fun findByIdempotencyKey(idempotencyKey: String): TaskRecord? {
            lookups++
            return if (lookups == 1) null else super.findByIdempotencyKey(idempotencyKey)
          }
        }
      // The winner has already persisted with this key.
      val winner =
        racingRepo.create(
          productionId = productionId,
          title = "Winner",
          description = null,
          dueDate = null,
          assigneeUserId = null,
          idempotencyKey = "key-1"
        )

      val service =
        TaskService(
          racingRepo,
          env.access,
          env.productionMembers,
          env.transactor,
          env.notificationsRepo,
          env.assignmentNotifier,
          env.fileStorage
        )

      // The loser arrives with the same key and a freshly stored attachment.
      val blob = env.fileStorage.store("hello".byteInputStream(), MAX_ATTACHMENT_BYTES)
      val orphan = NewAttachment("f.bin", "application/octet-stream", blob.sizeBytes, blob.storageKey)

      val result =
        service.create(
          owner,
          CreateTaskRequest(productionId = prod.id, title = "Loser"),
          attachment = orphan,
          idempotencyKey = "key-1"
        )

      assertEquals(winner.id.toString(), result.id, "must return the winning task, not 500")
      assertFalse(env.fileStorage.exists(blob.storageKey), "the loser's orphaned blob must be cleaned up")
    }

  @Test
  fun `create stores deduped participants alongside the task`() =
    runBlocking {
      val env = TestAppEnv()
      val owner = UUID.randomUUID()
      val prod = env.productionService.createProduction(owner, productionRequest)
      val crew = env.productionMembers.add(UUID.fromString(prod.id), UUID.randomUUID(), "Cre W", "Grip", null)
      val crewId = crew.userId.toString()

      val task = env.taskService.create(
        owner,
        CreateTaskRequest(
          productionId = prod.id,
          title = "Team task",
          // The same id twice must collapse to a single participant row.
          participantUserIds = listOf(crewId, crewId)
        )
      )

      assertEquals(listOf(crewId), task.participants.map { it.userId })
    }

  @Test
  fun `create rejects a participant who is not a member of the production`() =
    runBlocking {
      val env = TestAppEnv()
      val owner = UUID.randomUUID()
      val prod = env.productionService.createProduction(owner, productionRequest)

      val error = assertFailsWith<AppException> {
        env.taskService.create(
          owner,
          CreateTaskRequest(
            productionId = prod.id,
            title = "T",
            participantUserIds = listOf(UUID.randomUUID().toString())
          )
        )
      }

      assertTrue(error.error is AppError.ValidationError)
      assertTrue(env.tasks.tasks.isEmpty(), "the task must not be created when validation fails")
    }

  @Test
  fun `the task creator can replace the participant list`() =
    runBlocking {
      val env = TestAppEnv()
      val owner = UUID.randomUUID()
      val prod = env.productionService.createProduction(owner, productionRequest)
      val crew = env.productionMembers.add(UUID.fromString(prod.id), UUID.randomUUID(), "Cre W", "Grip", null)
      val task = env.taskService.create(owner, CreateTaskRequest(productionId = prod.id, title = "T"))

      val updated = env.taskService.updateParticipants(
        owner,
        UUID.fromString(task.id),
        UpdateTaskParticipantsRequest(participantUserIds = listOf(crew.userId.toString()))
      )

      assertEquals(listOf(crew.userId.toString()), updated.participants.map { it.userId })
    }

  @Test
  fun `the current assignee can replace the participant list`() =
    runBlocking {
      val env = TestAppEnv()
      val owner = UUID.randomUUID()
      val prod = env.productionService.createProduction(owner, productionRequest)
      val productionId = UUID.fromString(prod.id)
      val assigneeId = UUID.randomUUID()
      env.productionMembers.add(productionId, assigneeId, "Assig Nee", "VFX Supervisor", null)
      val task = env.taskService.create(
        owner,
        CreateTaskRequest(productionId = prod.id, title = "T", assigneeUserId = assigneeId.toString())
      )

      // The assignee may also list themself as a participant — deduped, harmless.
      val updated = env.taskService.updateParticipants(
        assigneeId,
        UUID.fromString(task.id),
        UpdateTaskParticipantsRequest(
          participantUserIds = listOf(assigneeId.toString(), assigneeId.toString())
        )
      )

      assertEquals(listOf(assigneeId.toString()), updated.participants.map { it.userId })
    }

  @Test
  fun `a production member who is neither creator nor assignee cannot modify participants`() =
    runBlocking {
      val env = TestAppEnv()
      val owner = UUID.randomUUID()
      val prod = env.productionService.createProduction(owner, productionRequest)
      val bystanderId = UUID.randomUUID()
      env.productionMembers.add(UUID.fromString(prod.id), bystanderId, "By Stander", "Gaffer", null)
      val task = env.taskService.create(owner, CreateTaskRequest(productionId = prod.id, title = "T"))

      val error = assertFailsWith<AppException> {
        env.taskService.updateParticipants(
          bystanderId,
          UUID.fromString(task.id),
          UpdateTaskParticipantsRequest(participantUserIds = listOf(bystanderId.toString()))
        )
      }

      assertEquals(AppError.Forbidden, error.error)
    }

  @Test
  fun `updateParticipants rejects ids outside the production membership`() =
    runBlocking {
      val env = TestAppEnv()
      val owner = UUID.randomUUID()
      val prod = env.productionService.createProduction(owner, productionRequest)
      val task = env.taskService.create(owner, CreateTaskRequest(productionId = prod.id, title = "T"))

      val error = assertFailsWith<AppException> {
        env.taskService.updateParticipants(
          owner,
          UUID.fromString(task.id),
          UpdateTaskParticipantsRequest(participantUserIds = listOf(UUID.randomUUID().toString()))
        )
      }

      assertTrue(error.error is AppError.ValidationError)
      assertTrue(env.tasks.tasks.single().participants.isEmpty(), "participants must stay unchanged")
    }

  @Test
  fun `patch update with participants enforces the creator-or-assignee rule`() =
    runBlocking {
      val env = TestAppEnv()
      val owner = UUID.randomUUID()
      val prod = env.productionService.createProduction(owner, productionRequest)
      val bystanderId = UUID.randomUUID()
      env.productionMembers.add(UUID.fromString(prod.id), bystanderId, "By Stander", "Gaffer", null)
      val task = env.taskService.create(owner, CreateTaskRequest(productionId = prod.id, title = "T"))

      val error = assertFailsWith<AppException> {
        env.taskService.update(
          bystanderId,
          UUID.fromString(task.id),
          UpdateTaskRequest(participantUserIds = listOf(bystanderId.toString()))
        )
      }
      assertEquals(AppError.Forbidden, error.error)

      // Omitting the field (null) leaves participants untouched and is open to any member.
      env.taskService.update(bystanderId, UUID.fromString(task.id), UpdateTaskRequest(title = "Renamed"))
      assertEquals("Renamed", env.tasks.tasks.single().title)
    }

  @Test
  fun `reassigning a task to yourself does not notify`() =
    runBlocking {
      val env = TestAppEnv()
      val owner = UUID.randomUUID()
      env.deviceTokens.upsert(owner, "tok-owner", DevicePlatform.ANDROID)
      val prod = env.productionService.createProduction(owner, productionRequest)
      val task = env.taskService.create(owner, CreateTaskRequest(productionId = prod.id, title = "Open"))

      env.taskService.update(
        owner,
        UUID.fromString(task.id),
        UpdateTaskRequest(assigneeUserId = owner.toString())
      )

      assertTrue(env.notificationsRepo.notifications.isEmpty())
      assertTrue(env.pushSender.sent.isEmpty())
    }
}
