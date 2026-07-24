package com.frame.zero.repository.chat

import com.frame.zero.core.network.ChatSocketClient
import com.frame.zero.core.session.SessionCleaner
import com.frame.zero.database.ChatDao
import com.frame.zero.repository.chat.outbox.ChatOutboxScheduler

internal class ChatSessionCleaner(
  private val dao: ChatDao,
  private val socketClient: ChatSocketClient,
  private val outboxScheduler: ChatOutboxScheduler
) : SessionCleaner {
  override suspend fun clear() {
    socketClient.stop()
    // Cancel scheduled drains before wiping the rows, so no worker wakes up mid-clear and sends
    // the signed-out user's messages.
    outboxScheduler.cancelAll()
    dao.clearAll()
  }
}
