package com.frame.zero.services

import com.frame.zero.AppError
import com.frame.zero.AppException
import com.frame.zero.domain.production.ProductionPhase
import com.frame.zero.domain.production.ProductionSort
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
import java.time.Clock
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.time.toKotlinInstant

class ProductionService(
  private val productionRepository: ProductionRepository,
  private val productionMemberRepository: ProductionMemberRepository,
  private val userRepository: UserRepository,
  private val productionAccessService: ProductionAccessService,
  private val clock: Clock = Clock.systemUTC()
) {
  suspend fun createProduction(
    userId: UUID,
    request: CreateProductionRequest
  ): ProductionDetailDto {
    validateCreateRequest(request)
    val production = productionRepository.create(
        title = request.title.trim(),
        genre = request.genre,
        logline = request.logline?.trim(),
        phase = request.phase,
        startDate = request.startDate.toJava(),
        wrapDate = request.wrapDate.toJava(),
        budgetCents = request.budgetCents,
        ownerUserId = userId
      )

    val owner = userRepository.findById(userId)
    productionMemberRepository.add(
      productionId = production.id,
      userId = userId,
      name = owner?.let { "${it.firstName} ${it.lastName}".trim() } ?: "",
      role = "Owner",
      email = owner?.email
    )

    request.crew.forEach { crewEntry ->
      val existingUser = crewEntry.email?.let { userRepository.findByEmail(it) }
      productionMemberRepository.add(
        productionId = production.id,
        userId = existingUser?.id,
        name = crewEntry.name.trim(),
        role = crewEntry.role.trim(),
        email = crewEntry.email
      )
    }

    return buildDetailDto(production)
  }

  suspend fun listProductions(
    userId: UUID,
    phases: List<ProductionPhase>,
    query: String?,
    sort: ProductionSort,
    limit: Int,
    cursor: String?
  ): Pair<List<ProductionSummaryDto>, String?> {
    val boundedLimit = limit.coerceIn(1, MAX_PAGE_SIZE)
    val (productions, nextCursor) = productionRepository.findAccessible(
      userId, phases, query, sort, boundedLimit, cursor
    )
    val today = LocalDate.now(clock)
    val summaries = productions.map { production ->
      production.toSummaryDto(productionMemberRepository.countByProduction(production.id), today)
    }
    return Pair(summaries, nextCursor)
  }

  suspend fun getProduction(
    userId: UUID,
    productionId: UUID
  ): ProductionDetailDto {
    productionAccessService.requireAccess(userId, productionId, AccessLevel.READ)
    val production = productionRepository.findById(productionId)
      ?: throw AppException(AppError.NotFound)
    return buildDetailDto(production)
  }

  suspend fun updateProduction(
    userId: UUID,
    productionId: UUID,
    request: UpdateProductionRequest
  ): ProductionDetailDto {
    productionAccessService.requireAccess(userId, productionId, AccessLevel.WRITE)
    validateUpdateRequest(request, productionId)
    val updatedProduction = productionRepository.update(
      id = productionId,
      title = request.title?.trim(),
      logline = request.logline?.trim(),
      startDate = request.startDate?.toJava(),
      wrapDate = request.wrapDate?.toJava(),
      budgetCents = request.budgetCents
    ) ?: throw AppException(AppError.NotFound)
    return buildDetailDto(updatedProduction)
  }

  suspend fun advancePhase(
    userId: UUID,
    productionId: UUID,
    request: PhaseTransitionRequest
  ): ProductionDetailDto {
    productionAccessService.requireAccess(userId, productionId, AccessLevel.WRITE)
    val production = productionRepository.findById(productionId)
      ?: throw AppException(AppError.NotFound)
    if (!request.phase.isForwardFrom(production.phase)) {
      throw AppException(AppError.InvalidPhaseTransition)
    }
    val updatedProduction = productionRepository.updatePhase(productionId, request.phase)
      ?: throw AppException(AppError.NotFound)
    return buildDetailDto(updatedProduction)
  }

  suspend fun deleteProduction(
    userId: UUID,
    productionId: UUID
  ) {
    productionAccessService.requireAccess(userId, productionId, AccessLevel.OWNER)
    productionRepository.softDelete(productionId)
  }

  suspend fun listMembers(
    userId: UUID,
    productionId: UUID
  ): List<ProductionMemberDto> {
    productionAccessService.requireAccess(userId, productionId, AccessLevel.READ)
    return productionMemberRepository.findByProduction(productionId).map { it.toDto() }
  }

  suspend fun addMember(
    userId: UUID,
    productionId: UUID,
    request: AddMemberRequest
  ): ProductionMemberDto {
    productionAccessService.requireAccess(userId, productionId, AccessLevel.WRITE)
    val errors = mutableMapOf<String, String>()
    if (request.name.isBlank()) errors["name"] = "Required"
    if (request.role.isBlank()) errors["role"] = "Required"
    if (errors.isNotEmpty()) throw AppException(AppError.ValidationError(errors))

    val existingUser = request.email?.let { userRepository.findByEmail(it) }
    val record = productionMemberRepository.add(
        productionId = productionId,
        userId = existingUser?.id,
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
    productionAccessService.requireAccess(userId, productionId, AccessLevel.WRITE)
    if (request.role.isBlank()) {
      throw AppException(AppError.ValidationError(mapOf("role" to "Required")))
    }
    return productionMemberRepository.updateRole(memberId, request.role.trim())?.toDto()
      ?: throw AppException(AppError.NotFound)
  }

  suspend fun removeMember(
    userId: UUID,
    productionId: UUID,
    memberId: UUID
  ) {
    productionAccessService.requireAccess(userId, productionId, AccessLevel.WRITE)
    val member = productionMemberRepository.findById(memberId)
      ?: throw AppException(AppError.NotFound)
    val production = productionRepository.findById(productionId)
      ?: throw AppException(AppError.NotFound)
    if (member.userId == production.ownerUserId) {
      throw AppException(AppError.Conflict("Cannot remove the production owner"))
    }
    productionMemberRepository.remove(memberId)
  }

  // -- Validation ----------------------------------------------------------

  private fun validateCreateRequest(request: CreateProductionRequest) {
    val errors = mutableMapOf<String, String>()
    val title = request.title
    val logline = request.logline
    val budgetCents = request.budgetCents
    if (title.isBlank()) errors["title"] = "Required"
    if (title.length > MAX_TITLE_LENGTH) errors["title"] = "Max $MAX_TITLE_LENGTH characters"
    if (logline != null && logline.length > MAX_LOGLINE_LENGTH) {
      errors["logline"] = "Max $MAX_LOGLINE_LENGTH characters"
    }
    if (request.wrapDate < request.startDate) errors["wrapDate"] = "Must be on or after startDate"
    if (budgetCents != null && budgetCents < 0) errors["budgetCents"] = "Must be >= 0"
    if (errors.isNotEmpty()) throw AppException(AppError.ValidationError(errors))
  }

  private suspend fun validateUpdateRequest(
    request: UpdateProductionRequest,
    productionId: UUID
  ) {
    val errors = mutableMapOf<String, String>()
    val title = request.title
    val logline = request.logline
    val budgetCents = request.budgetCents
    if (title != null && title.isBlank()) errors["title"] = "Cannot be empty"
    if (title != null && title.length > MAX_TITLE_LENGTH) {
      errors["title"] = "Max $MAX_TITLE_LENGTH characters"
    }
    if (logline != null && logline.length > MAX_LOGLINE_LENGTH) {
      errors["logline"] = "Max $MAX_LOGLINE_LENGTH characters"
    }
    if (budgetCents != null && budgetCents < 0) errors["budgetCents"] = "Must be >= 0"

    // Cross-field date validation: resolve effective start/wrap considering existing record
    if (request.startDate != null || request.wrapDate != null) {
      val existing = productionRepository.findById(productionId)
        ?: throw AppException(AppError.NotFound)
      val effectiveStart = request.startDate?.toJava() ?: existing.startDate
      val effectiveWrap = request.wrapDate?.toJava() ?: existing.wrapDate
      if (effectiveWrap < effectiveStart) {
        errors["wrapDate"] = "Must be on or after startDate"
      }
    }

    if (errors.isNotEmpty()) throw AppException(AppError.ValidationError(errors))
  }

  // -- DTO mapping ---------------------------------------------------------

  private suspend fun buildDetailDto(production: ProductionRecord): ProductionDetailDto {
    val allMembers = productionMemberRepository.findByProduction(production.id)
    val keyCrew = allMembers.sortedWith(keyCrewComparator).take(KEY_CREW_LIMIT).map { it.toDto() }
    val today = LocalDate.now(clock)
    return production.toDetailDto(membersCount = allMembers.size, keyCrew = keyCrew, today = today)
  }

  private fun ProductionRecord.toDetailDto(
    membersCount: Int,
    keyCrew: List<ProductionMemberDto>,
    today: LocalDate
  ): ProductionDetailDto {
    val progressPercent = computeProgressPercent(startDate, wrapDate, today)
    val daysUntilWrap = ChronoUnit.DAYS
      .between(today, wrapDate)
      .toInt()
    return ProductionDetailDto(
      id = id.toString(),
      title = title,
      genre = genre,
      logline = logline,
      phase = phase,
      progressPercent = progressPercent,
      daysLeft = daysUntilWrap,
      startDate = startDate.toKotlin(),
      wrapDate = wrapDate.toKotlin(),
      budgetCents = budgetCents,
      membersCount = membersCount,
      keyCrew = keyCrew,
      pipeline = buildPipelinePhases(phase),
      createdAt = createdAt.toKotlinInstant(),
      updatedAt = updatedAt.toKotlinInstant()
    )
  }

  private fun ProductionRecord.toSummaryDto(
    membersCount: Int,
    today: LocalDate
  ): ProductionSummaryDto {
    val progressPercent = computeProgressPercent(startDate, wrapDate, today)
    val daysUntilWrap = ChronoUnit.DAYS
      .between(today, wrapDate)
      .toInt()
    return ProductionSummaryDto(
      id = id.toString(),
      title = title,
      genre = genre,
      phase = phase,
      progressPercent = progressPercent,
      daysLeft = daysUntilWrap,
      membersCount = membersCount,
      updatedAt = updatedAt.toKotlinInstant()
    )
  }

  private fun ProductionMemberRecord.toDto(): ProductionMemberDto =
    ProductionMemberDto(
      id = id.toString(),
      userId = userId?.toString(),
      name = name,
      role = role,
      initials = extractInitials(name),
      avatarColorHex = avatarColorHex,
      addedAt = addedAt.toKotlinInstant()
    )

  private companion object {
    const val MAX_TITLE_LENGTH = 120
    const val MAX_LOGLINE_LENGTH = 280
    const val MAX_PAGE_SIZE = 50
    const val KEY_CREW_LIMIT = 6

    fun computeProgressPercent(
      start: LocalDate,
      wrap: LocalDate,
      today: LocalDate
    ): Int {
      if (!today.isAfter(start)) return 0
      if (!today.isBefore(wrap)) return 100
      val totalDays = ChronoUnit.DAYS
        .between(start, wrap)
        .coerceAtLeast(1)
      val elapsedDays = ChronoUnit.DAYS
        .between(start, today)
      return (elapsedDays * 100 / totalDays).toInt().coerceIn(0, 100)
    }

    fun buildPipelinePhases(currentPhase: ProductionPhase): List<PipelinePhaseDto> =
      ProductionPhase.entries.map { phase ->
        PipelinePhaseDto(
          phase = phase,
          label = phase.name
            .replace('_', ' ')
            .lowercase()
            .replaceFirstChar { it.uppercase() },
          isCompleted = phase.ordinal < currentPhase.ordinal,
          isCurrent = phase == currentPhase
        )
      }

    fun extractInitials(fullName: String): String {
      val parts = fullName.trim().split("\\s+".toRegex())
      return when {
        parts.size >= 2 ->
          "${parts.first().first().uppercaseChar()}${parts.last().first().uppercaseChar()}"
        parts.size == 1 && parts[0].isNotEmpty() ->
          parts[0].first().uppercaseChar().toString()
        else -> "?"
      }
    }

    val KEY_CREW_ROLE_ORDER = listOf(
      "Director",
      "Producer",
      "DP",
      "Director of Photography",
      "1st AD",
      "Production Designer"
    )

    val keyCrewComparator: Comparator<ProductionMemberRecord> =
      compareBy { member ->
        val index = KEY_CREW_ROLE_ORDER.indexOfFirst {
          member.role.equals(it, ignoreCase = true)
        }
        if (index >= 0) index else KEY_CREW_ROLE_ORDER.size
      }

    fun kotlinx.datetime.LocalDate.toJava(): LocalDate = LocalDate.of(year, month.number, day)
  }
}
