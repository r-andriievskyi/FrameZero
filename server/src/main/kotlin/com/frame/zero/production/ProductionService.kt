package com.frame.zero.production

import com.frame.zero.AppError
import com.frame.zero.AppException
import com.frame.zero.auth.UserRepository
import com.frame.zero.common.Transactor
import com.frame.zero.common.Validators
import com.frame.zero.common.parseUuidField
import com.frame.zero.common.computeProgressPercent
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
import com.frame.zero.dto.production.ViewerCrewDto
import java.util.UUID
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.todayIn

class ProductionService(
  private val productionRepository: ProductionRepository,
  private val productionMemberRepository: ProductionMemberRepository,
  private val userRepository: UserRepository,
  private val productionAccessService: ProductionAccessService,
  private val transactor: Transactor,
  private val clock: Clock = Clock.System
) {
  suspend fun createProduction(
    userId: UUID,
    request: CreateProductionRequest
  ): ProductionDetailDto =
    transactor.transaction {
      validateCreateRequest(request)
      val production = productionRepository.create(
        title = request.title.trim(),
        genre = request.genre,
        logline = request.logline?.trim(),
        phase = ProductionPhase.IDEA,
        startDate = request.startDate,
        wrapDate = request.wrapDate,
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
        val normalizedEmail = crewEntry.email?.trim()?.lowercase()
        val existingUser = normalizedEmail?.let { userRepository.findByEmail(it) }
        productionMemberRepository.add(
          productionId = production.id,
          userId = existingUser?.id,
          name = crewEntry.name.trim(),
          role = crewEntry.role.trim(),
          email = normalizedEmail
        )
      }

      buildDetailDto(production, userId)
    }

  suspend fun listProductions(
    userId: UUID,
    phases: List<ProductionPhase>,
    query: String?,
    sort: ProductionSort,
    limit: Int,
    cursor: String?
  ): Pair<List<ProductionSummaryDto>, String?> =
    transactor.transaction {
      val boundedLimit = limit.coerceIn(1, MAX_PAGE_SIZE)
      val (productions, nextCursor) = productionRepository.findAccessible(
        userId,
        phases,
        query,
        sort,
        boundedLimit,
        cursor
      )
      val today = clock.todayIn(TimeZone.UTC)
      val productionIds = productions.map { it.id }
      val membersCounts = productionMemberRepository.countByProductions(productionIds)
      val summaries = productions.map { production ->
        production.toSummaryDto(membersCounts[production.id] ?: 0, today)
      }
      Pair(summaries, nextCursor)
    }

  suspend fun getProduction(
    userId: UUID,
    productionId: UUID
  ): ProductionDetailDto =
    transactor.transaction {
      productionAccessService.requireAccess(userId, productionId, AccessLevel.READ)
      val production = productionRepository.findById(productionId)
        ?: throw AppException(AppError.NotFound)
      buildDetailDto(production, userId)
    }

  suspend fun updateProduction(
    userId: UUID,
    productionId: UUID,
    request: UpdateProductionRequest
  ): ProductionDetailDto =
    transactor.transaction {
      productionAccessService.requireAccess(userId, productionId, AccessLevel.WRITE)
      validateUpdateRequest(request, productionId)
      val updatedProduction = productionRepository.update(
        id = productionId,
        title = request.title?.trim(),
        logline = request.logline?.trim(),
        startDate = request.startDate,
        wrapDate = request.wrapDate,
        budgetCents = request.budgetCents
      ) ?: throw AppException(AppError.NotFound)
      buildDetailDto(updatedProduction, userId)
    }

  suspend fun advancePhase(
    userId: UUID,
    productionId: UUID,
    request: PhaseTransitionRequest
  ): ProductionDetailDto =
    transactor.transaction {
      productionAccessService.requireAccess(userId, productionId, AccessLevel.WRITE)
      val production = productionRepository.findById(productionId)
        ?: throw AppException(AppError.NotFound)
      if (!request.phase.isForwardFrom(production.phase)) {
        throw AppException(AppError.InvalidPhaseTransition)
      }
      val updatedProduction = productionRepository.updatePhase(productionId, request.phase)
        ?: throw AppException(AppError.NotFound)
      buildDetailDto(updatedProduction, userId)
    }

  suspend fun deleteProduction(
    userId: UUID,
    productionId: UUID
  ): Unit =
    transactor.transaction {
      productionAccessService.requireAccess(userId, productionId, AccessLevel.OWNER)
      productionRepository.softDelete(productionId)
    }

  suspend fun listMembers(
    userId: UUID,
    productionId: UUID
  ): List<ProductionMemberDto> =
    transactor.transaction {
      productionAccessService.requireAccess(userId, productionId, AccessLevel.READ)
      productionMemberRepository.findByProduction(productionId).map { it.toDto() }
    }

  suspend fun addMember(
    userId: UUID,
    productionId: UUID,
    request: AddMemberRequest
  ): ProductionMemberDto =
    transactor.transaction {
      productionAccessService.requireAccess(userId, productionId, AccessLevel.WRITE)
      val errors = mutableMapOf<String, String>()
      validateMemberFields(request.name, request.role, request.email, errors, fieldPrefix = "")
      if (errors.isNotEmpty()) throw AppException(AppError.ValidationError(errors))

      val normalizedEmail = request.email?.trim()?.lowercase()
      val existingUser = normalizedEmail?.let { userRepository.findByEmail(it) }
      val record = productionMemberRepository.add(
        productionId = productionId,
        userId = existingUser?.id,
        name = request.name.trim(),
        role = request.role.trim(),
        email = normalizedEmail
      )
      record.toDto()
    }

  suspend fun updateMember(
    userId: UUID,
    productionId: UUID,
    memberId: UUID,
    request: UpdateMemberRequest
  ): ProductionMemberDto =
    transactor.transaction {
      productionAccessService.requireAccess(userId, productionId, AccessLevel.WRITE)
      val role = request.role
      val reportsToMemberId = request.reportsToMemberId
      if (role == null && reportsToMemberId == null) {
        throw AppException(
          AppError.ValidationError(mapOf("body" to "Provide role or reportsToMemberId"))
        )
      }
      if (role != null && role.isBlank()) {
        throw AppException(AppError.ValidationError(mapOf("role" to "Required")))
      }
      val parsedReportsTo = reportsToMemberId?.let { parseUuidField("reportsToMemberId", it) }
      if (parsedReportsTo != null && parsedReportsTo == memberId) {
        throw AppException(
          AppError.ValidationError(mapOf("reportsToMemberId" to "Cannot report to self"))
        )
      }
      var current = productionMemberRepository.findById(memberId)
        ?: throw AppException(AppError.NotFound)
      if (current.productionId != productionId) {
        throw AppException(AppError.NotFound)
      }
      if (role != null) {
        current = productionMemberRepository.updateRole(memberId, role.trim())
          ?: throw AppException(AppError.NotFound)
      }
      if (parsedReportsTo != null) {
        val target = productionMemberRepository.findById(parsedReportsTo)
          ?: throw AppException(
            AppError.ValidationError(mapOf("reportsToMemberId" to "Unknown member"))
          )
        if (target.productionId != productionId) {
          throw AppException(
            AppError.ValidationError(mapOf("reportsToMemberId" to "Member is on a different production"))
          )
        }
        current = productionMemberRepository.updateReportsTo(memberId, parsedReportsTo)
          ?: throw AppException(AppError.NotFound)
      }
      current.toDto()
    }

  suspend fun removeMember(
    userId: UUID,
    productionId: UUID,
    memberId: UUID
  ): Unit =
    transactor.transaction {
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
    request.crew.forEachIndexed { index, crewEntry ->
      validateMemberFields(
        name = crewEntry.name,
        role = crewEntry.role,
        email = crewEntry.email,
        errors = errors,
        fieldPrefix = "crew[$index]."
      )
    }
    if (errors.isNotEmpty()) throw AppException(AppError.ValidationError(errors))
  }

  private fun validateMemberFields(
    name: String,
    role: String,
    email: String?,
    errors: MutableMap<String, String>,
    fieldPrefix: String
  ) {
    if (name.isBlank()) errors["${fieldPrefix}name"] = "Required"
    if (name.trim().length > MAX_MEMBER_NAME_LENGTH) {
      errors["${fieldPrefix}name"] = "Max $MAX_MEMBER_NAME_LENGTH characters"
    }
    if (role.isBlank()) errors["${fieldPrefix}role"] = "Required"
    if (role.trim().length > MAX_MEMBER_ROLE_LENGTH) {
      errors["${fieldPrefix}role"] = "Max $MAX_MEMBER_ROLE_LENGTH characters"
    }
    val trimmedEmail = email?.trim()
    if (trimmedEmail != null) {
      if (trimmedEmail.length > MAX_MEMBER_EMAIL_LENGTH) {
        errors["${fieldPrefix}email"] = "Max $MAX_MEMBER_EMAIL_LENGTH characters"
      } else if (!Validators.isValidEmail(trimmedEmail)) {
        errors["${fieldPrefix}email"] = "Invalid email format"
      }
    }
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
      val effectiveStart = request.startDate ?: existing.startDate
      val effectiveWrap = request.wrapDate ?: existing.wrapDate
      if (effectiveWrap < effectiveStart) {
        errors["wrapDate"] = "Must be on or after startDate"
      }
    }

    if (errors.isNotEmpty()) throw AppException(AppError.ValidationError(errors))
  }

  // -- DTO mapping ---------------------------------------------------------

  private suspend fun buildDetailDto(
    production: ProductionRecord,
    viewerUserId: UUID
  ): ProductionDetailDto {
    val allMembers = productionMemberRepository.findByProduction(production.id)
    val keyCrew = allMembers.sortedWith(keyCrewComparator).take(KEY_CREW_LIMIT).map { it.toDto() }
    val viewerCrew = buildViewerCrew(viewerUserId, allMembers)
    val today = clock.todayIn(TimeZone.UTC)
    return production.toDetailDto(
      membersCount = allMembers.size,
      keyCrew = keyCrew,
      viewerCrew = viewerCrew,
      today = today
    )
  }

  private fun buildViewerCrew(
    viewerUserId: UUID,
    allMembers: List<ProductionMemberRecord>
  ): ViewerCrewDto? {
    val viewer = allMembers.firstOrNull { it.userId == viewerUserId } ?: return null
    val manager = viewer.reportsToMemberId?.let { managerId ->
      allMembers.firstOrNull { it.id == managerId }
    }
    val peers = viewer.reportsToMemberId?.let { managerId ->
      allMembers.filter { it.id != viewer.id && it.reportsToMemberId == managerId }
    }.orEmpty()
    val reports = allMembers.filter { it.reportsToMemberId == viewer.id }
    if (manager == null && peers.isEmpty() && reports.isEmpty()) return null
    return ViewerCrewDto(
      viewer = viewer.toDto(),
      manager = manager?.toDto(),
      peers = peers.map { it.toDto() },
      reports = reports.map { it.toDto() }
    )
  }

  private fun ProductionRecord.toDetailDto(
    membersCount: Int,
    keyCrew: List<ProductionMemberDto>,
    viewerCrew: ViewerCrewDto?,
    today: LocalDate
  ): ProductionDetailDto {
    val progressPercent = computeProgressPercent(startDate, wrapDate, today)
    val daysUntilWrap = today.daysUntil(wrapDate)
    return ProductionDetailDto(
      id = id.toString(),
      title = title,
      genre = genre,
      logline = logline,
      phase = phase,
      progressPercent = progressPercent,
      daysLeft = daysUntilWrap,
      startDate = startDate,
      wrapDate = wrapDate,
      budgetCents = budgetCents,
      membersCount = membersCount,
      keyCrew = keyCrew,
      pipeline = buildPipelinePhases(phase),
      createdAt = createdAt,
      updatedAt = updatedAt,
      viewerCrew = viewerCrew
    )
  }

  private fun ProductionRecord.toSummaryDto(
    membersCount: Int,
    today: LocalDate
  ): ProductionSummaryDto {
    val progressPercent = computeProgressPercent(startDate, wrapDate, today)
    val daysUntilWrap = today.daysUntil(wrapDate)
    return ProductionSummaryDto(
      id = id.toString(),
      title = title,
      genre = genre,
      phase = phase,
      progressPercent = progressPercent,
      daysLeft = daysUntilWrap,
      membersCount = membersCount,
      updatedAt = updatedAt
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
      addedAt = addedAt,
      reportsToMemberId = reportsToMemberId?.toString()
    )

  private companion object {
    const val MAX_TITLE_LENGTH = 120
    const val MAX_LOGLINE_LENGTH = 280
    const val MAX_PAGE_SIZE = 50
    const val KEY_CREW_LIMIT = 6

    // Match the column sizes in ProductionMembersTable.
    const val MAX_MEMBER_NAME_LENGTH = 200
    const val MAX_MEMBER_ROLE_LENGTH = 100
    const val MAX_MEMBER_EMAIL_LENGTH = 320

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
  }
}
