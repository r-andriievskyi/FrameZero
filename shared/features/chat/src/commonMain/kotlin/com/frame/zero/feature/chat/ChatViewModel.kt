package com.frame.zero.feature.chat

import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.frame.zero.core.error.DomainErrorMessages
import com.frame.zero.core.error.toUiText
import com.frame.zero.domain.Outcome
import com.frame.zero.domain.chat.ChatMessage
import com.frame.zero.domain.chat.PendingChatMessage
import com.frame.zero.domain.chat.PendingMessageStatus
import com.frame.zero.feature.chat.domain.DiscardPendingMessageUseCase
import com.frame.zero.feature.chat.domain.EnqueueMessageUseCase
import com.frame.zero.feature.chat.domain.GetCurrentUserIdUseCase
import com.frame.zero.feature.chat.domain.MarkReadUseCase
import com.frame.zero.feature.chat.domain.ObservePendingMessagesUseCase
import com.frame.zero.feature.chat.domain.OpenConversationUseCase
import com.frame.zero.feature.chat.domain.RetryPendingMessageUseCase
import com.frame.zero.repository.chat.ChatRepository
import framezero.shared.features.chat.generated.resources.Res
import framezero.shared.features.chat.generated.resources.chat_error_auth_failed
import framezero.shared.features.chat.generated.resources.chat_error_conflict
import framezero.shared.features.chat.generated.resources.chat_error_forbidden
import framezero.shared.features.chat.generated.resources.chat_error_network
import framezero.shared.features.chat.generated.resources.chat_error_not_found
import framezero.shared.features.chat.generated.resources.chat_error_server
import framezero.shared.features.chat.generated.resources.chat_error_unknown
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kotlin.coroutines.CoroutineContext
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class ChatViewModel(
  private val taskId: String,
  private val chatRepository: ChatRepository,
  private val openConversationUseCase: OpenConversationUseCase,
  private val enqueueMessageUseCase: EnqueueMessageUseCase,
  private val observePendingMessagesUseCase: ObservePendingMessagesUseCase,
  private val retryPendingMessageUseCase: RetryPendingMessageUseCase,
  private val discardPendingMessageUseCase: DiscardPendingMessageUseCase,
  private val markReadUseCase: MarkReadUseCase,
  private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
  private val timeZone: TimeZone = TimeZone.currentSystemDefault(),
  dispatcher: CoroutineContext = Dispatchers.Main.immediate
) : InstanceKeeper.Instance {
  private val scope = CoroutineScope(dispatcher + SupervisorJob())

  private val _state = MutableStateFlow(ChatState())
  val state: StateFlow<ChatState> = _state.asStateFlow()

  private val _events = MutableSharedFlow<ChatEvent>(
    extraBufferCapacity = 1,
    onBufferOverflow = BufferOverflow.DROP_OLDEST
  )
  val events: SharedFlow<ChatEvent> = _events.asSharedFlow()

  private val conversationId = MutableStateFlow<String?>(null)
  private var currentUserId: String? = null

  // Highest ordinal the server has confirmed as read, so scroll-driven MarkRead intents
  // don't fire a redundant PUT once the cursor is there. Advanced only on success, so a
  // failed mark-read is retried by the next scroll-to-bottom.
  private var lastMarkedOrdinal = 0L

  // Ordinal of the mark-read currently in flight, to dedupe concurrent intents without
  // blocking a retry: on completion it falls back to lastMarkedOrdinal.
  private var inFlightOrdinal = 0L

  @OptIn(ExperimentalCoroutinesApi::class)
  val messages: Flow<PagingData<ChatMessageUi>> =
    conversationId
      .filterNotNull()
      .flatMapLatest { id ->
        chatRepository.messages(id).map { page -> page.map { it.toUi() } }
      }.cachedIn(scope)

  @OptIn(ExperimentalCoroutinesApi::class)
  private val pending: Flow<List<PendingChatMessage>> =
    conversationId.filterNotNull().flatMapLatest { observePendingMessagesUseCase(it) }

  init {
    // Read the cached user id up front so the first paged frames style own/other bubbles
    // correctly — including offline, where the id comes from local cache, not the network.
    currentUserId = getCurrentUserIdUseCase()
    scope.launch { openConversation() }
    scope.launch {
      pending.collect { pendingMessages ->
        // Newest first, matching the reversed message list: the outbox emits oldest first.
        val bubbles = pendingMessages.map { it.toUi() }.asReversed().toImmutableList()
        _state.update { it.copy(pending = bubbles) }
      }
    }
  }

  fun onIntent(intent: ChatIntent) {
    when (intent) {
      is ChatIntent.MessageChanged -> _state.update { it.copy(draft = intent.text) }
      ChatIntent.SendClicked -> send()
      ChatIntent.Retry -> scope.launch { openConversation() }
      is ChatIntent.RetryPending -> retryPending(intent.clientMessageId)
      is ChatIntent.DiscardPending -> discardPending(intent.clientMessageId)
      is ChatIntent.MarkRead -> markRead(intent.ordinal)
    }
  }

  /**
   * Both outbox actions are their own feedback: the pending row is what the user is looking at, so
   * a failure simply leaves the bubble where it was — still failed, still offering retry and
   * discard — and a success is visible as the row changing or disappearing. Nothing to report.
   */
  private fun retryPending(clientMessageId: String) {
    val id = conversationId.value ?: return
    scope.launch {
      when (retryPendingMessageUseCase(RetryPendingMessageUseCase.Params(id, clientMessageId))) {
        is Outcome.Success -> Unit
        is Outcome.Failure -> Unit
      }
    }
  }

  private fun discardPending(clientMessageId: String) {
    val id = conversationId.value ?: return
    scope.launch {
      when (discardPendingMessageUseCase(DiscardPendingMessageUseCase.Params(id, clientMessageId))) {
        is Outcome.Success -> Unit
        is Outcome.Failure -> Unit
      }
    }
  }

  private fun markRead(ordinal: Long) {
    val id = conversationId.value ?: return
    // Skip if already confirmed or a request for this ordinal (or newer) is in flight.
    if (ordinal <= maxOf(lastMarkedOrdinal, inFlightOrdinal)) return
    inFlightOrdinal = ordinal
    scope.launch {
      when (markReadUseCase(MarkReadUseCase.Params(id, ordinal))) {
        is Outcome.Success -> lastMarkedOrdinal = maxOf(lastMarkedOrdinal, ordinal)
        // Failure leaves lastMarkedOrdinal untouched so the next scroll-to-bottom retries.
        is Outcome.Failure -> Unit
      }
      // Release the in-flight guard down to what's confirmed, re-enabling a retry.
      inFlightOrdinal = lastMarkedOrdinal
    }
    // The divider is intentionally not moved — it stays put for the session.
  }

  private suspend fun openConversation() {
    _state.update { it.copy(isLoadingConversation = true, conversationError = null) }
    when (val outcome = openConversationUseCase(taskId)) {
      is Outcome.Success -> {
        val conversation = outcome.data
        conversationId.value = conversation.id
        lastMarkedOrdinal = conversation.lastReadOrdinal
        val divider = conversation.lastReadOrdinal.takeIf { conversation.unreadCount > 0 }
        _state.update {
          it.copy(isLoadingConversation = false, isReady = true, newMessagesDividerOrdinal = divider)
        }
      }
      is Outcome.Failure ->
        _state.update {
          it.copy(
            isLoadingConversation = false,
            isReady = false,
            conversationError = outcome.error.toUiText(errorMessages)
          )
        }
    }
  }

  /**
   * Queues the draft and clears the composer straight away — the outbox owns delivery from here, so
   * there is nothing to wait for and nothing to fail. The message appears as a pending bubble until
   * the server confirms it.
   */
  private fun send() {
    val id = conversationId.value ?: return
    val body = _state.value.draft.trim()
    if (body.isEmpty()) return
    // A fresh id per send: the outbox row owns retry idempotency now, so nothing is reused.
    val clientMessageId = Uuid.random().toString()
    _state.update { it.copy(draft = "") }
    scope.launch {
      when (enqueueMessageUseCase(EnqueueMessageUseCase.Params(id, clientMessageId, body))) {
        is Outcome.Success -> _events.tryEmit(ChatEvent.MessageSent)
        // Queueing itself failed (a broken local database), so there is no pending bubble to show
        // and nothing will retry it. Put the text back in the composer rather than dropping it —
        // tapping send again is the retry.
        is Outcome.Failure -> _state.update { current -> current.copy(draft = current.draft.ifEmpty { body }) }
      }
    }
  }

  private fun PendingChatMessage.toUi(): PendingMessageUi {
    val localDateTime = createdAt.toLocalDateTime(timeZone)
    return PendingMessageUi(
      clientMessageId = clientMessageId,
      body = body,
      timeLabel = timeFormat.format(localDateTime.time),
      day = localDateTime.date,
      isFailed = status == PendingMessageStatus.Failed
    )
  }

  private fun ChatMessage.toUi(): ChatMessageUi {
    val localDateTime = createdAt.toLocalDateTime(timeZone)
    return ChatMessageUi(
      id = id,
      ordinal = ordinal,
      body = body,
      isOwn = currentUserId != null && senderUserId == currentUserId,
      timeLabel = timeFormat.format(localDateTime.time),
      day = localDateTime.date
    )
  }

  override fun onDestroy() {
    scope.cancel()
  }

  private companion object {
    // 12-hour clock with an uppercase meridiem, e.g. "9:12 AM".
    val timeFormat = LocalTime.Format {
      amPmHour(Padding.NONE)
      char(':')
      minute(Padding.ZERO)
      char(' ')
      amPmMarker("AM", "PM")
    }

    val errorMessages = DomainErrorMessages(
      network = Res.string.chat_error_network,
      server = Res.string.chat_error_server,
      notFound = Res.string.chat_error_not_found,
      forbidden = Res.string.chat_error_forbidden,
      conflict = Res.string.chat_error_conflict,
      invalidCredentials = Res.string.chat_error_auth_failed,
      emailExists = Res.string.chat_error_unknown,
      fallback = Res.string.chat_error_unknown
    )
  }
}
