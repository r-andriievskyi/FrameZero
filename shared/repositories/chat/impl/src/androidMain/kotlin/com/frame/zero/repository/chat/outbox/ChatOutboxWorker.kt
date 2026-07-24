package com.frame.zero.repository.chat.outbox

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Drains one conversation's outbox in the background. Runs in the app process, so it shares the
 * singleton [ChatOutbox] — and therefore its per-conversation mutex — with any drain the UI
 * started.
 */
internal class ChatOutboxWorker(
  appContext: Context,
  params: WorkerParameters
) : CoroutineWorker(appContext, params),
  KoinComponent {
  private val outbox: ChatOutbox by inject()

  override suspend fun doWork(): Result {
    val conversationId = inputData.getString(KEY_CONVERSATION_ID) ?: return Result.failure()
    // A false drain means a transient failure with messages still queued; WorkManager's backoff is
    // exactly the retry schedule we want, and its network constraint gates the next attempt.
    return if (outbox.drain(conversationId)) Result.success() else Result.retry()
  }

  companion object {
    const val KEY_CONVERSATION_ID = "conversationId"
  }
}
