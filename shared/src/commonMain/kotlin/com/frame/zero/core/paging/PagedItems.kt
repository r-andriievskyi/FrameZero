package com.frame.zero.core.paging

import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.PagingDataEvent
import androidx.paging.PagingDataPresenter
import com.frame.zero.domain.toDomainError
import com.frame.zero.ui.UiText
import com.frame.zero.ui.toUiText
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Presents a `Flow<PagingData<T>>` to a UI without a Paging binding — the SwiftUI counterpart of
 * paging-compose's `LazyPagingItems` (Android keeps using that). Keeps Room + `RemoteMediator`
 * offline-first behaviour unchanged, since the same `PagingData` drives both.
 *
 * Starts collecting on construction; the owner must call [close] when done (the Swift screen model
 * does it in `deinit`). Assumes `enablePlaceholders = false` (true for every pager in the app), so
 * [PagedListState.items] indices match [get].
 */
class PagedItems<T : Any> internal constructor(
  flow: Flow<PagingData<T>>,
  mainContext: CoroutineContext
) {
  constructor(flow: Flow<PagingData<T>>) : this(flow, Dispatchers.Main.immediate)

  private val scope = CoroutineScope(mainContext + SupervisorJob())
  private val _state = MutableStateFlow(PagedListState<T>())
  val state: StateFlow<PagedListState<T>> = _state.asStateFlow()

  private val presenter = object : PagingDataPresenter<T>(mainContext) {
    override suspend fun presentPagingDataEvent(event: PagingDataEvent<T>) {
      _state.update { it.copy(items = snapshot().items) }
    }
  }

  init {
    scope.launch { flow.collectLatest(presenter::collectFrom) }
    scope.launch {
      presenter.loadStateFlow.filterNotNull().collect { loadStates ->
        _state.update {
          it.copy(refresh = loadStates.refresh.toPageLoad(), append = loadStates.append.toPageLoad())
        }
      }
    }
  }

  /** The item at [index]; also tells Paging the UI reached it, which triggers the next page. */
  fun get(index: Int): T? = presenter[index]

  fun refresh() = presenter.refresh()

  fun retry() = presenter.retry()

  fun close() {
    scope.cancel()
  }
}

/**
 * One rendered paging snapshot. The derived flags mirror design_system's `PagingListUiState`
 * (Compose), minus its filter-transition handling — no paged screen on iOS has a filter yet.
 */
data class PagedListState<T : Any>(
  val items: List<T> = emptyList(),
  // Loading until the first PagingData arrives, like LazyPagingItems, so the UI shows progress
  // rather than a flash of the empty state.
  val refresh: PageLoad = PageLoad.Loading,
  val append: PageLoad = PageLoad.Idle
) {
  /** First load with nothing to show yet. */
  val isInitialLoad: Boolean get() = refresh is PageLoad.Loading && items.isEmpty()

  /** Refresh failed with no cached items to fall back on. */
  val initialError: UiText? get() = (refresh as? PageLoad.Failed)?.message?.takeIf { items.isEmpty() }

  /** Reload while items are already visible. */
  val isRefreshing: Boolean get() = refresh is PageLoad.Loading && items.isNotEmpty()

  /** Finished loading with nothing to show — not after a failed refresh (that is an error). */
  val isEmpty: Boolean get() = refresh is PageLoad.Idle && items.isEmpty()
}

/** One direction's load status, with the failure already mapped to user-facing text. */
sealed interface PageLoad {
  data object Idle : PageLoad

  data object Loading : PageLoad

  data class Failed(
    val message: UiText
  ) : PageLoad
}

private fun LoadState.toPageLoad(): PageLoad =
  when (this) {
    is LoadState.NotLoading -> PageLoad.Idle
    LoadState.Loading -> PageLoad.Loading
    is LoadState.Error -> PageLoad.Failed(error.toDomainError().toUiText())
  }
