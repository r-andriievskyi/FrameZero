package com.frame.zero.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.frame.zero.database.dao.TaskSummariesDao
import com.frame.zero.database.entity.TaskSummaryEntity
import com.frame.zero.database.entity.TaskSummaryRemoteKeyEntity

@Database(
  entities = [
    ProductionEntity::class,
    ProductionRemoteKeyEntity::class,
    PendingUploadEntity::class,
    ConversationEntity::class,
    MessageEntity::class,
    PendingMessageEntity::class,
    TaskSummaryEntity::class,
    TaskSummaryRemoteKeyEntity::class
  ],
  version = 6,
  exportSchema = false
)
@ConstructedBy(FrameZeroDatabaseConstructor::class)
abstract class FrameZeroDatabase : RoomDatabase() {
  abstract fun productionsCacheDao(): ProductionsDao

  abstract fun taskSummariesDao(): TaskSummariesDao

  abstract fun pendingUploadsDao(): PendingUploadDao

  abstract fun chatDao(): ChatDao

  abstract fun chatOutboxDao(): ChatOutboxDao
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object FrameZeroDatabaseConstructor : RoomDatabaseConstructor<FrameZeroDatabase> {
  override fun initialize(): FrameZeroDatabase
}
