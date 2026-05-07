package com.frame.zero.production

import org.koin.dsl.module

fun productionModule() =
  module {
    single<ProductionRepository> { ProductionRepositoryExposed() }
    single<ProductionMemberRepository> { ProductionMemberRepositoryExposed() }
    single { ProductionAccessService(get(), get()) }
    single { ProductionService(get(), get(), get(), get()) }
  }
