package com.frame.zero.feature.task.create.domain

import com.frame.zero.domain.UseCase
import com.frame.zero.domain.task.AssignableMember
import com.frame.zero.domain.production.ProductionMember
import com.frame.zero.repository.productions.ProductionsRepository

class GetAssignableMembersUseCase(
  private val productionsRepository: ProductionsRepository
) : UseCase<GetAssignableMembersUseCase.Params, List<AssignableMember>>() {
  data class Params(
    val productionId: String
  )

  // Only members backed by a real user account can be assignees server-side, so
  // unlinked crew are filtered out. Deduplicated by userId.
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
