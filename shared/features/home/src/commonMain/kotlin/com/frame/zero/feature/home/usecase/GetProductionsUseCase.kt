package com.frame.zero.feature.home.usecase

import com.frame.zero.domain.DomainError
import com.frame.zero.domain.NoParamsUseCase
import com.frame.zero.domain.production.Production
import com.frame.zero.domain.production.toProduction
import com.frame.zero.repository.productions.ProductionsRepository
import io.ktor.client.plugins.ResponseException
import kotlinx.io.IOException

class GetProductionsUseCase(
  private val productionsRepository: ProductionsRepository
) : NoParamsUseCase<List<Production>>() {
  override fun mapError(throwable: Throwable): DomainError =
    when (throwable) {
      is IOException -> DomainError.Network(throwable.message ?: "Network error")
      is ResponseException -> DomainError.Unknown(throwable.message)
      else -> DomainError.Unknown(throwable.message)
    }

  override suspend fun execute(): List<Production> = productionsRepository.list().items.map { it.toProduction() }
}
