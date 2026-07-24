package com.frame.zero.repository.chat

import com.frame.zero.core.network.ChatSocketClient
import com.frame.zero.core.session.SessionCleaner
import com.frame.zero.database.ChatDao
import com.frame.zero.repository.chat.outbox.ChatOutbox
import com.frame.zero.repository.chat.outbox.ChatOutboxScheduler

internal class ChatSessionCleaner(
  private val dao: ChatDao,
  private val socketClient: ChatSocketClient,
  private val outbox: ChatOutbox,
  private val outboxScheduler: ChatOutboxScheduler
) : SessionCleaner {
  override suspend fun clear() {
    socketClient.stop()
    // Order matters. Cancel scheduled work so no worker wakes up mid-clear, then stop and *await*
    // the in-process delivery: a send still in flight would otherwise return after the wipe and
    // write the signed-out user's message body into the next user's database.
    outboxScheduler.cancelAll()
    outbox.shutdown()
    dao.clearAll()
  }
}
