package com.frame.zero.chat

import com.frame.zero.auth.UserRepositoryImpl
import com.frame.zero.common.testing.PostgresTestDatabase
import com.frame.zero.domain.production.Genre
import com.frame.zero.domain.production.ProductionPhase
import com.frame.zero.production.ProductionRepositoryImpl
import com.frame.zero.task.TaskRepositoryImpl
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import java.util.UUID
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ConversationRepositoryImplTest {
  private val db = PostgresTestDatabase()
  private val users = UserRepositoryImpl()
  private val productions = ProductionRepositoryImpl()
  private val tasks = TaskRepositoryImpl()
  private val conversations = ConversationRepositoryImpl()

  @BeforeTest
  fun setUp() {
    db.start()
  }

  @AfterTest
  fun tearDown() {
    db.stop()
  }

  private suspend fun newProduction(ownerId: UUID) =
    productions.create(
      title = "Film",
      genre = Genre.DRAMA,
      logline = null,
      phase = ProductionPhase.IDEA,
      startDate = LocalDate(2026, 1, 1),
      wrapDate = LocalDate(2026, 12, 31),
      budgetCents = null,
      ownerUserId = ownerId
    )

  @Test
  fun `getOrCreateTaskConversation creates a TASK conversation once and is idempotent per task`() =
    runBlocking {
      val ownerId = users.create("owner@x.com", "h", "Own", "Er").id
      val prod = newProduction(ownerId)
      val task = tasks.create(prod.id, "Title", null, null, null)

      val created = conversations.getOrCreateTaskConversation(task.id, prod.id)
      assertEquals(ConversationKind.TASK, created.kind)
      assertEquals(task.id, created.taskId)
      assertEquals(prod.id, created.productionId)

      val again = conversations.getOrCreateTaskConversation(task.id, prod.id)
      assertEquals(created.id, again.id, "second get-or-create must return the same conversation")
    }

  @Test
  fun `findByTaskId and findById return the conversation, null for unknown ids`() =
    runBlocking {
      val ownerId = users.create("owner@x.com", "h", "Own", "Er").id
      val prod = newProduction(ownerId)
      val task = tasks.create(prod.id, "Title", null, null, null)
      val created = conversations.getOrCreateTaskConversation(task.id, prod.id)

      assertEquals(created.id, conversations.findByTaskId(task.id)?.id)
      assertEquals(created.id, conversations.findById(created.id)?.id)

      assertNull(conversations.findByTaskId(UUID.randomUUID()))
      assertNull(conversations.findById(UUID.randomUUID()))
    }

  @Test
  fun `findIdsByProductionId returns only that production's conversations`() =
    runBlocking {
      val ownerId = users.create("owner@x.com", "h", "Own", "Er").id
      val prod = newProduction(ownerId)
      val otherProd = newProduction(ownerId)
      val taskA = tasks.create(prod.id, "A", null, null, null)
      val taskB = tasks.create(prod.id, "B", null, null, null)
      val taskOther = tasks.create(otherProd.id, "C", null, null, null)
      val conversationA = conversations.getOrCreateTaskConversation(taskA.id, prod.id)
      val conversationB = conversations.getOrCreateTaskConversation(taskB.id, prod.id)
      conversations.getOrCreateTaskConversation(taskOther.id, otherProd.id)

      assertEquals(
        setOf(conversationA.id, conversationB.id),
        conversations.findIdsByProductionId(prod.id).toSet()
      )
      assertEquals(emptyList(), conversations.findIdsByProductionId(UUID.randomUUID()))
    }

  @Test
  fun `ensureParticipant is idempotent for the same conversation and user`() =
    runBlocking {
      val ownerId = users.create("owner@x.com", "h", "Own", "Er").id
      val participantId = users.create("crew@x.com", "h", "Cre", "W").id
      val prod = newProduction(ownerId)
      val task = tasks.create(prod.id, "Title", null, null, null)
      val conversation = conversations.getOrCreateTaskConversation(task.id, prod.id)

      conversations.ensureParticipant(conversation.id, participantId)
      // A second call must be a no-op (INSERT ... ON CONFLICT DO NOTHING), not throw.
      conversations.ensureParticipant(conversation.id, participantId)
    }
}
