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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MessageRepositoryImplTest {
  private val db = PostgresTestDatabase()
  private val users = UserRepositoryImpl()
  private val productions = ProductionRepositoryImpl()
  private val tasks = TaskRepositoryImpl()
  private val conversations = ConversationRepositoryImpl()
  private val messages = MessageRepositoryImpl()

  private lateinit var conversationId: UUID
  private lateinit var senderId: UUID
  private lateinit var prodId: UUID

  @BeforeTest
  fun setUp() {
    db.start()
    runBlocking {
      val ownerId = users.create("owner@x.com", "h", "Own", "Er").id
      senderId = users.create("sender@x.com", "h", "Sen", "Der").id
      val prod = productions.create(
        title = "Film",
        genre = Genre.DRAMA,
        logline = null,
        phase = ProductionPhase.IDEA,
        startDate = LocalDate(2026, 1, 1),
        wrapDate = LocalDate(2026, 12, 31),
        budgetCents = null,
        ownerUserId = ownerId
      )
      val task = tasks.create(prod.id, "Title", null, null, null)
      conversationId = conversations.getOrCreateTaskConversation(task.id, prod.id).id
      prodId = prod.id
    }
  }

  @AfterTest
  fun tearDown() {
    db.stop()
  }

  @Test
  fun `append assigns a per-conversation seq starting at one and incrementing`() =
    runBlocking {
      val first = messages.append(conversationId, senderId, "one", "c1")
      val second = messages.append(conversationId, senderId, "two", "c2")
      val third = messages.append(conversationId, senderId, "three", "c3")

      assertEquals(1L, first.message.seq)
      assertEquals(2L, second.message.seq)
      assertEquals(3L, third.message.seq)
      assertTrue(first.isNew)
      assertTrue(second.isNew)
      assertTrue(third.isNew)
    }

  @Test
  fun `append is idempotent on conversation, sender and clientMessageId and does not advance seq`() =
    runBlocking {
      val first = messages.append(conversationId, senderId, "hello", "c1")
      val retry = messages.append(conversationId, senderId, "hello", "c1")

      assertEquals(first.message.id, retry.message.id, "retry must return the same message")
      assertEquals(first.message.seq, retry.message.seq, "retry must not advance seq")
      assertFalse(retry.isNew, "a replayed clientMessageId must not be reported as new")

      // The next new client id gets the next seq, proving the retry consumed none.
      val next = messages.append(conversationId, senderId, "world", "c2")
      assertEquals(first.message.seq + 1, next.message.seq)
    }

  @Test
  fun `append with the same clientMessageId in a different conversation is not treated as a replay`() =
    runBlocking {
      val otherTask = tasks.create(prodId, "Other", null, null, null)
      val otherConversationId = conversations.getOrCreateTaskConversation(otherTask.id, prodId).id

      val first = messages.append(conversationId, senderId, "hello", "c1")
      val second = messages.append(otherConversationId, senderId, "hello elsewhere", "c1")

      assertTrue(second.isNew, "the same clientMessageId in a different conversation must persist a new message")
      assertEquals(1L, second.message.seq, "seq is per-conversation")
      assertEquals(1, messages.findByConversation(otherConversationId, beforeSeq = null, limit = 10).size)
      assertEquals(1, messages.findByConversation(conversationId, beforeSeq = null, limit = 10).size)
    }

  @Test
  fun `findByConversation returns messages newest-first`() =
    runBlocking {
      messages.append(conversationId, senderId, "one", "c1")
      messages.append(conversationId, senderId, "two", "c2")
      messages.append(conversationId, senderId, "three", "c3")

      val page = messages.findByConversation(conversationId, beforeSeq = null, limit = 10)
      assertEquals(listOf(3L, 2L, 1L), page.map { it.seq })
    }

  @Test
  fun `findByConversation with beforeSeq returns only earlier messages, newest-first`() =
    runBlocking {
      messages.append(conversationId, senderId, "one", "c1")
      messages.append(conversationId, senderId, "two", "c2")
      messages.append(conversationId, senderId, "three", "c3")

      val page = messages.findByConversation(conversationId, beforeSeq = 3L, limit = 10)
      assertEquals(listOf(2L, 1L), page.map { it.seq }, "beforeSeq is exclusive")
    }

  @Test
  fun `findByConversation respects the limit`() =
    runBlocking {
      messages.append(conversationId, senderId, "one", "c1")
      messages.append(conversationId, senderId, "two", "c2")
      messages.append(conversationId, senderId, "three", "c3")

      val page = messages.findByConversation(conversationId, beforeSeq = null, limit = 2)
      assertEquals(listOf(3L, 2L), page.map { it.seq })
    }
}
