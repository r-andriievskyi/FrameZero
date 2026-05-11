package com.frame.zero.domain.production

import com.frame.zero.dto.production.PipelinePhaseDto
import com.frame.zero.dto.production.ProductionDetailDto
import com.frame.zero.dto.production.ProductionMemberDto
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

data class ProductionDetail(
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
  val keyCrew: List<ProductionMember>,
  val pipeline: List<ProductionPipelinePhase>,
  val createdAt: Instant,
  val updatedAt: Instant
)

data class ProductionMember(
  val id: String,
  val userId: String?,
  val name: String,
  val role: String,
  val initials: String,
  val avatarColorHex: String?,
  val addedAt: Instant
)

data class ProductionPipelinePhase(
  val phase: ProductionPhase,
  val label: String,
  val isCompleted: Boolean,
  val isCurrent: Boolean
)

fun ProductionDetailDto.toProductionDetail(): ProductionDetail =
  ProductionDetail(
    id = id,
    title = title,
    genre = genre,
    logline = logline,
    phase = phase,
    progressPercent = progressPercent,
    daysLeft = daysLeft,
    startDate = startDate,
    wrapDate = wrapDate,
    budgetCents = budgetCents,
    membersCount = membersCount,
    keyCrew = keyCrew.map { it.toProductionMember() },
    pipeline = pipeline.map { it.toProductionPipelinePhase() },
    createdAt = createdAt,
    updatedAt = updatedAt
  )

fun ProductionMemberDto.toProductionMember(): ProductionMember =
  ProductionMember(
    id = id,
    userId = userId,
    name = name,
    role = role,
    initials = initials,
    avatarColorHex = avatarColorHex,
    addedAt = addedAt
  )

fun PipelinePhaseDto.toProductionPipelinePhase(): ProductionPipelinePhase =
  ProductionPipelinePhase(
    phase = phase,
    label = label,
    isCompleted = isCompleted,
    isCurrent = isCurrent
  )
