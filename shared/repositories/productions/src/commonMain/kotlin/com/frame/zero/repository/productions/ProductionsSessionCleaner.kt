package com.frame.zero.repository.productions

import com.frame.zero.core.session.SessionCleaner
import com.frame.zero.database.ProductionsDao

internal class ProductionsSessionCleaner(
  private val dao: ProductionsDao
) : SessionCleaner {
  override suspend fun clear() {
    dao.clearAll()
  }
}
