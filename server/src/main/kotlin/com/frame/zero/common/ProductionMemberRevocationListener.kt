package com.frame.zero.common

import java.util.UUID

/**
 * Hook the production layer calls after a removal ends a user's membership in a
 * production, so a live chat hub can drop that user's subscriptions to the
 * production's conversations. Kept as an interface in `common` — with a [NONE]
 * no-op — so the production module never depends on chat internals and tests that
 * don't care about chat can pass the no-op.
 */
fun interface ProductionMemberRevocationListener {
  suspend fun onProductionMemberRemoved(
    productionId: UUID,
    userId: UUID
  )

  companion object {
    val NONE = ProductionMemberRevocationListener { _, _ -> }
  }
}
