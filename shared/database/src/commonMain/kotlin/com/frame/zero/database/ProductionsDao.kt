package com.frame.zero.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
abstract class ProductionsDao {
  @Query("SELECT * FROM productions ORDER BY pageOrder ASC")
  abstract fun pagingSource(): PagingSource<Int, ProductionEntity>

  @Query("SELECT MAX(pageOrder) FROM productions")
  abstract suspend fun maxPageOrder(): Long?

  @Query("SELECT * FROM production_remote_keys LIMIT 1")
  abstract suspend fun remoteKey(): ProductionRemoteKeyEntity?

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
  open suspend fun refresh(
    entities: List<ProductionEntity>,
    nextCursor: String?
  ) {
    deleteAll()
    deleteRemoteKey()
    insertProductions(entities)
    upsertRemoteKey(ProductionRemoteKeyEntity(nextCursor = nextCursor))
  }

  @Transaction
  open suspend fun append(
    entities: List<ProductionEntity>,
    nextCursor: String?
  ) {
    insertProductions(entities)
    upsertRemoteKey(ProductionRemoteKeyEntity(nextCursor = nextCursor))
  }

  @Transaction
  open suspend fun clearAll() {
    deleteAll()
    deleteRemoteKey()
  }
}
