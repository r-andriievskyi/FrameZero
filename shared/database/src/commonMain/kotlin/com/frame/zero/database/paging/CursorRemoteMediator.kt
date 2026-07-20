package com.frame.zero.database.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import kotlin.coroutines.cancellation.CancellationException

/** A page of already-mapped entities plus the cursor for the following page. */
data class CursorPage<E : Any>(
  val entities: List<E>,
  val nextCursor: String?
)

/**
 * Fetches one remote page and maps its DTOs into entities. [baseOrder] is the
 * `pageOrder` to assign the first item; increment per item to keep insert order.
 */
fun interface RemotePageSource<E : Any> {
  suspend fun load(
    limit: Int,
    cursor: String?,
    baseOrder: Long
  ): CursorPage<E>
}

/**
 * Generic offline-first mediator: REFRESH replaces the cache, APPEND continues
 * from the stored cursor, PREPEND is a no-op. All per-feature specifics (endpoint,
 * DTO entity mapping) live in the [RemotePageSource]; all persistence in [PagedDao].
 */
@OptIn(ExperimentalPagingApi::class)
class CursorRemoteMediator<E : Any>(
  private val dao: PagedDao<E>,
  private val source: RemotePageSource<E>
) : RemoteMediator<Int, E>() {
  override suspend fun load(
    loadType: LoadType,
    state: PagingState<Int, E>
  ): MediatorResult =
    try {
      val cursor: String? = when (loadType) {
        LoadType.REFRESH -> null
        LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
        LoadType.APPEND ->
          dao.nextCursor() ?: return MediatorResult.Success(endOfPaginationReached = true)
      }

      val baseOrder = if (loadType == LoadType.REFRESH) 0L else (dao.maxPageOrder() ?: -1L) + 1L
      val page = source.load(limit = state.config.pageSize, cursor = cursor, baseOrder = baseOrder)

      if (loadType == LoadType.REFRESH) {
        dao.refresh(page.entities, page.nextCursor)
      } else {
        dao.append(page.entities, page.nextCursor)
      }

      MediatorResult.Success(endOfPaginationReached = page.nextCursor == null)
    } catch (cancellation: CancellationException) {
      throw cancellation
    } catch (throwable: Throwable) {
      MediatorResult.Error(throwable)
    }
}
