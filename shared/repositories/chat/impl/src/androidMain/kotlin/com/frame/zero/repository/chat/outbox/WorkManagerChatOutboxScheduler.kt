package com.frame.zero.repository.chat.outbox

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

/**
 * One unique work chain per conversation. [ExistingWorkPolicy.KEEP] rather than `REPLACE`: the
 * pending work already covers "drain this conversation", and replacing it would cancel a running
 * drain mid-send.
 */
internal class WorkManagerChatOutboxScheduler(
  private val context: Context
) : ChatOutboxScheduler {
  override suspend fun schedule(conversationId: String) {
    val request = OneTimeWorkRequestBuilder<ChatOutboxWorker>()
      .setInputData(workDataOf(ChatOutboxWorker.KEY_CONVERSATION_ID to conversationId))
      .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
      .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACKOFF_SECONDS, TimeUnit.SECONDS)
      .addTag(WORK_TAG)
      .build()
    WorkManager.getInstance(context)
      .enqueueUniqueWork(workName(conversationId), ExistingWorkPolicy.KEEP, request)
  }

  override suspend fun cancelAll() {
    WorkManager.getInstance(context).cancelAllWorkByTag(WORK_TAG)
  }

  private fun workName(conversationId: String): String = "$WORK_PREFIX$conversationId"

  private companion object {
    const val WORK_PREFIX = "chat-outbox-"
    const val WORK_TAG = "chat-outbox"
    const val BACKOFF_SECONDS = 10L
  }
}
