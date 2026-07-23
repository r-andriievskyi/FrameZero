package com.frame.zero.repository.chat.outbox

import com.frame.zero.domain.chat.PendingMessageStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlin.time.Instant

class ChatOutboxStoreTest {
  private val dao = FakeChatOutboxDao()

  // Every enqueue lands one second after the previous one, so ordering is unambiguous.
  private var tick = 0L
  private val clock = object : Clock {
    override fun now(): Instant = Instant.fromEpochMilliseconds(tick++ * 1_000)
  }
  private val store = ChatOutboxStore(dao, clock)

  @Test
  fun `next queued follows compose order`() =
    runTest {
      store.enqueue(CONVERSATION, "a", "first")
      store.enqueue(CONVERSATION, "b", "second")

      assertEquals("a", store.nextQueued(CONVERSATION)?.clientMessageId)

      store.remove("a")
      assertEquals("b", store.nextQueued(CONVERSATION)?.clientMessageId)

      store.remove("b")
      assertNull(store.nextQueued(CONVERSATION))
    }

  @Test
  fun `a sending message is no longer offered as the next queued one`() =
    runTest {
      store.enqueue(CONVERSATION, "a", "first")
      store.enqueue(CONVERSATION, "b", "second")

      store.markSending("a")

      assertEquals("b", store.nextQueued(CONVERSATION)?.clientMessageId)
    }

  @Test
  fun `requeue counts the attempt and returns the message to the queue`() =
    runTest {
      store.enqueue(CONVERSATION, "a", "first")
      store.markSending("a")

      store.requeue("a")

      assertEquals(PendingMessageStatus.Queued, store.get("a")?.status)
      assertEquals(1, dao.rows.value.single().attemptCount)
    }

  @Test
  fun `a failed message stays out of the queue until it is retried`() =
    runTest {
      store.enqueue(CONVERSATION, "a", "first")
      store.markSending("a")
      store.markFailed("a")

      assertEquals(PendingMessageStatus.Failed, store.get("a")?.status)
      assertNull(store.nextQueued(CONVERSATION))

      store.retry("a")

      assertEquals("a", store.nextQueued(CONVERSATION)?.clientMessageId)
      // The retry keeps the id, so the resend is deduped server-side rather than duplicated.
      assertEquals("first", store.get("a")?.body)
    }

  @Test
  fun `conversations with queued messages skip drained ones`() =
    runTest {
      store.enqueue(CONVERSATION, "a", "first")
      store.enqueue(OTHER_CONVERSATION, "b", "second")
      store.markSending("b")

      assertEquals(listOf(CONVERSATION), store.conversationsWithQueued())
    }

  @Test
  fun `observe emits a conversation's messages oldest first`() =
    runTest {
      store.enqueue(CONVERSATION, "a", "first")
      store.enqueue(OTHER_CONVERSATION, "x", "elsewhere")
      store.enqueue(CONVERSATION, "b", "second")

      val pending = store.observe(CONVERSATION).first()

      assertEquals(listOf("a", "b"), pending.map { it.clientMessageId })
    }

  private companion object {
    const val CONVERSATION = "conversation-1"
    const val OTHER_CONVERSATION = "conversation-2"
  }
}
