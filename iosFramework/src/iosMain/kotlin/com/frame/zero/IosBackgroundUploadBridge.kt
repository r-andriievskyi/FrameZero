package com.frame.zero

import com.frame.zero.core.upload.BackgroundUploadCompletion
import com.frame.zero.core.upload.BackgroundUrlSessionTaskUploadScheduler
import com.frame.zero.di.IosGraphHolder

/**
 * Swift entry point for `application(_:handleEventsForBackgroundURLSession:completionHandler:)`.
 * Stashes the OS completion handler, then ensures the background `NSURLSession` is alive so the
 * session re-delivers any events that completed while the app was dead — the delegate calls
 * [BackgroundUploadCompletion.complete] when it has drained them.
 *
 * A relaunch can reach this before any UI exists, which is why [IosGraphHolder] is lazy and
 * shared: the scheduler resolved here must be the same instance the UI later uses.
 */
object IosBackgroundUploadBridge {
  fun handleEventsForBackgroundSession(completionHandler: () -> Unit) {
    BackgroundUploadCompletion.setHandler(completionHandler)
    val scheduler = IosGraphHolder.graph.taskUploadScheduler
    (scheduler as? BackgroundUrlSessionTaskUploadScheduler)?.ensureSessionActive()
  }
}
