package com.frame.zero.repository.chat

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.frame.zero.database.ChatDao
import com.frame.zero.database.MessageEntity
import com.frame.zero.repository.chat.local.toEntity
import com.frame.zero.repository.chat.network.ChatApi
import kotlinx.coroutines.CancellationException

/**
 * Backfills older history from the REST endpoint. Newer messages arrive on the socket (or on
 * a reconnect sync), so [LoadType.PREPEND] is a no-op. Older pages load with
 * `before = MIN(ordinal)` currently in Room — the server returns newest-first, so its last row
 * is the next cursor.
 */
@OptIn(ExperimentalPagingApi::class)
internal class ChatMessagesRemoteMediator(
  private val conversationId: String,
  private val api: ChatApi,
  private val dao: ChatDao
) : RemoteMediator<Int, MessageEntity>() {
  override suspend fun load(
    loadType: LoadType,
    state: PagingState<Int, MessageEntity>
  ): MediatorResult =
    try {
      val before: Long? = when (loadType) {
        LoadType.REFRESH -> null
        LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
        LoadType.APPEND -> dao.minOrdinal(conversationId)
          ?: return MediatorResult.Success(endOfPaginationReached = true)
      }

      val response = api.listMessages(conversationId, before, state.config.pageSize)
      dao.upsertMessagesAndAdvanceLatest(response.items.map { it.toEntity() })

      MediatorResult.Success(endOfPaginationReached = response.nextCursor == null)
    } catch (exception: CancellationException) {
      throw exception
    } catch (throwable: Throwable) {
      MediatorResult.Error(throwable)
    }
}
