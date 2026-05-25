package com.frame.zero.repository.productions.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

@Database(
  entities = [ProductionEntity::class, ProductionRemoteKeyEntity::class],
  version = 1,
  exportSchema = true
)
@ConstructedBy(FrameZeroDatabaseConstructor::class)
abstract class FrameZeroDatabase : RoomDatabase() {
  abstract fun productionsCacheDao(): ProductionsDao
}

@Suppress("KotlinNoActualForExpect", "NO_ACTUAL_FOR_EXPECT")
expect object FrameZeroDatabaseConstructor : RoomDatabaseConstructor<FrameZeroDatabase> {
  override fun initialize(): FrameZeroDatabase
}
