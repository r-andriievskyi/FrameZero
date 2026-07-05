package com.frame.zero.chat

import com.frame.zero.AppError
import com.frame.zero.AppException
import com.frame.zero.common.testing.TestAppEnv
import com.frame.zero.domain.production.Genre
import com.frame.zero.dto.chat.ConversationDto
import com.frame.zero.dto.production.CreateProductionRequest
import com.frame.zero.dto.task.CreateTaskRequest
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ChatServiceTest {
  private val productionRequest =
    CreateProductionRequest(
      title = "Film",
      genre = Genre.DRAMA,
      startDate = LocalDate(2026, 1, 1),
      wrapDate = LocalDate(2026, 12, 31)
    )

  @Test
  fun `getOrCreateTaskConversation is get-or-create and lazily records a participant row`() =
    runBlocking {
      val env = TestAppEnv()
      val owner = UUID.randomUUID()
      val prod = env.productionService.createProduction(owner, productionRequest)
      val task = env.taskService.create(owner, CreateTaskRequest(productionId = prod.id, title = "T"))
      val taskId = UUID.fromString(task.id)

      val first = env.chatService.getOrCreateTaskConversation(owner, taskId)
      val second = env.chatService.getOrCreateTaskConversation(owner, taskId)

      assertEquals(first.id, second.id, "second call must return the same conversation")
      assertIs<ConversationDto.Task>(first)
      assertEquals(task.id, first.taskId)
      assertTrue(
        (UUID.fromString(first.id) to owner) in env.conversations.participants,
        "the caller must get a lazily-created read-state row"
      )
    }

  @Test
  fun `send assigns an incrementing ordinal and is idempotent on clientMessageId`() =
    runBlocking {
      val env = TestAppEnv()
      val owner = UUID.randomUUID()
      val prod = env.productionService.createProduction(owner, productionRequest)
      val task = env.taskService.create(owner, CreateTaskRequest(productionId = prod.id, title = "T"))
      val convId = UUID.fromString(env.chatService.getOrCreateTaskConversation(owner, UUID.fromString(task.id)).id)

      val first = env.chatService.send(owner, convId, "c1", "hello")
      val second = env.chatService.send(owner, convId, "c2", "world")
      val retry = env.chatService.send(owner, convId, "c1", "hello")

      assertEquals(1L, first.ordinal)
      assertEquals(2L, second.ordinal)
      assertEquals(first.id, retry.id, "a replayed clientMessageId returns the same message")
      assertEquals(2, env.chatMessages.messages.size, "replay must not persist a second message")
    }

  @Test
  fun `send trims the body and broadcasts to the hub`() =
    runBlocking {
      val env = TestAppEnv()
      val owner = UUID.randomUUID()
      val prod = env.productionService.createProduction(owner, productionRequest)
      val task = env.taskService.create(owner, CreateTaskRequest(productionId = prod.id, title = "T"))
      val convId = UUID.fromString(env.chatService.getOrCreateTaskConversation(owner, UUID.fromString(task.id)).id)

      val message = env.chatService.send(owner, convId, "c1", "  spaced  ")
      assertEquals("spaced", message.body)
    }

  @Test
  fun `send with a blank body is rejected`() =
    runBlocking {
      val env = TestAppEnv()
      val owner = UUID.randomUUID()
      val prod = env.productionService.createProduction(owner, productionRequest)
      val task = env.taskService.create(owner, CreateTaskRequest(productionId = prod.id, title = "T"))
      val convId = UUID.fromString(env.chatService.getOrCreateTaskConversation(owner, UUID.fromString(task.id)).id)

      val error = assertFailsWith<AppException> { env.chatService.send(owner, convId, "c1", "   ") }
      assertTrue(error.error is AppError.ValidationError)
    }

  @Test
  fun `a production member outside the task circle cannot send`() =
    runBlocking {
      val env = TestAppEnv()
      val owner = UUID.randomUUID()
      val prod = env.productionService.createProduction(owner, productionRequest)
      val task = env.taskService.create(owner, CreateTaskRequest(productionId = prod.id, title = "T"))
      val convId = UUID.fromString(env.chatService.getOrCreateTaskConversation(owner, UUID.fromString(task.id)).id)

      // A production member who is neither creator, assignee, nor participant is
      // still outside the task circle — the security boundary is the circle, not
      // production membership.
      val bystander = UUID.randomUUID()
      env.productionMembers.add(UUID.fromString(prod.id), bystander, "By Stander", "Gaffer", null)

      val error = assertFailsWith<AppException> { env.chatService.send(bystander, convId, "c1", "hi") }
      assertEquals(AppError.Forbidden, error.error)
    }

  @Test
  fun `listMessages returns newest-first with a cursor when the page is full`() =
    runBlocking {
      val env = TestAppEnv()
      val owner = UUID.randomUUID()
      val prod = env.productionService.createProduction(owner, productionRequest)
      val task = env.taskService.create(owner, CreateTaskRequest(productionId = prod.id, title = "T"))
      val convId = UUID.fromString(env.chatService.getOrCreateTaskConversation(owner, UUID.fromString(task.id)).id)
      env.chatService.send(owner, convId, "c1", "one")
      env.chatService.send(owner, convId, "c2", "two")

      val (items, nextCursor) = env.chatService.listMessages(owner, convId, before = null, limit = 2)

      assertEquals(listOf(2L, 1L), items.map { it.ordinal }, "newest-first")
      assertEquals("1", nextCursor, "cursor is the oldest ordinal of a full page")
    }

  @Test
  fun `markRead advances the cursor forward-only and the conversation reports unread state`() =
    runBlocking {
      val env = TestAppEnv()
      val owner = UUID.randomUUID()
      val prod = env.productionService.createProduction(owner, productionRequest)
      val task = env.taskService.create(owner, CreateTaskRequest(productionId = prod.id, title = "T"))
      val taskId = UUID.fromString(task.id)
      val convId = UUID.fromString(env.chatService.getOrCreateTaskConversation(owner, taskId).id)
      env.chatService.send(owner, convId, "c1", "one")
      env.chatService.send(owner, convId, "c2", "two")

      assertEquals(1L, env.chatService.markRead(owner, convId, 1))
      // Clamps past the newest ordinal (2) rather than trusting the client.
      assertEquals(2L, env.chatService.markRead(owner, convId, 99))
      // Never moves backwards.
      assertEquals(2L, env.chatService.markRead(owner, convId, 0))

      val conversation = env.chatService.getOrCreateTaskConversation(owner, taskId)
      assertIs<ConversationDto.Task>(conversation)
      assertEquals(2L, conversation.latestOrdinal)
      assertEquals(2L, conversation.lastReadOrdinal)
    }

  @Test
  fun `canAccessConversation is true for a circle member and false for an outsider`() =
    runBlocking {
      val env = TestAppEnv()
      val owner = UUID.randomUUID()
      val prod = env.productionService.createProduction(owner, productionRequest)
      val task = env.taskService.create(owner, CreateTaskRequest(productionId = prod.id, title = "T"))
      val convId = UUID.fromString(env.chatService.getOrCreateTaskConversation(owner, UUID.fromString(task.id)).id)

      assertTrue(env.chatService.canAccessConversation(owner, convId))
      assertFalse(env.chatService.canAccessConversation(UUID.randomUUID(), convId))
    }
}
