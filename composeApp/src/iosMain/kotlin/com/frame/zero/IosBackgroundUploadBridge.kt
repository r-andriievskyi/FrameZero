package com.frame.zero

import com.frame.zero.core.upload.BackgroundUploadCompletion
import com.frame.zero.core.upload.BackgroundUrlSessionTaskUploadScheduler
import com.frame.zero.core.upload.TaskUploadScheduler
import com.frame.zero.di.initKoin

/**
 * Swift entry point for `application(_:handleEventsForBackgroundURLSession:completionHandler:)`.
 * Stashes the OS completion handler, then ensures Koin and the background `NSURLSession` are
 * alive so the session re-delivers any events that completed while the app was dead — the
 * delegate calls [BackgroundUploadCompletion.complete] when it has drained them.
 */
object IosBackgroundUploadBridge {
  fun handleEventsForBackgroundSession(completionHandler: () -> Unit) {
    BackgroundUploadCompletion.setHandler(completionHandler)
    val scheduler = initKoin().get<TaskUploadScheduler>()
    (scheduler as? BackgroundUrlSessionTaskUploadScheduler)?.ensureSessionActive()
  }
}
