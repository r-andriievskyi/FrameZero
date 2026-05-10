package com.frame.zero.production

import com.frame.zero.AppError
import com.frame.zero.AppException
import java.util.UUID

enum class AccessLevel {
  READ,
  WRITE,
  OWNER
}

class ProductionAccessService(
  private val productions: ProductionRepository,
  private val members: ProductionMemberRepository
) {
  suspend fun requireAccess(
    userId: UUID,
    productionId: UUID,
    level: AccessLevel
  ) {
    val production = productions.findById(productionId) ?: throw AppException(AppError.NotFound)

    when (level) {
      AccessLevel.OWNER -> {
        if (production.ownerUserId != userId) throw AppException(AppError.Forbidden)
      }
      AccessLevel.READ,
      AccessLevel.WRITE -> {
        if (production.ownerUserId == userId) return
        if (!members.isMember(userId, productionId)) throw AppException(AppError.Forbidden)
      }
    }
  }
}
