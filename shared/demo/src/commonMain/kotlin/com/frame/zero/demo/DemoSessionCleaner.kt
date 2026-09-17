package com.frame.zero.demo

import com.frame.zero.core.session.SessionCleaner

internal class DemoSessionCleaner(
  private val store: DemoDataStore
) : SessionCleaner {
  override suspend fun clear() {
    store.reset()
  }
}
