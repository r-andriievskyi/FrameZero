package com.frame.zero.feature.production.details

import com.frame.zero.domain.production.ProductionPhase
import kotlinx.collections.immutable.ImmutableList

/**
 * Presentation model for the production-details header/cards, with budget and dates already
 * resolved to display strings by the ViewModel. Keeps the domain [com.frame.zero.domain.production.ProductionDetail]
 * out of Compose (stability + layering).
 */
data class ProductionDetailUi(
  val title: String,
  val logline: String?,
  val phase: ProductionPhase,
  val progressPercent: Int,
  val daysLeft: Int,
  val membersCount: Int,
  val budgetLabel: String,
  val startDateLabel: String,
  val wrapDateLabel: String,
  val pipeline: ImmutableList<ProductionPipelinePhaseUi>,
  val viewerCrew: ViewerCrewUi?
)

data class ProductionPipelinePhaseUi(
  val phase: ProductionPhase,
  val label: String,
  val isCompleted: Boolean,
  val isCurrent: Boolean
)

data class ViewerCrewUi(
  val viewerRole: String,
  val manager: ProductionMemberUi?,
  val peers: ImmutableList<ProductionMemberUi>,
  val reports: ImmutableList<ProductionMemberUi>
)

data class ProductionMemberUi(
  val id: String,
  val name: String,
  val role: String,
  val initials: String,
  val avatarColorHex: String?
)
