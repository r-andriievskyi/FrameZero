package com.frame.zero.repository.productions

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.frame.zero.domain.production.ProductionPhase
import com.frame.zero.repository.productions.local.ProductionEntity
import com.frame.zero.repository.productions.local.ProductionsCacheDao
import com.frame.zero.repository.productions.local.filterKeyFor
import com.frame.zero.repository.productions.network.ProductionsRemoteApi
import kotlinx.coroutines.CancellationException

@OptIn(ExperimentalPagingApi::class)
internal class ProductionsRemoteMediator(
  private val phase: ProductionPhase?,
  private val remoteApi: ProductionsRemoteApi,
  private val dao: ProductionsCacheDao
) : RemoteMediator<Int, ProductionEntity>() {
  private val filter: String = filterKeyFor(phase)

  override suspend fun load(
    loadType: LoadType,
    state: PagingState<Int, ProductionEntity>
  ): MediatorResult =
    try {
      val cursor: String? = when (loadType) {
        LoadType.REFRESH -> null
        LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
        LoadType.APPEND -> {
          val nextCursor = dao.remoteKey(filter)?.nextCursor
            ?: return MediatorResult.Success(endOfPaginationReached = true)
          nextCursor
        }
      }

      val response = remoteApi.getAll(
        limit = state.config.pageSize,
        cursor = cursor,
        phase = phase
      )

      val baseOrder: Long =
        if (loadType == LoadType.REFRESH) 0L else (dao.maxPageOrder(filter) ?: -1L) + 1L

      val entities = response.items.mapIndexed { index, dto ->
        ProductionEntity(
          id = dto.id,
          phaseFilter = filter,
          title = dto.title,
          genre = dto.genre.name,
          phase = dto.phase.name,
          progressPercent = dto.progressPercent,
          daysLeft = dto.daysLeft,
          membersCount = dto.membersCount,
          updatedAtEpochMs = dto.updatedAt.toEpochMilliseconds(),
          pageOrder = baseOrder + index
        )
      }

      if (loadType == LoadType.REFRESH) {
        dao.refresh(filter, entities, response.nextCursor)
      } else {
        dao.append(filter, entities, response.nextCursor)
      }

      MediatorResult.Success(endOfPaginationReached = response.nextCursor == null)
    } catch (cancellation: CancellationException) {
      throw cancellation
    } catch (throwable: Throwable) {
      MediatorResult.Error(throwable)
    }
}
