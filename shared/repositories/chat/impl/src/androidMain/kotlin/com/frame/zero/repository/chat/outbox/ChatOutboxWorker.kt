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
    if (outbox.drain(conversationId)) return Result.success()
    // Capped, like TaskUploadWorker: an "offline" stop never spends a message's attempt budget, so
    // a device that reports CONNECTED while every request fails (captive portal, flapping VPN)
    // would otherwise wake the process forever. The rows stay queued for the next real trigger.
    return if (runAttemptCount + 1 < MAX_ATTEMPTS) Result.retry() else Result.failure()
  }

  companion object {
    const val KEY_CONVERSATION_ID = "conversationId"
    private const val MAX_ATTEMPTS = 4
  }
}
