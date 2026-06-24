package com.frame.zero.task

import com.frame.zero.AppError
import com.frame.zero.AppException
import com.frame.zero.common.testing.TestAppEnv
import com.frame.zero.domain.production.Genre
import com.frame.zero.dto.device.DevicePlatform
import com.frame.zero.dto.production.CreateProductionRequest
import com.frame.zero.dto.task.CreateTaskRequest
import com.frame.zero.dto.task.UpdateTaskRequest
import com.frame.zero.notification.TaskAssignmentNotifier
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
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
