package com.frame.zero.repository.productions

import com.frame.zero.core.session.SessionCleaner
import com.frame.zero.database.ProductionsDao
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn

@SingleIn(AppScope::class)
@ContributesIntoSet(AppScope::class)
@Inject
class ProductionsSessionCleaner(
  private val dao: ProductionsDao
) : SessionCleaner {
  override suspend fun clear() {
    dao.clearAll()
  }
}
