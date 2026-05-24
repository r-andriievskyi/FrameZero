package com.frame.zero.repository.productions.local

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

private const val DB_NAME = "framezero.db"

class AndroidDatabaseBuilderFactory(
  private val context: Context
) : DatabaseBuilderFactory {
  override fun create(): RoomDatabase.Builder<FrameZeroDatabase> {
    val applicationContext = context.applicationContext
    val dbFile = applicationContext.getDatabasePath(DB_NAME)
    return Room.databaseBuilder<FrameZeroDatabase>(
      context = applicationContext,
      name = dbFile.absolutePath
    )
  }
}
