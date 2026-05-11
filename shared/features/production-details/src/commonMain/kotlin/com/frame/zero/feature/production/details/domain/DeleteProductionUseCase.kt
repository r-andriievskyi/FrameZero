package com.frame.zero.feature.production.details.domain

import com.frame.zero.domain.DomainError
import com.frame.zero.domain.UseCase
import com.frame.zero.repository.productions.ProductionsRepository
import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.io.IOException

class DeleteProductionUseCase(
  private val productionsRepository: ProductionsRepository
) : UseCase<DeleteProductionUseCase.Params, Unit>() {
  data class Params(
    val productionId: String
  )

  override fun mapError(throwable: Throwable): DomainError =
    when (throwable) {
      is IOException -> DomainError.Network(throwable.message ?: "Network error")
      is ResponseException -> {
        when (throwable.response.status) {
          HttpStatusCode.Forbidden -> DomainError.Unknown("Only the owner can delete this production")
          HttpStatusCode.NotFound -> DomainError.Unknown("Production not found")
          else -> DomainError.Unknown(throwable.message)
        }
      }
      else -> DomainError.Unknown(throwable.message)
    }

  override suspend fun execute(params: Params) {
    productionsRepository.delete(params.productionId)
  }
}
