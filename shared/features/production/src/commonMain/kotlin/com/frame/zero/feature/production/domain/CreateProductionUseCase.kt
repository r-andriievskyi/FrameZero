package com.frame.zero.feature.production.domain

import com.frame.zero.domain.DomainError
import com.frame.zero.domain.UseCase
import com.frame.zero.domain.production.Genre
import com.frame.zero.domain.production.Production
import com.frame.zero.domain.production.ProductionPhase
import com.frame.zero.domain.production.toProduction
import com.frame.zero.dto.production.CreateProductionRequest
import com.frame.zero.repository.productions.ProductionsRepository
import io.ktor.client.plugins.ResponseException
import kotlinx.datetime.LocalDate
import kotlinx.io.IOException

class CreateProductionUseCase(
  private val productionsRepository: ProductionsRepository
) : UseCase<CreateProductionUseCase.Params, Production>() {
  data class Params(
    val title: String,
    val genre: Genre,
    val phase: ProductionPhase,
    val logline: String?,
    val startDate: LocalDate,
    val wrapDate: LocalDate
  )

  override fun mapError(throwable: Throwable): DomainError =
    when (throwable) {
      is IOException -> DomainError.Network(throwable.message ?: "Network error")
      is ResponseException -> DomainError.Unknown(throwable.message)
      else -> DomainError.Unknown(throwable.message)
    }

  override suspend fun execute(params: Params): Production =
    productionsRepository.createProduction(
      CreateProductionRequest(
        title = params.title,
        genre = params.genre,
        phase = params.phase,
        logline = params.logline?.ifBlank { null },
        startDate = params.startDate,
        wrapDate = params.wrapDate
      )
    ).toProduction()
}
