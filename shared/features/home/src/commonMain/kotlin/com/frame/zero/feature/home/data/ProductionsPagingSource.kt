package com.frame.zero.feature.home.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.frame.zero.domain.production.Production
import com.frame.zero.domain.production.ProductionPhase
import com.frame.zero.domain.production.toProduction
import com.frame.zero.repository.productions.ProductionsRepository

class ProductionsPagingSource(
  private val repository: ProductionsRepository,
  private val phase: ProductionPhase?
) : PagingSource<String, Production>() {
  override fun getRefreshKey(state: PagingState<String, Production>): String? = null

  override suspend fun load(params: LoadParams<String>): LoadResult<String, Production> =
    try {
      val response = repository.getAll(
        limit = params.loadSize,
        cursor = params.key,
        phase = phase
      )
      LoadResult.Page(
        data = response.items.map { it.toProduction() },
        prevKey = null,
        nextKey = response.nextCursor
      )
    } catch (cancellation: kotlinx.coroutines.CancellationException) {
      throw cancellation
    } catch (throwable: Throwable) {
      LoadResult.Error(throwable)
    }
}
