package com.frame.zero.feature.production.details

import com.frame.zero.feature.production.details.domain.DeleteProductionUseCase
import com.frame.zero.feature.production.details.domain.GetProductionDetailsUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

val featureProductionDetailsModule: Module =
  module {
    factory { GetProductionDetailsUseCase(get()) }
    factory { DeleteProductionUseCase(get()) }
    factory { (productionId: String) ->
      ProductionDetailsViewModel(
        productionId = productionId,
        getProductionDetailsUseCase = get(),
        deleteProductionUseCase = get()
      )
    }
  }
