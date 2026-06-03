package com.frame.zero.repository.productions.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "production_remote_keys")
data class ProductionRemoteKeyEntity(
  @PrimaryKey val id: Int = SINGLETON_ID,
  val nextCursor: String?
) {
  companion object {
    const val SINGLETON_ID: Int = 0
  }
}
