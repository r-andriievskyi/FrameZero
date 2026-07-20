package com.frame.zero.database.paging

import androidx.paging.PagingSource

/**
 * Contract every offline-first, cursor-paged Room DAO fulfils so a single
 * [CursorRemoteMediator] can drive them. Room DAOs are abstract classes, so a
 * DAO implements this by declaring `abstract class XDao : PagedDao<XEntity>()`
 * and annotating the concrete `override`s with `@Query`/`@Insert`/`@Transaction`.
 *
 * Entities must carry a `pageOrder` column so [pagingSource] can return a stable
 * order and [maxPageOrder] can continue numbering appended pages.
 */
interface PagedDao<E : Any> {
  fun pagingSource(): PagingSource<Int, E>

  suspend fun nextCursor(): String?

  suspend fun maxPageOrder(): Long?

  suspend fun refresh(
    entities: List<E>,
    nextCursor: String?
  )

  suspend fun append(
    entities: List<E>,
    nextCursor: String?
  )

  suspend fun clearAll()
}
