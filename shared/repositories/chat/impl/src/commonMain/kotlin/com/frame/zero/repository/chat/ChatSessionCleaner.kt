package com.frame.zero.repository.chat

import com.frame.zero.core.network.ChatSocketClient
import com.frame.zero.core.session.SessionCleaner
import com.frame.zero.database.ChatDao

internal class ChatSessionCleaner(
  private val dao: ChatDao,
  private val socketClient: ChatSocketClient
) : SessionCleaner {
  override suspend fun clear() {
    socketClient.stop()
    dao.clearAll()
  }
}
