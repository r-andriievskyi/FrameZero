package com.frame.zero.repository.productions.local

import androidx.room.Room
import androidx.room.RoomDatabase
import platform.Foundation.NSHomeDirectory

private const val DB_NAME = "framezero.db"

class IosDatabaseBuilderFactory : DatabaseBuilderFactory {
  override fun create(): RoomDatabase.Builder<FrameZeroDatabase> {
    val dbFile = NSHomeDirectory() + "/$DB_NAME"
    return Room.databaseBuilder<FrameZeroDatabase>(name = dbFile)
  }
}
