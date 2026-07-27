package com.frame.zero.repository.chat.outbox

import com.frame.zero.core.session.SessionManager
import com.frame.zero.core.session.SessionState
import com.frame.zero.repository.chat.ChatRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch

/**
 * Flushes the outbox at app start, not just when a chat is opened: a message queued before the
 * process died must go out whether or not the user navigates back to that conversation.
 *
 * Created eagerly by Koin and kept alive for the process. Waits for a signed-in session — there is
 * nobody to send as before that, and a signed-out start must not touch the database at all.
 */
internal class ChatOutboxStarter(
  private val sessionManager: SessionManager,
  private val chatRepository: ChatRepository,
  scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) {
  init {
    scope.launch {
      sessionManager.state
        .filterIsInstance<SessionState.LoggedIn>()
        .collect { chatRepository.flushOutbox() }
    }
  }
}
