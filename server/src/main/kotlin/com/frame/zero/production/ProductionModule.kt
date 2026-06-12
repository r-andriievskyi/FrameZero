package com.frame.zero.production

import org.koin.dsl.module

fun productionModule() =
  module {
    single<ProductionRepository> { ProductionRepositoryImpl() }
    single<ProductionMemberRepository> { ProductionMemberRepositoryImpl() }
    single { ProductionAccessService(get(), get()) }
    single { ProductionService(get(), get(), get(), get(), get()) }
  }
