package com.frame.zero.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor

@Database(
  entities = [
    ProductionEntity::class,
    ProductionRemoteKeyEntity::class,
    PendingUploadEntity::class,
    ConversationEntity::class,
    MessageEntity::class
  ],
  version = 4,
  exportSchema = false
)
@ConstructedBy(FrameZeroDatabaseConstructor::class)
abstract class FrameZeroDatabase : RoomDatabase() {
  abstract fun productionsCacheDao(): ProductionsDao

  abstract fun pendingUploadsDao(): PendingUploadDao

  abstract fun chatDao(): ChatDao
}

@Suppress("KotlinNoActualForExpect", "NO_ACTUAL_FOR_EXPECT")
expect object FrameZeroDatabaseConstructor : RoomDatabaseConstructor<FrameZeroDatabase> {
  override fun initialize(): FrameZeroDatabase
}
