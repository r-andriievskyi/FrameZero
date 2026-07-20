package com.frame.zero.database.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.frame.zero.database.entity.TaskSummaryEntity
import com.frame.zero.database.entity.TaskSummaryRemoteKeyEntity
import com.frame.zero.database.paging.PagedDao

@Dao
abstract class TaskSummariesDao : PagedDao<TaskSummaryEntity> {
  @Query("SELECT * FROM task_summaries ORDER BY pageOrder ASC")
  abstract override fun pagingSource(): PagingSource<Int, TaskSummaryEntity>

  @Query("SELECT MAX(pageOrder) FROM task_summaries")
  abstract override suspend fun maxPageOrder(): Long?

  @Query("SELECT nextCursor FROM task_summary_remote_keys LIMIT 1")
  abstract override suspend fun nextCursor(): String?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  abstract suspend fun insertTasks(entities: List<TaskSummaryEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  abstract suspend fun upsertRemoteKey(key: TaskSummaryRemoteKeyEntity)

  @Query("DELETE FROM task_summaries")
  abstract suspend fun deleteAll()

  @Query("DELETE FROM task_summary_remote_keys")
  abstract suspend fun deleteRemoteKey()

  @Transaction
  override suspend fun refresh(
    entities: List<TaskSummaryEntity>,
    nextCursor: String?
  ) {
    deleteAll()
    deleteRemoteKey()
    insertTasks(entities)
    upsertRemoteKey(TaskSummaryRemoteKeyEntity(nextCursor = nextCursor))
  }

  @Transaction
  override suspend fun append(
    entities: List<TaskSummaryEntity>,
    nextCursor: String?
  ) {
    insertTasks(entities)
    upsertRemoteKey(TaskSummaryRemoteKeyEntity(nextCursor = nextCursor))
  }

  @Transaction
  override suspend fun clearAll() {
    deleteAll()
    deleteRemoteKey()
  }
}
