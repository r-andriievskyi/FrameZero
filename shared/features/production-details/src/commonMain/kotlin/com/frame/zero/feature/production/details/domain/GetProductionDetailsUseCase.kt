package com.frame.zero.feature.production.details.domain

import com.frame.zero.domain.UseCase
import com.frame.zero.domain.production.ProductionDetail
import com.frame.zero.repository.productions.ProductionsRepository

class GetProductionDetailsUseCase(
  private val productionsRepository: ProductionsRepository
) : UseCase<GetProductionDetailsUseCase.Params, ProductionDetail>() {
  data class Params(
    val productionId: String
  )

  override suspend fun execute(params: Params): ProductionDetail = productionsRepository.getDetails(params.productionId)
}
