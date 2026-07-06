package com.frame.zero.feature.task.details.usecase

import com.frame.zero.domain.UseCase
import com.frame.zero.domain.task.AssignableMember
import com.frame.zero.domain.production.ProductionMember
import com.frame.zero.repository.productions.ProductionsRepository

/**
 * Candidates for the participants picker: production members backed by a real user account,
 * same filtering rule as task-create's assignee picker (kept as a module-local copy — task-create
 * and task-details are separate feature modules with no dependency between them).
 */
class GetAssignableMembersUseCase(
  private val productionsRepository: ProductionsRepository
) : UseCase<GetAssignableMembersUseCase.Params, List<AssignableMember>>() {
  data class Params(
    val productionId: String
  )

  override suspend fun execute(params: Params): List<AssignableMember> =
    productionsRepository
      .listMembers(params.productionId)
      .filter { it.userId != null }
      .distinctBy { it.userId }
      .map { it.toAssignableMember() }
}

private fun ProductionMember.toAssignableMember(): AssignableMember =
  AssignableMember(
    userId = requireNotNull(userId),
    name = name,
    initials = initials,
    avatarColorHex = avatarColorHex
  )
