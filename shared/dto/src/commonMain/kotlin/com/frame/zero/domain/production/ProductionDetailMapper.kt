package com.frame.zero.domain.production

import com.frame.zero.core.collections.mapImmutable
import com.frame.zero.dto.production.PipelinePhaseDto
import com.frame.zero.dto.production.ProductionDetailDto
import com.frame.zero.dto.production.ProductionMemberDto
import com.frame.zero.dto.production.ViewerCrewDto

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
    pipeline = pipeline.mapImmutable { it.toProductionPipelinePhase() },
    createdAt = createdAt,
    updatedAt = updatedAt,
    viewerCrew = viewerCrew?.toViewerCrew()
  )

fun ViewerCrewDto.toViewerCrew(): ViewerCrew =
  ViewerCrew(
    viewer = viewer.toProductionMember(),
    manager = manager?.toProductionMember(),
    peers = peers.map { it.toProductionMember() },
    reports = reports.map { it.toProductionMember() }
  )

fun ProductionMemberDto.toProductionMember(): ProductionMember =
  ProductionMember(
    id = id,
    userId = userId,
    name = name,
    role = role,
    initials = initials,
    avatarColorHex = avatarColorHex,
    addedAt = addedAt,
    reportsToMemberId = reportsToMemberId
  )

fun PipelinePhaseDto.toProductionPipelinePhase(): ProductionPipelinePhase =
  ProductionPipelinePhase(
    phase = phase,
    label = label,
    isCompleted = isCompleted,
    isCurrent = isCurrent
  )
