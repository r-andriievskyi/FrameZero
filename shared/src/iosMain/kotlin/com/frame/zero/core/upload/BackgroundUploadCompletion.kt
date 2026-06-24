package com.frame.zero.core.upload

import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

/**
 * Holds the system-supplied completion handler from
 * `application(_:handleEventsForBackgroundURLSession:completionHandler:)`. The Swift
 * AppDelegate stashes it here on a background relaunch; the background `NSURLSession`
 * delegate calls [complete] once it has finished delivering all queued events, which lets
 * the OS snapshot the UI and stop giving the app background time.
 */
object BackgroundUploadCompletion {
  private var handler: (() -> Unit)? = null

  fun setHandler(handler: () -> Unit) {
    this.handler = handler
  }

  fun complete() {
    val pending = handler ?: return
    handler = null
    // Apple requires the completion handler to run on the main queue.
    dispatch_async(dispatch_get_main_queue()) { pending() }
  }
}
