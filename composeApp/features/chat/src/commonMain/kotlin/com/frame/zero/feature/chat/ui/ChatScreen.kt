package com.frame.zero.feature.chat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.frame.zero.feature.chat.ChatComponent
import com.frame.zero.feature.chat.ChatEvent
import com.frame.zero.feature.chat.ChatIntent
import com.frame.zero.feature.chat.ChatMessageUi
import com.frame.zero.feature.chat.PendingMessageUi
import com.frame.zero.feature.chat.ui.components.ChatInputBar
import com.frame.zero.feature.chat.ui.components.MessageRow
import com.frame.zero.feature.chat.ui.components.PendingMessageRow
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.widgets.FullScreenError
import com.frame.zero.shared.design_system.widgets.FullScreenProgress
import com.frame.zero.shared.design_system.widgets.TopToolbar
import com.frame.zero.ui.asString
import framezero.composeapp.features.chat.generated.resources.Res
import framezero.composeapp.features.chat.generated.resources.chat_empty
import framezero.composeapp.features.chat.generated.resources.chat_message_placeholder
import framezero.composeapp.features.chat.generated.resources.chat_retry
import framezero.composeapp.features.chat.generated.resources.chat_send
import framezero.composeapp.features.chat.generated.resources.chat_title
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock

@Composable
fun ChatScreen(
  component: ChatComponent,
  modifier: Modifier = Modifier
) {
  val state by component.state.collectAsStateWithLifecycle()
  val messages = component.messages.collectAsLazyPagingItems()
  val listState = rememberLazyListState()
  // Not remembered: recomputed each recomposition so day separators don't freeze on "Today"
  // if the screen stays open across midnight.
  val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

  LaunchedEffect(component) {
    component.events.collect { event ->
      when (event) {
        ChatEvent.MessageSent -> listState.animateScrollToItem(0)
      }
    }
  }

  // The list is reverseLayout, so index 0 is the newest message; parked at the bottom means
  // it's seen. A snapshotFlow keeps the per-frame scroll read out of the composition (so the
  // screen doesn't recompose while scrolling) and re-emits when the newest ordinal changes
  // while still parked at the bottom. The VM dedupes and only advances the cursor forward.
  LaunchedEffect(messages) {
    snapshotFlow {
      if (messages.itemCount > 0 && listState.firstVisibleItemIndex == 0) messages.peek(0)?.ordinal else null
    }.distinctUntilChanged().collect { ordinal ->
      ordinal?.let { component.onIntent(ChatIntent.MarkRead(it)) }
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(AppTheme.colorSystem.background)
      .systemBarsPadding()
      .imePadding()
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      TopToolbar(title = stringResource(Res.string.chat_title), onBack = component.onBack)

      Box(modifier = Modifier.weight(1f)) {
        val isRefreshing = messages.loadState.refresh is LoadState.Loading
        // A queued message is content: offline, it can be the only thing in the conversation, so it
        // must not be hidden behind the empty or loading state.
        val isEmpty = messages.itemCount == 0 && state.pending.isEmpty()
        when {
          state.conversationError != null && isEmpty ->
            FullScreenError(
              modifier = Modifier.fillMaxSize().testTag(ChatTestTags.ERROR),
              message = state.conversationError?.asString().orEmpty(),
              onRetry = { component.onIntent(ChatIntent.Retry) },
              retryLabel = stringResource(Res.string.chat_retry)
            )

          (state.isLoadingConversation || isRefreshing) && isEmpty ->
            FullScreenProgress(modifier = Modifier.fillMaxSize().testTag(ChatTestTags.LOADING))

          isEmpty -> EmptyChat(modifier = Modifier.fillMaxSize())

          else -> MessageList(
            messages = messages,
            pending = state.pending,
            listState = listState,
            today = today,
            newMessagesDividerOrdinal = state.newMessagesDividerOrdinal,
            onRetryPending = { component.onIntent(ChatIntent.RetryPending(it)) },
            onDiscardPending = { component.onIntent(ChatIntent.DiscardPending(it)) }
          )
        }
      }

      ChatInputBar(
        value = state.draft,
        onValueChange = { component.onIntent(ChatIntent.MessageChanged(it)) },
        onSend = { component.onIntent(ChatIntent.SendClicked) },
        canSend = state.canSend,
        placeholder = stringResource(Res.string.chat_message_placeholder),
        sendContentDescription = stringResource(Res.string.chat_send)
      )
    }
  }
}

@Composable
private fun MessageList(
  messages: LazyPagingItems<ChatMessageUi>,
  pending: ImmutableList<PendingMessageUi>,
  listState: LazyListState,
  today: LocalDate,
  newMessagesDividerOrdinal: Long?,
  onRetryPending: (clientMessageId: String) -> Unit,
  onDiscardPending: (clientMessageId: String) -> Unit,
  modifier: Modifier = Modifier
) {
  LazyColumn(
    modifier = modifier.fillMaxSize().testTag(ChatTestTags.LIST),
    state = listState,
    reverseLayout = true,
    contentPadding = PaddingValues(
      horizontal = AppTheme.spacingSystem.space16,
      vertical = AppTheme.spacingSystem.space8
    ),
    verticalArrangement = Arrangement.spacedBy(AppTheme.spacingSystem.space4)
  ) {
    // Read once for the whole list rather than per row: reading itemCount inside an item's content
    // would tie every pending row's recomposition to any change in the paged count.
    // peek() throws past the end, and offline there may be no confirmed message at all.
    val newestConfirmedDay = if (messages.itemCount > 0) messages.peek(0)?.day else null

    // Declared first, so under reverseLayout the queued messages sit at the bottom — after every
    // confirmed one, which is where they will land once the server assigns their ordinals.
    itemsIndexed(
      items = pending,
      key = { _, message -> message.clientMessageId },
      contentType = { _, _ -> PENDING_CONTENT_TYPE }
    ) { index, message ->
      // pending is newest first, so the next-older row is the following index, and past the end of
      // the list it is the newest confirmed message.
      val olderDay = pending.getOrNull(index + 1)?.day ?: newestConfirmedDay
      PendingMessageRow(
        message = message,
        showDaySeparator = message.day != olderDay,
        today = today,
        onRetry = { onRetryPending(message.clientMessageId) },
        onDiscard = { onDiscardPending(message.clientMessageId) }
      )
    }

    items(
      count = messages.itemCount,
      key = messages.itemKey { it.id },
      contentType = { CONFIRMED_CONTENT_TYPE }
    ) { index ->
      val message = messages[index] ?: return@items
      // The list is newest-first; a day separator sits above the oldest message of each day,
      // which — in reverseLayout — renders at the top of that day's group. peek() throws past
      // the end, so only look at the next-older item when there is one.
      val olderMessage = if (index + 1 < messages.itemCount) messages.peek(index + 1) else null
      // The "New messages" divider goes above the first unread message — this row is unread
      // (ordinal past the cursor) while the next-older row is read (or absent).
      val showNewMessagesDivider = newMessagesDividerOrdinal != null &&
        message.ordinal > newMessagesDividerOrdinal &&
        (olderMessage == null || olderMessage.ordinal <= newMessagesDividerOrdinal)
      MessageRow(
        message = message,
        showDaySeparator = message.day != olderMessage?.day,
        showNewMessagesDivider = showNewMessagesDivider,
        today = today
      )
    }
  }
}

@Composable
private fun EmptyChat(modifier: Modifier = Modifier) {
  Box(modifier = modifier, contentAlignment = Alignment.Center) {
    Text(
      text = stringResource(Res.string.chat_empty),
      style = AppTheme.typographySystem.bodyLarge,
      color = AppTheme.colorSystem.textMuted
    )
  }
}

// Distinct content types so the LazyColumn reuses a slot within each block instead of discarding
// compositions at the pending/confirmed boundary.
private const val PENDING_CONTENT_TYPE = "pending"
private const val CONFIRMED_CONTENT_TYPE = "confirmed"

internal object ChatTestTags {
  const val LIST = "chat_list"
  const val LOADING = "chat_loading"
  const val ERROR = "chat_error"
}
