package com.frame.zero.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.frame.zero.database.paging.PagedDao

@Dao
abstract class ProductionsDao : PagedDao<ProductionEntity> {
  @Query("SELECT * FROM productions ORDER BY pageOrder ASC")
  abstract override fun pagingSource(): PagingSource<Int, ProductionEntity>

  @Query("SELECT MAX(pageOrder) FROM productions")
  abstract override suspend fun maxPageOrder(): Long?

  @Query("SELECT nextCursor FROM production_remote_keys LIMIT 1")
  abstract override suspend fun nextCursor(): String?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  abstract suspend fun insertProductions(entities: List<ProductionEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  abstract suspend fun upsertRemoteKey(key: ProductionRemoteKeyEntity)

  @Query("DELETE FROM productions")
  abstract suspend fun deleteAll()

  @Query("DELETE FROM productions WHERE id = :id")
  abstract suspend fun deleteById(id: String)

  @Query("DELETE FROM production_remote_keys")
  abstract suspend fun deleteRemoteKey()

  @Transaction
  override suspend fun refresh(
    entities: List<ProductionEntity>,
    nextCursor: String?
  ) {
    deleteAll()
    deleteRemoteKey()
    insertProductions(entities)
    upsertRemoteKey(ProductionRemoteKeyEntity(nextCursor = nextCursor))
  }

  @Transaction
  override suspend fun append(
    entities: List<ProductionEntity>,
    nextCursor: String?
  ) {
    insertProductions(entities)
    upsertRemoteKey(ProductionRemoteKeyEntity(nextCursor = nextCursor))
  }

  @Transaction
  override suspend fun clearAll() {
    deleteAll()
    deleteRemoteKey()
  }
}
