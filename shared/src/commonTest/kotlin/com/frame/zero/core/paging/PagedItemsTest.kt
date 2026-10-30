package com.frame.zero.core.paging

import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import com.frame.zero.domain.OfflineException
import com.frame.zero.ui.ErrorStringKey
import com.frame.zero.ui.asUiText
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PagedItemsTest {
  private val dispatcher = StandardTestDispatcher()

  private fun TestScope.present(flow: Flow<PagingData<String>>): PagedItems<String> {
    val paged = PagedItems(flow, dispatcher)
    advanceUntilIdle()
    return paged
  }

  private fun loaded(items: List<String>) =
    PagingData.from(items, sourceLoadStates = idle(refresh = LoadState.NotLoading(endOfPaginationReached = false)))

  private fun idle(refresh: LoadState) =
    LoadStates(
      refresh = refresh,
      prepend = LoadState.NotLoading(endOfPaginationReached = true),
      append = LoadState.NotLoading(endOfPaginationReached = true)
    )

  @Test
  fun `starts in initial load before the first PagingData`() {
    val state = PagedListState<String>()
    assertTrue(state.isInitialLoad)
    assertFalse(state.isEmpty)
  }

  @Test
  fun `emits the snapshot and idle load states`() =
    runTest(dispatcher) {
      val paged = present(flowOf(loaded(listOf("a", "b"))))

      val state = paged.state.value
      assertEquals(listOf("a", "b"), state.items)
      assertEquals(PageLoad.Idle, state.refresh)
      assertEquals(PageLoad.Idle, state.append)
      assertFalse(state.isInitialLoad)
      assertFalse(state.isEmpty)
      assertEquals("b", paged.get(1))
      paged.close()
    }

  @Test
  fun `empty result is empty and not loading`() =
    runTest(dispatcher) {
      val paged = present(flowOf(loaded(emptyList())))

      assertTrue(paged.state.value.isEmpty)
      assertFalse(paged.state.value.isInitialLoad)
      paged.close()
    }

  @Test
  fun `static data without load states keeps the initial Loading refresh`() =
    runTest(dispatcher) {
      // Why fakes must pass sourceLoadStates: Paging dispatches none for a bare PagingData.from(list).
      val paged = present(flowOf(PagingData.from(listOf("a"))))

      assertEquals(listOf("a"), paged.state.value.items)
      assertEquals(PageLoad.Loading, paged.state.value.refresh)
      paged.close()
    }

  @Test
  fun `failed refresh with no items is an initial error with mapped text`() =
    runTest(dispatcher) {
      val failed = idle(refresh = LoadState.Error(OfflineException()))
      val paged = present(flowOf(PagingData.from(emptyList(), sourceLoadStates = failed)))

      val state = paged.state.value
      assertEquals(ErrorStringKey.NETWORK.asUiText(), state.initialError)
      assertFalse(state.isEmpty)
      paged.close()
    }

  @Test
  fun `new PagingData replaces the snapshot`() =
    runTest(dispatcher) {
      val source = MutableStateFlow(loaded(listOf("a")))
      val paged = present(source)

      source.value = loaded(listOf("x", "y", "z"))
      advanceUntilIdle()

      assertEquals(listOf("x", "y", "z"), paged.state.value.items)
      paged.close()
    }

  @Test
  fun `close stops presenting`() =
    runTest(dispatcher) {
      val source = MutableStateFlow(loaded(listOf("a")))
      val paged = present(source)

      paged.close()
      source.value = loaded(listOf("x"))
      advanceUntilIdle()

      assertEquals(listOf("a"), paged.state.value.items)
    }
}
