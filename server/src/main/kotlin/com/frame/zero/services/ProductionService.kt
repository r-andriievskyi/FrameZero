package com.frame.zero.services

import com.frame.zero.AppError
import com.frame.zero.AppException
import com.frame.zero.domain.production.ProductionPhase
import com.frame.zero.domain.production.ProductionSort
import com.frame.zero.dto.production.AccentColorHint
import com.frame.zero.dto.production.AddMemberRequest
import com.frame.zero.dto.production.CreateProductionRequest
import com.frame.zero.dto.production.PhaseTransitionRequest
import com.frame.zero.dto.production.PipelinePhaseDto
import com.frame.zero.dto.production.ProductionDetailDto
import com.frame.zero.dto.production.ProductionMemberDto
import com.frame.zero.dto.production.ProductionSummaryDto
import com.frame.zero.dto.production.UpdateMemberRequest
import com.frame.zero.dto.production.UpdateProductionRequest
import com.frame.zero.repository.ProductionMemberRecord
import com.frame.zero.repository.ProductionMemberRepository
import com.frame.zero.repository.ProductionRecord
import com.frame.zero.repository.ProductionRepository
import com.frame.zero.repository.UserRepository
import com.frame.zero.util.toKotlin
import kotlinx.datetime.number
import java.time.LocalDate
import java.util.UUID
import kotlin.time.toKotlinInstant

class ProductionService(
  private val productions: ProductionRepository,
  private val members: ProductionMemberRepository,
  private val users: UserRepository,
  private val access: ProductionAccessService
) {
  suspend fun create(
    userId: UUID,
    request: CreateProductionRequest
  ): ProductionDetailDto {
    validate(request)
    val production =
      productions.create(
        title = request.title.trim(),
        genre = request.genre,
        logline = request.logline?.trim(),
        phase = request.phase,
        startDate = request.startDate.toJava(),
        wrapDate = request.wrapDate.toJava(),
        budgetCents = request.budgetCents,
        ownerUserId = userId
      )

    val owner = users.findById(userId)
    members.add(
      productionId = production.id,
      userId = userId,
      name = owner?.let { "${it.firstName} ${it.lastName}".trim() } ?: "",
      role = "Owner",
      email = owner?.email
    )

    request.crew.forEach { crew ->
      val linkedUser = crew.email?.let { users.findByEmail(it) }
      members.add(
        productionId = production.id,
        userId = linkedUser?.id,
        name = crew.name.trim(),
        role = crew.role.trim(),
        email = crew.email
      )
    }

    return detailDto(production, userId)
  }

  suspend fun list(
    userId: UUID,
    phases: List<ProductionPhase>,
    query: String?,
    sort: ProductionSort,
    limit: Int,
    cursor: String?
  ): Pair<List<ProductionSummaryDto>, String?> {
    val (items, nextCursor) = productions.findAccessible(userId, phases, query, sort, limit, cursor)
    val summaries = buildList {
      for (item in items) {
        add(item.toSummaryDto(members.countByProduction(item.id)))
      }
    }
    return Pair(summaries, nextCursor)
  }

  suspend fun get(
    userId: UUID,
    productionId: UUID
  ): ProductionDetailDto {
    access.requireAccess(userId, productionId, AccessLevel.READ)
    val production = productions.findById(productionId) ?: throw AppException(AppError.NotFound)
    return detailDto(production, userId)
  }

  suspend fun update(
    userId: UUID,
    productionId: UUID,
    request: UpdateProductionRequest
  ): ProductionDetailDto {
    access.requireAccess(userId, productionId, AccessLevel.WRITE)
    validate(request)
    val updated =
      productions.update(
        id = productionId,
        title = request.title?.trim(),
        logline = request.logline?.trim(),
        startDate = request.startDate?.toJava(),
        wrapDate = request.wrapDate?.toJava(),
        budgetCents = request.budgetCents
      ) ?: throw AppException(AppError.NotFound)
    return detailDto(updated, userId)
  }

  suspend fun transitionPhase(
    userId: UUID,
    productionId: UUID,
    request: PhaseTransitionRequest
  ): ProductionDetailDto {
    access.requireAccess(userId, productionId, AccessLevel.WRITE)
    val production = productions.findById(productionId) ?: throw AppException(AppError.NotFound)
    if (!request.phase.isForwardFrom(production.phase)) {
      throw AppException(AppError.InvalidPhaseTransition)
    }
    val updated =
      productions.updatePhase(productionId, request.phase) ?: throw AppException(AppError.NotFound)
    return detailDto(updated, userId)
  }

  suspend fun delete(
    userId: UUID,
    productionId: UUID
  ) {
    access.requireAccess(userId, productionId, AccessLevel.OWNER)
    productions.softDelete(productionId)
  }

  suspend fun listMembers(
    userId: UUID,
    productionId: UUID
  ): List<ProductionMemberDto> {
    access.requireAccess(userId, productionId, AccessLevel.READ)
    return members.findByProduction(productionId).map { it.toDto() }
  }

  suspend fun addMember(
    userId: UUID,
    productionId: UUID,
    request: AddMemberRequest
  ): ProductionMemberDto {
    access.requireAccess(userId, productionId, AccessLevel.WRITE)
    val errors = mutableMapOf<String, String>()
    if (request.name.isBlank()) errors["name"] = "Required"
    if (request.role.isBlank()) errors["role"] = "Required"
    if (errors.isNotEmpty()) throw AppException(AppError.ValidationError(errors))

    val linkedUser = request.email?.let { users.findByEmail(it) }
    val record =
      members.add(
        productionId = productionId,
        userId = linkedUser?.id,
        name = request.name.trim(),
        role = request.role.trim(),
        email = request.email
      )
    return record.toDto()
  }

  suspend fun updateMemberRole(
    userId: UUID,
    productionId: UUID,
    memberId: UUID,
    request: UpdateMemberRequest
  ): ProductionMemberDto {
    access.requireAccess(userId, productionId, AccessLevel.WRITE)
    if (request.role.isBlank()) {
      throw AppException(AppError.ValidationError(mapOf("role" to "Required")))
    }
    return members.updateRole(memberId, request.role.trim())?.toDto()
      ?: throw AppException(AppError.NotFound)
  }

  suspend fun removeMember(
    userId: UUID,
    productionId: UUID,
    memberId: UUID
  ) {
    access.requireAccess(userId, productionId, AccessLevel.WRITE)
    val member = members.findById(memberId) ?: throw AppException(AppError.NotFound)
    val production = productions.findById(productionId) ?: throw AppException(AppError.NotFound)
    if (member.userId == production.ownerUserId) {
      throw AppException(AppError.Conflict("Cannot remove the production owner"))
    }
    members.remove(memberId)
  }

  private fun validate(request: CreateProductionRequest) {
    val errors = mutableMapOf<String, String>()
    val title = request.title
    val logline = request.logline
    val budgetCents = request.budgetCents
    if (title.isBlank()) errors["title"] = "Required"
    if (title.length > 120) errors["title"] = "Max 120 characters"
    if (logline != null && logline.length > 280) errors["logline"] = "Max 280 characters"
    if (request.wrapDate < request.startDate) errors["wrapDate"] = "Must be on or after startDate"
    if (budgetCents != null && budgetCents < 0) errors["budgetCents"] = "Must be >= 0"
    if (errors.isNotEmpty()) throw AppException(AppError.ValidationError(errors))
  }

  private fun validate(request: UpdateProductionRequest) {
    val errors = mutableMapOf<String, String>()
    val title = request.title
    val logline = request.logline
    val budgetCents = request.budgetCents
    if (title != null && title.isBlank()) errors["title"] = "Cannot be empty"
    if (title != null && title.length > 120) errors["title"] = "Max 120 characters"
    if (logline != null && logline.length > 280) errors["logline"] = "Max 280 characters"
    if (budgetCents != null && budgetCents < 0) errors["budgetCents"] = "Must be >= 0"
    if (errors.isNotEmpty()) throw AppException(AppError.ValidationError(errors))
  }

  private suspend fun detailDto(
    production: ProductionRecord,
    userId: UUID
  ): ProductionDetailDto {
    val allMembers = members.findByProduction(production.id)
    val membersCount = allMembers.size
    val keyCrew = allMembers.sortedWith(rolePriority).take(6).map { it.toDto() }
    return production.toDetailDto(membersCount = membersCount, keyCrew = keyCrew)
  }

  private fun ProductionRecord.toDetailDto(
    membersCount: Int,
    keyCrew: List<ProductionMemberDto>
  ): ProductionDetailDto {
    val today = LocalDate.now()
    val progress = computeProgress(startDate, wrapDate, today)
    val daysLeft =
      java.time.temporal.ChronoUnit.DAYS
        .between(today, wrapDate)
        .toInt()
    return ProductionDetailDto(
      id = id.toString(),
      title = title,
      genre = genre,
      logline = logline,
      phase = phase,
      progressPercent = progress,
      daysLeft = daysLeft,
      startDate = startDate.toKotlin(),
      wrapDate = wrapDate.toKotlin(),
      budgetCents = budgetCents,
      membersCount = membersCount,
      keyCrew = keyCrew,
      pipeline = buildPipeline(phase),
      createdAt = createdAt.toKotlinInstant(),
      updatedAt = updatedAt.toKotlinInstant()
    )
  }

  private fun ProductionRecord.toSummaryDto(membersCount: Int): ProductionSummaryDto {
    val today = LocalDate.now()
    val progress = computeProgress(startDate, wrapDate, today)
    val daysLeft =
      java.time.temporal.ChronoUnit.DAYS
        .between(today, wrapDate)
        .toInt()
    return ProductionSummaryDto(
      id = id.toString(),
      title = title,
      genre = genre,
      phase = phase,
      progressPercent = progress,
      daysLeft = daysLeft,
      membersCount = membersCount,
      accentColorHint = phase.toAccentHint(),
      updatedAt = updatedAt.toKotlinInstant()
    )
  }

  private fun ProductionMemberRecord.toDto(): ProductionMemberDto =
    ProductionMemberDto(
      id = id.toString(),
      userId = userId?.toString(),
      name = name,
      role = role,
      initials = initialsFrom(name),
      avatarColorHex = avatarColorHex,
      addedAt = addedAt.toKotlinInstant()
    )

  private companion object {
    fun computeProgress(
      start: LocalDate,
      wrap: LocalDate,
      today: LocalDate
    ): Int {
      if (!today.isAfter(start)) return 0
      if (!today.isBefore(wrap)) return 100
      val total =
        java.time.temporal.ChronoUnit.DAYS
          .between(start, wrap)
          .coerceAtLeast(1)
      val elapsed =
        java.time.temporal.ChronoUnit.DAYS
          .between(start, today)
      return (elapsed * 100 / total).toInt().coerceIn(0, 100)
    }

    fun buildPipeline(current: ProductionPhase): List<PipelinePhaseDto> =
      ProductionPhase.entries.map { p ->
        PipelinePhaseDto(
          phase = p,
          label =
            p.name
              .replace('_', ' ')
              .lowercase()
              .replaceFirstChar { it.uppercase() },
          isCompleted = p.ordinal < current.ordinal,
          isCurrent = p == current
        )
      }

    fun ProductionPhase.toAccentHint(): AccentColorHint =
      when (this) {
        ProductionPhase.DEVELOPMENT -> AccentColorHint.GREEN
        ProductionPhase.PRE_PRODUCTION -> AccentColorHint.ORANGE
        ProductionPhase.PRODUCTION -> AccentColorHint.ORANGE
        ProductionPhase.POST_PRODUCTION -> AccentColorHint.PURPLE
        ProductionPhase.DISTRIBUTION -> AccentColorHint.GREEN
      }

    fun initialsFrom(name: String): String {
      val parts = name.trim().split("\\s+".toRegex())
      return when {
        parts.size >= 2 ->
          "${parts.first().first().uppercaseChar()}${parts.last().first().uppercaseChar()}"
        parts.size == 1 && parts[0].isNotEmpty() -> parts[0].first().uppercaseChar().toString()
        else -> "?"
      }
    }

    val roleOrder =
      listOf(
        "Director",
        "Producer",
        "DP",
        "Director of Photography",
        "1st AD",
        "Production Designer"
      )

    val rolePriority: Comparator<ProductionMemberRecord> =
      compareBy { member ->
        val idx = roleOrder.indexOfFirst { member.role.equals(it, ignoreCase = true) }
        if (idx >= 0) idx else roleOrder.size
      }

    fun kotlinx.datetime.LocalDate.toJava(): LocalDate = LocalDate.of(year, month.number, day)
  }
}
