package com.frame.zero.domain.production

import kotlinx.collections.immutable.ImmutableList
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
  val pipeline: ImmutableList<ProductionPipelinePhase>,
  val createdAt: Instant,
  val updatedAt: Instant,
  val viewerCrew: ViewerCrew?
)

data class ViewerCrew(
  val viewer: ProductionMember,
  val manager: ProductionMember?,
  val peers: List<ProductionMember>,
  val reports: List<ProductionMember>
)

data class ProductionMember(
  val id: String,
  val userId: String?,
  val name: String,
  val role: String,
  val initials: String,
  val avatarColorHex: String?,
  val addedAt: Instant,
  val reportsToMemberId: String?
)

data class ProductionPipelinePhase(
  val phase: ProductionPhase,
  val label: String,
  val isCompleted: Boolean,
  val isCurrent: Boolean
)
