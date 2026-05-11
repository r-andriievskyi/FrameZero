package com.frame.zero.feature.production.details.domain

import com.frame.zero.domain.DomainError
import com.frame.zero.domain.UseCase
import com.frame.zero.domain.production.ProductionDetail
import com.frame.zero.domain.production.toProductionDetail
import com.frame.zero.repository.productions.ProductionsRepository
import io.ktor.client.plugins.ResponseException
import kotlinx.io.IOException

class GetProductionDetailsUseCase(
  private val productionsRepository: ProductionsRepository
) : UseCase<GetProductionDetailsUseCase.Params, ProductionDetail>() {
  data class Params(
    val productionId: String
  )

  override fun mapError(throwable: Throwable): DomainError =
    when (throwable) {
      is IOException -> DomainError.Network(throwable.message ?: "Network error")
      is ResponseException -> DomainError.Unknown(throwable.message)
      else -> DomainError.Unknown(throwable.message)
    }

  override suspend fun execute(params: Params): ProductionDetail =
    productionsRepository.getDetails(params.productionId).toProductionDetail()
}
