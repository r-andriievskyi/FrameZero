package com.frame.zero.repository.productions

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.frame.zero.repository.productions.local.DatabaseBuilderFactory
import com.frame.zero.repository.productions.local.FrameZeroDatabase
import com.frame.zero.repository.productions.network.KtorProductionsRemoteApi
import com.frame.zero.repository.productions.network.ProductionsRemoteApi
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.dsl.module

val productionsRepositoryModule: Module =
  module {
    single<ProductionsRemoteApi> { KtorProductionsRemoteApi(get(), get()) }
    single<FrameZeroDatabase> {
      get<DatabaseBuilderFactory>()
        .create()
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.Default)
        .build()
    }
    single<ProductionsRepository> { OfflineFirstProductionsRepository(get(), get()) }
  }
