package com.frame.zero.dto.production

import com.frame.zero.domain.production.Genre
import com.frame.zero.domain.production.ProductionPhase
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class ProductionSummaryDto(
  val id: String,
  val title: String,
  val phase: ProductionPhase,
  val progressPercent: Int,
  val daysLeft: Int,
  val accentColorHint: AccentColorHint,
  val updatedAt: Instant,
)

@Serializable
data class ProductionDetailDto(
  val id: String,
  val title: String,
  val genre: Genre,
  val logline: String?,
  val phase: ProductionPhase,
  val progressPercent: Int,
  val daysLeft: Int,
  val startDate: LocalDate,
  val wrapDate: LocalDate,
  val budgetCents: Long?,
  val membersCount: Int,
  val keyCrew: List<ProductionMemberDto>,
  val pipeline: List<PipelinePhaseDto>,
  val createdAt: Instant,
  val updatedAt: Instant,
)

@Serializable
data class ProductionMemberDto(
  val id: String,
  val userId: String?,
  val name: String,
  val role: String,
  val initials: String,
  val avatarColorHex: String?,
  val addedAt: Instant,
)

@Serializable
data class PipelinePhaseDto(
  val phase: ProductionPhase,
  val label: String,
  val isCompleted: Boolean,
  val isCurrent: Boolean,
)

@Serializable
enum class AccentColorHint {
  GREEN,
  PURPLE,
  ORANGE,
}

@Serializable
data class CreateProductionRequest(
  val title: String,
  val genre: Genre,
  val logline: String? = null,
  val phase: ProductionPhase,
  val startDate: LocalDate,
  val wrapDate: LocalDate,
  val budgetCents: Long? = null,
  val crew: List<CreateCrewMemberDto> = emptyList(),
)

@Serializable
data class CreateCrewMemberDto(
  val name: String,
  val role: String,
  val email: String? = null,
)

@Serializable
data class UpdateProductionRequest(
  val title: String? = null,
  val logline: String? = null,
  val startDate: LocalDate? = null,
  val wrapDate: LocalDate? = null,
  val budgetCents: Long? = null,
)

@Serializable
data class PhaseTransitionRequest(val phase: ProductionPhase)

@Serializable
data class AddMemberRequest(
  val name: String,
  val role: String,
  val email: String? = null,
)

@Serializable
data class UpdateMemberRequest(val role: String)
