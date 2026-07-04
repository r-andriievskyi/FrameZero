package com.frame.zero.database

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Builds the single shared [FrameZeroDatabase] and exposes its DAOs. The platform
 * [DatabaseBuilderFactory] is registered separately in `platformModule()` (Android needs a
 * Context). Schema changes drop and recreate the DB — fine pre-production (no migrations yet).
 */
val databaseModule: Module =
  module {
    single<FrameZeroDatabase> {
      get<DatabaseBuilderFactory>()
        .create()
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.Default)
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()
    }
    single { get<FrameZeroDatabase>().productionsCacheDao() }
    single { get<FrameZeroDatabase>().pendingUploadsDao() }
    single { get<FrameZeroDatabase>().chatDao() }
  }
