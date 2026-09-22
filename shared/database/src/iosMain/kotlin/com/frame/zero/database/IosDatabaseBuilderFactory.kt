package com.frame.zero.database

import androidx.room.Room
import androidx.room.RoomDatabase
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import platform.Foundation.NSHomeDirectory

private const val DB_NAME = "framezero.db"

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class IosDatabaseBuilderFactory : DatabaseBuilderFactory {
  override fun create(): RoomDatabase.Builder<FrameZeroDatabase> {
    val dbFile = "${NSHomeDirectory()}/$DB_NAME"
    return Room.databaseBuilder<FrameZeroDatabase>(name = dbFile)
  }
}
