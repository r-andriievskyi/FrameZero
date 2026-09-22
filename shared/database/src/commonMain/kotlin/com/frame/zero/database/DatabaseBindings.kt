package com.frame.zero.database

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.frame.zero.database.dao.TaskSummariesDao
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.Dispatchers

/**
 * Builds the single shared [FrameZeroDatabase] and exposes its DAOs. The platform
 * [DatabaseBuilderFactory] is bound separately per platform (Android needs a Context).
 * Schema changes drop and recreate the DB — fine pre-production (no migrations yet).
 */
@ContributesTo(AppScope::class)
@BindingContainer
object DatabaseBindings {
  @Provides
  @SingleIn(AppScope::class)
  fun database(builderFactory: DatabaseBuilderFactory): FrameZeroDatabase =
    builderFactory
      .create()
      .setDriver(BundledSQLiteDriver())
      .setQueryCoroutineContext(Dispatchers.Default)
      .fallbackToDestructiveMigration(dropAllTables = true)
      .build()

  @Provides
  @SingleIn(AppScope::class)
  fun productionsCacheDao(database: FrameZeroDatabase): ProductionsDao = database.productionsCacheDao()

  @Provides
  @SingleIn(AppScope::class)
  fun pendingUploadsDao(database: FrameZeroDatabase): PendingUploadDao = database.pendingUploadsDao()

  @Provides
  @SingleIn(AppScope::class)
  fun taskSummariesDao(database: FrameZeroDatabase): TaskSummariesDao = database.taskSummariesDao()
}
