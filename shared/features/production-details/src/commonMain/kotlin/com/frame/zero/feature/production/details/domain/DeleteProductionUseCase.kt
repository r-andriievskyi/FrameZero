package com.frame.zero.feature.production.details.domain

import com.frame.zero.domain.UseCase
import com.frame.zero.repository.productions.ProductionsRepository

class DeleteProductionUseCase(
  private val productionsRepository: ProductionsRepository
) : UseCase<DeleteProductionUseCase.Params, Unit>() {
  data class Params(
    val productionId: String
  )

  override suspend fun execute(params: Params) {
    productionsRepository.delete(params.productionId)
  }
}
