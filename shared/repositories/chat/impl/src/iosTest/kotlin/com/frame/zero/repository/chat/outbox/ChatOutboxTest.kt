package com.frame.zero.repository.chat.outbox

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import app.cash.turbine.test
import com.frame.zero.core.logging.LoggerImpl
import com.frame.zero.database.FrameZeroDatabase
import com.frame.zero.domain.OfflineException
import com.frame.zero.domain.chat.PendingMessageStatus
import com.frame.zero.repository.chat.network.FakeChatApi
import com.frame.zero.testing.FakeConnectivityObserver
import com.frame.zero.testing.responseException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The drain against a real in-memory Room DB, so the outbox SQL — the `NOT EXISTS` in-flight guard,
 * the compare-and-set claim, the sequence ordering and the clear-pending transaction — is actually
 * executed rather than mimicked by a fake.
 *
 * Lives in `iosTest` (not `commonTest`) because the no-arg in-memory Room builder is only available
 * off-Android; the code under test is common either way. Mirrors [ProductionsRepositoryImplTest].
 */
class ChatOutboxTest {
  private val database: FrameZeroDatabase =
    Room.inMemoryDatabaseBuilder<FrameZeroDatabase>()
      .setDriver(BundledSQLiteDriver())
      // Unconfined so Room work runs inline on the test scheduler: a drain launched into a
      // background scope would otherwise still be running when advanceUntilIdle() returns.
      .setQueryCoroutineContext(Dispatchers.Unconfined)
      .build()

  private val store = ChatOutboxStore(database.chatOutboxDao())
  private val api = FakeChatApi()
  private val scheduler = RecordingChatOutboxScheduler()
  private val connectivity = FakeConnectivityObserver()
  private val outbox = ChatOutbox(
    store = store,
    api = api,
    chatDao = database.chatDao(),
    scheduler = scheduler,
    connectivityObserver = connectivity,
    logger = LoggerImpl(emptyList())
  )

  @AfterTest
  fun tearDown() = database.close()

  @Test
  fun `messages are sent in compose order and land as confirmed rows`() =
    runTest {
      store.enqueue(CONVERSATION, "a", "first")
      store.enqueue(CONVERSATION, "b", "second")
      store.enqueue(CONVERSATION, "c", "third")

      assertTrue(outbox.drain(CONVERSATION))

      assertEquals(listOf("first", "second", "third"), api.sentBodies)
      // Ordinals are assigned on arrival, so compose order is only preserved if the drain was serial.
      assertEquals(3L, database.chatDao().maxOrdinal(CONVERSATION))
      assertEquals(emptyList(), store.pendingIds())
    }

  @Test
  fun `offline stops the drain without spending an attempt and keeps the head in place`() =
    runTest {
      store.enqueue(CONVERSATION, "a", "first")
      store.enqueue(CONVERSATION, "b", "second")
      api.failures["first"] = OfflineException()

      assertFalse(outbox.drain(CONVERSATION), "work is left, so the caller must re-kick")

      assertEquals(emptyList(), api.sentBodies)
      assertEquals(PendingMessageStatus.Queued, store.get(CONVERSATION, "a")?.status)
      // No network is not a delivery failure, so it must not burn toward the Failed cap.
      assertEquals(0, store.get(CONVERSATION, "a")?.attemptCount)
      // "second" must not overtake it — that is the whole point of stopping.
      assertEquals(listOf("a", "b"), store.pendingIds())
      assertEquals("a", store.nextQueued(CONVERSATION)?.clientMessageId)
    }

  @Test
  fun `a server error counts an attempt then stops and keeps the head in place`() =
    runTest {
      store.enqueue(CONVERSATION, "a", "first")
      store.enqueue(CONVERSATION, "b", "second")
      api.failures["first"] = responseException(HttpStatusCode.InternalServerError)

      assertFalse(outbox.drain(CONVERSATION))

      assertEquals(PendingMessageStatus.Queued, store.get(CONVERSATION, "a")?.status)
      assertEquals(1, store.get(CONVERSATION, "a")?.attemptCount)
      assertEquals("a", store.nextQueued(CONVERSATION)?.clientMessageId)
    }

  @Test
  fun `a permanently rejected message is parked and the rest of the queue still drains`() =
    runTest {
      store.enqueue(CONVERSATION, "a", "first")
      store.enqueue(CONVERSATION, "b", "second")
      api.failures["first"] = responseException(HttpStatusCode.Forbidden)

      assertTrue(outbox.drain(CONVERSATION))

      assertEquals(listOf("second"), api.sentBodies)
      assertEquals(PendingMessageStatus.Failed, store.get(CONVERSATION, "a")?.status)
      assertNull(store.get(CONVERSATION, "b"), "the delivered message leaves the outbox")
    }

  @Test
  fun `a message the server keeps rejecting is eventually parked instead of blocking forever`() =
    runTest {
      store.enqueue(CONVERSATION, "a", "first")
      store.enqueue(CONVERSATION, "b", "second")
      api.failures["first"] = responseException(HttpStatusCode.InternalServerError)

      repeat(MAX_ATTEMPTS) { outbox.drain(CONVERSATION) }

      assertEquals(PendingMessageStatus.Failed, store.get(CONVERSATION, "a")?.status)
      // Head-of-line released: the next drain gets the queue moving again.
      assertTrue(outbox.drain(CONVERSATION))
      assertEquals(listOf("second"), api.sentBodies)
    }

  @Test
  fun `a send interrupted by process death is recovered and retried`() =
    runTest {
      store.enqueue(CONVERSATION, "a", "first")
      // What a killed process leaves behind: claimed, never answered.
      store.claim(CONVERSATION, "a")

      assertTrue(outbox.drain(CONVERSATION))

      assertEquals(listOf("first"), api.sentBodies)
    }

  @Test
  fun `a send stranded after an earlier drain is still recovered later in the same process`() =
    runTest {
      // An earlier drain already ran this session — the recovery must not be a once-per-process
      // sweep, or a message stranded afterwards (a cancelled worker) blocks the queue until restart.
      outbox.drain(CONVERSATION)

      store.enqueue(CONVERSATION, "a", "first")
      store.claim(CONVERSATION, "a")

      assertTrue(outbox.drain(CONVERSATION))
      assertEquals(listOf("first"), api.sentBodies)
    }

  @Test
  fun `drain all recovers a conversation whose only row was stranded in flight`() =
    runTest {
      store.enqueue(CONVERSATION, "a", "first")
      // No Queued row remains, only a stranded Sending one — the flush must still find it.
      store.claim(CONVERSATION, "a")

      outbox.drainAll()

      assertEquals(listOf("first"), api.sentBodies)
    }

  @Test
  fun `draining all conversations covers every one with queued messages`() =
    runTest {
      store.enqueue(CONVERSATION, "a", "first")
      store.enqueue(OTHER_CONVERSATION, "b", "second")

      outbox.drainAll()

      assertEquals(listOf("first", "second"), api.sentBodies)
    }

  @Test
  fun `a user retry hands back the full attempt budget`() =
    runTest {
      store.enqueue(CONVERSATION, "a", "first")
      api.failures["first"] = responseException(HttpStatusCode.InternalServerError)
      repeat(MAX_ATTEMPTS) { outbox.drain(CONVERSATION) }
      assertEquals(PendingMessageStatus.Failed, store.get(CONVERSATION, "a")?.status)

      store.retry(CONVERSATION, "a")

      // Without the reset the very next failure would re-park it, making the retry button one
      // request deep.
      assertEquals(0, store.get(CONVERSATION, "a")?.attemptCount)
      assertFalse(outbox.drain(CONVERSATION), "the retry gets the backoff loop, not an instant park")
      assertEquals(PendingMessageStatus.Queued, store.get(CONVERSATION, "a")?.status)
    }

  @Test
  fun `kick runs one delivery loop per conversation`() =
    runTest {
      val outbox = testOutbox()
      store.enqueue(CONVERSATION, "a", "first")
      store.enqueue(CONVERSATION, "b", "second")
      api.failures["first"] = responseException(HttpStatusCode.InternalServerError)

      // Three sends in a row would otherwise start three loops sharing one message's budget and
      // park it almost immediately instead of over the backoff schedule.
      outbox.kick(CONVERSATION)
      outbox.kick(CONVERSATION)
      outbox.kick(CONVERSATION)
      runCurrent()

      assertEquals(1, store.get(CONVERSATION, "a")?.attemptCount)
      outbox.shutdown()
    }

  @Test
  fun `shutdown stops delivery so a sign-out wipe is not written over`() =
    runTest {
      val outbox = testOutbox()
      store.enqueue(CONVERSATION, "a", "first")
      api.holdSends()
      outbox.kick(CONVERSATION)
      runCurrent()

      outbox.shutdown()
      api.releaseSends()
      runCurrent()

      // The send was cancelled in flight, so nothing landed — the row would otherwise have been
      // written back after the signed-out user's data was wiped.
      assertNull(database.chatDao().maxOrdinal(CONVERSATION))
    }

  /** An outbox whose delivery coroutines run on the test scheduler rather than a real dispatcher. */
  private fun TestScope.testOutbox() =
    ChatOutbox(
      store = store,
      api = api,
      chatDao = database.chatDao(),
      scheduler = scheduler,
      connectivityObserver = connectivity,
      logger = LoggerImpl(emptyList()),
      scope = backgroundScope
    )

  /** Client ids still in the outbox for [CONVERSATION], in queue order. */
  private suspend fun ChatOutboxStore.pendingIds(): List<String> {
    var ids: List<String> = emptyList()
    observe(CONVERSATION).test {
      ids = awaitItem().map { it.clientMessageId }
    }
    return ids
  }

  private companion object {
    const val CONVERSATION = "conversation-1"
    const val OTHER_CONVERSATION = "conversation-2"
    const val MAX_ATTEMPTS = 5
  }
}
