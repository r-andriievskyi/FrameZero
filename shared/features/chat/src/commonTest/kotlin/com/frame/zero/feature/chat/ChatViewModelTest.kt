package com.frame.zero.feature.chat

import com.frame.zero.core.session.UserCache
import com.frame.zero.domain.User
import com.frame.zero.domain.chat.Conversation
import com.frame.zero.domain.chat.PendingChatMessage
import com.frame.zero.domain.chat.PendingMessageStatus
import com.frame.zero.feature.chat.domain.DiscardPendingMessageUseCase
import com.frame.zero.feature.chat.domain.EnqueueMessageUseCase
import com.frame.zero.feature.chat.domain.GetCurrentUserIdUseCase
import com.frame.zero.feature.chat.domain.MarkReadUseCase
import com.frame.zero.feature.chat.domain.ObservePendingMessagesUseCase
import com.frame.zero.feature.chat.domain.OpenConversationUseCase
import com.frame.zero.feature.chat.domain.RetryPendingMessageUseCase
import com.frame.zero.testing.FakeChatRepository
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class ChatViewModelTest {
  private val conversation = Conversation(
    id = CONVERSATION_ID,
    taskId = TASK_ID,
    productionId = "p1",
    createdAt = Instant.fromEpochMilliseconds(0),
    latestOrdinal = 0,
    lastReadOrdinal = 0
  )

  @Test
  fun `isDisconnected tracks the repository's connection state`() =
    runTest {
      val repo = FakeChatRepository(conversation)
      val viewModel = makeViewModel(this, repo)
      advanceUntilIdle()
      assertFalse(viewModel.state.value.isDisconnected)

      repo.connectionState.value = false
      advanceUntilIdle()
      assertTrue(viewModel.state.value.isDisconnected)

      repo.connectionState.value = true
      advanceUntilIdle()
      assertFalse(viewModel.state.value.isDisconnected)
    }

  @Test
  fun `sending clears the composer immediately and queues the trimmed body`() =
    runTest {
      val repo = FakeChatRepository(conversation)
      val viewModel = makeViewModel(this, repo)
      advanceUntilIdle()

      viewModel.onIntent(ChatIntent.MessageChanged("  hello crew  "))
      viewModel.onIntent(ChatIntent.SendClicked)

      // Cleared before the enqueue coroutine runs: composing never waits on storage or network.
      assertEquals("", viewModel.state.value.draft)

      advanceUntilIdle()
      val (conversationId, _, body) = repo.enqueued.single()
      assertEquals(CONVERSATION_ID, conversationId)
      assertEquals("hello crew", body)
    }

  @Test
  fun `each send gets its own idempotency key`() =
    runTest {
      val repo = FakeChatRepository(conversation)
      val viewModel = makeViewModel(this, repo)
      advanceUntilIdle()

      viewModel.onIntent(ChatIntent.MessageChanged("first"))
      viewModel.onIntent(ChatIntent.SendClicked)
      viewModel.onIntent(ChatIntent.MessageChanged("second"))
      viewModel.onIntent(ChatIntent.SendClicked)
      advanceUntilIdle()

      val ids = repo.enqueued.map { it.second }
      assertEquals(2, ids.distinct().size, "reusing a key would make the server dedupe the second send")
    }

  @Test
  fun `a draft that could not be queued is put back in the composer`() =
    runTest {
      val repo = FakeChatRepository(conversation).apply { enqueueFailure = IllegalStateException("disk") }
      val viewModel = makeViewModel(this, repo)
      advanceUntilIdle()

      viewModel.onIntent(ChatIntent.MessageChanged("hello crew"))
      viewModel.onIntent(ChatIntent.SendClicked)
      advanceUntilIdle()

      // Nothing queued means no pending bubble and nothing to retry, so the text must not vanish.
      assertEquals("hello crew", viewModel.state.value.draft)
    }

  @Test
  fun `a draft typed while a failing send is in flight is not overwritten`() =
    runTest {
      val repo = FakeChatRepository(conversation).apply { enqueueFailure = IllegalStateException("disk") }
      val viewModel = makeViewModel(this, repo)
      advanceUntilIdle()

      viewModel.onIntent(ChatIntent.MessageChanged("hello crew"))
      viewModel.onIntent(ChatIntent.SendClicked)
      viewModel.onIntent(ChatIntent.MessageChanged("something else"))
      advanceUntilIdle()

      assertEquals("something else", viewModel.state.value.draft)
    }

  @Test
  fun `a blank draft is not sent`() =
    runTest {
      val repo = FakeChatRepository(conversation)
      val viewModel = makeViewModel(this, repo)
      advanceUntilIdle()

      viewModel.onIntent(ChatIntent.MessageChanged("   "))
      viewModel.onIntent(ChatIntent.SendClicked)
      advanceUntilIdle()

      assertTrue(repo.enqueued.isEmpty())
      assertFalse(viewModel.state.value.canSend)
    }

  @Test
  fun `pending messages reach the state newest first`() =
    runTest {
      val repo = FakeChatRepository(conversation)
      val viewModel = makeViewModel(this, repo)
      advanceUntilIdle()

      repo.pending.value = listOf(
        pendingMessage("a", "first", PendingMessageStatus.Queued),
        pendingMessage("b", "second", PendingMessageStatus.Failed)
      )
      advanceUntilIdle()

      val pending = viewModel.state.value.pending
      assertEquals(listOf("b", "a"), pending.map { it.clientMessageId })
      assertEquals(listOf(true, false), pending.map { it.isFailed })
    }

  @Test
  fun `pending messages carry a formatted timestamp and day`() =
    runTest {
      val repo = FakeChatRepository(conversation)
      val viewModel = makeViewModel(this, repo)
      advanceUntilIdle()

      // 2026-07-24T09:05:00Z
      repo.pending.value = listOf(
        pendingMessage("a", "first", PendingMessageStatus.Queued, epochMs = 1_784_883_900_000)
      )
      advanceUntilIdle()

      val message = viewModel.state.value.pending.single()
      // Exact rendering (12h/24h, meridiem wording) is locale-dependent by design
      // (formatClockTime is expect/actual) — just check the hour/minute made it through.
      assertTrue(message.timeLabel.contains("9:05") || message.timeLabel.contains("09:05"))
      assertEquals(LocalDate(2026, 7, 24), message.day)
    }

  @Test
  fun `retry and discard reach the repository with the conversation id`() =
    runTest {
      val repo = FakeChatRepository(conversation)
      val viewModel = makeViewModel(this, repo)
      advanceUntilIdle()

      viewModel.onIntent(ChatIntent.RetryPending("a"))
      viewModel.onIntent(ChatIntent.DiscardPending("b"))
      advanceUntilIdle()

      assertEquals(listOf(CONVERSATION_ID to "a"), repo.retried)
      assertEquals(listOf(CONVERSATION_ID to "b"), repo.discarded)
    }

  private fun pendingMessage(
    clientMessageId: String,
    body: String,
    status: PendingMessageStatus,
    epochMs: Long = 0
  ) = PendingChatMessage(
    clientMessageId = clientMessageId,
    conversationId = CONVERSATION_ID,
    body = body,
    status = status,
    attemptCount = 0,
    createdAt = Instant.fromEpochMilliseconds(epochMs)
  )

  private fun makeViewModel(
    scope: TestScope,
    repo: FakeChatRepository
  ): ChatViewModel {
    val userCache = UserCache(MapSettings()).apply {
      save(User(id = "me", email = "me@example.test", firstName = "Me", lastName = "Myself"))
    }
    return ChatViewModel(
      taskId = TASK_ID,
      chatRepository = repo,
      openConversationUseCase = OpenConversationUseCase(repo),
      enqueueMessageUseCase = EnqueueMessageUseCase(repo),
      observePendingMessagesUseCase = ObservePendingMessagesUseCase(repo),
      retryPendingMessageUseCase = RetryPendingMessageUseCase(repo),
      discardPendingMessageUseCase = DiscardPendingMessageUseCase(repo),
      markReadUseCase = MarkReadUseCase(repo),
      getCurrentUserIdUseCase = GetCurrentUserIdUseCase(userCache),
      timeZone = TimeZone.UTC,
      dispatcher = StandardTestDispatcher(scope.testScheduler)
    )
  }

  private companion object {
    const val TASK_ID = "t1"
    const val CONVERSATION_ID = "c1"
  }
}
