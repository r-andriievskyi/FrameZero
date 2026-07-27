package com.frame.zero.repository.chat.outbox

import com.frame.zero.domain.chat.PendingMessageStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ChatOutboxStoreTest {
  private val dao = FakeChatOutboxDao()
  private val store = ChatOutboxStore(dao)

  @Test
  fun `next queued follows compose order`() =
    runTest {
      store.enqueue(CONVERSATION, "a", "first")
      store.enqueue(CONVERSATION, "b", "second")

      assertEquals("a", store.nextQueued(CONVERSATION)?.clientMessageId)

      store.remove(CONVERSATION, "a")
      assertEquals("b", store.nextQueued(CONVERSATION)?.clientMessageId)

      store.remove(CONVERSATION, "b")
      assertNull(store.nextQueued(CONVERSATION))
    }

  @Test
  fun `a conversation with a message in flight offers nothing else to send`() =
    runTest {
      store.enqueue(CONVERSATION, "a", "first")
      store.enqueue(CONVERSATION, "b", "second")

      store.claim(CONVERSATION, "a")

      assertNull(store.nextQueued(CONVERSATION))
    }

  @Test
  fun `only one claim of the same message wins`() =
    runTest {
      store.enqueue(CONVERSATION, "a", "first")

      assertTrue(store.claim(CONVERSATION, "a"))
      assertFalse(store.claim(CONVERSATION, "a"))
    }

  @Test
  fun `re-enqueueing a queued message leaves its position and attempts alone`() =
    runTest {
      store.enqueue(CONVERSATION, "a", "first")
      store.enqueue(CONVERSATION, "b", "second")
      store.claim(CONVERSATION, "a")
      store.requeue(CONVERSATION, "a")

      store.enqueue(CONVERSATION, "a", "first")

      assertEquals(1, store.get(CONVERSATION, "a")?.attemptCount)
      assertEquals("a", store.nextQueued(CONVERSATION)?.clientMessageId)
    }

  @Test
  fun `sends interrupted by process death are returned to the queue`() =
    runTest {
      store.enqueue(CONVERSATION, "a", "first")
      store.claim(CONVERSATION, "a")

      assertEquals(1, store.resetInFlight(CONVERSATION))
      assertEquals("a", store.nextQueued(CONVERSATION)?.clientMessageId)
    }

  @Test
  fun `resetting one conversation leaves another's in-flight send untouched`() =
    runTest {
      store.enqueue(CONVERSATION, "a", "first")
      store.enqueue(OTHER_CONVERSATION, "b", "second")
      store.claim(CONVERSATION, "a")
      store.claim(OTHER_CONVERSATION, "b")

      store.resetInFlight(CONVERSATION)

      assertEquals(PendingMessageStatus.Queued, store.get(CONVERSATION, "a")?.status)
      assertEquals(PendingMessageStatus.Sending, store.get(OTHER_CONVERSATION, "b")?.status)
    }

  @Test
  fun `requeue counts the attempt and returns the message to the queue`() =
    runTest {
      store.enqueue(CONVERSATION, "a", "first")
      store.claim(CONVERSATION, "a")

      store.requeue(CONVERSATION, "a")

      assertEquals(PendingMessageStatus.Queued, store.get(CONVERSATION, "a")?.status)
      assertEquals(1, store.get(CONVERSATION, "a")?.attemptCount)
    }

  @Test
  fun `a failed message stays out of the queue until it is retried`() =
    runTest {
      store.enqueue(CONVERSATION, "a", "first")
      store.claim(CONVERSATION, "a")
      store.markFailed(CONVERSATION, "a")

      assertEquals(PendingMessageStatus.Failed, store.get(CONVERSATION, "a")?.status)
      assertNull(store.nextQueued(CONVERSATION))

      store.retry(CONVERSATION, "a")

      assertEquals("a", store.nextQueued(CONVERSATION)?.clientMessageId)
      // The retry keeps the id, so the resend is deduped server-side rather than duplicated.
      assertEquals("first", store.get(CONVERSATION, "a")?.body)
    }

  @Test
  fun `the same client id in another conversation is a separate message`() =
    runTest {
      store.enqueue(CONVERSATION, "a", "here")
      store.enqueue(OTHER_CONVERSATION, "a", "elsewhere")

      assertEquals("here", store.get(CONVERSATION, "a")?.body)
      assertEquals("elsewhere", store.get(OTHER_CONVERSATION, "a")?.body)
    }

  @Test
  fun `conversations with pending cover queued and stranded-in-flight rows`() =
    runTest {
      store.enqueue(CONVERSATION, "a", "first")
      store.enqueue(OTHER_CONVERSATION, "b", "second")
      // A row left Sending by a killed process must still be surfaced so a flush can recover it.
      store.claim(OTHER_CONVERSATION, "b")

      assertEquals(listOf(CONVERSATION, OTHER_CONVERSATION), store.conversationsWithPending())
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
