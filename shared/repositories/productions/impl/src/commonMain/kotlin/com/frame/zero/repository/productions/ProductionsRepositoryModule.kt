package com.frame.zero.repository.productions

import com.frame.zero.core.session.SessionCleaner
import com.frame.zero.database.FrameZeroDatabase
import com.frame.zero.repository.productions.network.ProductionsApi
import com.frame.zero.repository.productions.network.ProductionsApiImpl
import org.koin.dsl.bind
import org.koin.dsl.module

val productionsRepositoryModule = module {
  single<ProductionsApi> { ProductionsApiImpl(get(), get()) }
  single<ProductionsRepository> { ProductionsRepositoryImpl(get(), get()) }
  single {
    ProductionsSessionCleaner(get<FrameZeroDatabase>().productionsCacheDao())
  } bind SessionCleaner::class
}
