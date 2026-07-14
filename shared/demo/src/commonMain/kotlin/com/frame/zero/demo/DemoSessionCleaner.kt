package com.frame.zero.demo

import com.frame.zero.core.session.SessionCleaner
import com.frame.zero.demo.data.DemoChatRepository

internal class DemoSessionCleaner(
  private val store: DemoDataStore,
  private val chatRepository: DemoChatRepository
) : SessionCleaner {
  override suspend fun clear() {
    chatRepository.cancelPendingReplies()
    store.reset()
  }
}
