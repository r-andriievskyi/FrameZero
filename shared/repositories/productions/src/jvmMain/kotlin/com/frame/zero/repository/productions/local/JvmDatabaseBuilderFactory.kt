package com.frame.zero.repository.productions.local

import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

private const val DB_DIR = ".framezero"
private const val DB_NAME = "framezero.db"

class JvmDatabaseBuilderFactory : DatabaseBuilderFactory {
  override fun create(): RoomDatabase.Builder<FrameZeroDatabase> {
    val home = System.getProperty("user.home")
    val dir = File(home, DB_DIR).apply { mkdirs() }
    val dbFile = File(dir, DB_NAME)
    return Room.databaseBuilder<FrameZeroDatabase>(name = dbFile.absolutePath)
  }
}
