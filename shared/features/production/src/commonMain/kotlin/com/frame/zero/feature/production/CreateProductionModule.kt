package com.frame.zero.feature.production

import com.frame.zero.feature.production.domain.CreateProductionUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

val featureProductionModule: Module =
  module {
    factory { CreateProductionUseCase(get()) }
    factory { CreateProductionViewModel(get()) }
  }
