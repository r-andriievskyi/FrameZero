package com.frame.zero.repository.productions.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "production_remote_keys")
data class ProductionRemoteKeyEntity(
  @PrimaryKey val phaseFilter: String,
  val nextCursor: String?
)
