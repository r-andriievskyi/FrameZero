package com.frame.zero.repository.productions

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.frame.zero.core.session.SessionCleaner
import com.frame.zero.repository.productions.local.DatabaseBuilderFactory
import com.frame.zero.repository.productions.local.FrameZeroDatabase
import com.frame.zero.repository.productions.network.ProductionsApi
import com.frame.zero.repository.productions.network.ProductionsApiImpl
import kotlinx.coroutines.Dispatchers
import org.koin.dsl.bind
import org.koin.dsl.module

val productionsRepositoryModule = module {
  single<ProductionsApi> { ProductionsApiImpl(get(), get()) }
  single<FrameZeroDatabase> {
    get<DatabaseBuilderFactory>()
      .create()
      .setDriver(BundledSQLiteDriver())
      .setQueryCoroutineContext(Dispatchers.Default)
      .build()
  }
  single<ProductionsRepository> { ProductionsRepositoryImpl(get(), get()) }
  single {
    ProductionsSessionCleaner(get<FrameZeroDatabase>().productionsCacheDao())
  } bind SessionCleaner::class
}
