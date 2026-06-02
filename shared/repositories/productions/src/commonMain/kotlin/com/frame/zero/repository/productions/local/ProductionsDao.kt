package com.frame.zero.repository.productions.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
abstract class ProductionsDao {
  @Query("SELECT * FROM productions WHERE phaseFilter = :filter ORDER BY pageOrder ASC")
  abstract fun pagingSource(filter: String): PagingSource<Int, ProductionEntity>

  @Query("SELECT MAX(pageOrder) FROM productions WHERE phaseFilter = :filter")
  abstract suspend fun maxPageOrder(filter: String): Long?

  @Query("SELECT * FROM production_remote_keys WHERE phaseFilter = :filter")
  abstract suspend fun remoteKey(filter: String): ProductionRemoteKeyEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  abstract suspend fun insertProductions(entities: List<ProductionEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  abstract suspend fun upsertRemoteKey(key: ProductionRemoteKeyEntity)

  @Query("DELETE FROM productions WHERE phaseFilter = :filter")
  abstract suspend fun deleteByFilter(filter: String)

  @Query("DELETE FROM productions WHERE id = :id")
  abstract suspend fun deleteById(id: String)

  @Query("DELETE FROM production_remote_keys WHERE phaseFilter = :filter")
  abstract suspend fun deleteRemoteKey(filter: String)

  @Transaction
  open suspend fun refresh(
    filter: String,
    entities: List<ProductionEntity>,
    nextCursor: String?
  ) {
    deleteByFilter(filter)
    deleteRemoteKey(filter)
    insertProductions(entities)
    upsertRemoteKey(ProductionRemoteKeyEntity(filter, nextCursor))
  }

  @Transaction
  open suspend fun append(
    filter: String,
    entities: List<ProductionEntity>,
    nextCursor: String?
  ) {
    insertProductions(entities)
    upsertRemoteKey(ProductionRemoteKeyEntity(filter, nextCursor))
  }
}
