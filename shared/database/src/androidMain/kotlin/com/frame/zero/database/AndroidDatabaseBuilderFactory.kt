package com.frame.zero.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

private const val DB_NAME = "framezero.db"

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
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
