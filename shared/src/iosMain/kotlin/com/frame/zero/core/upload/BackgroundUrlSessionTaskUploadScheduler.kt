package com.frame.zero.core.upload

import com.frame.zero.core.files.toByteArray
import com.frame.zero.core.files.toNSData
import com.frame.zero.core.network.NetworkConfig
import com.frame.zero.core.session.TokenStorage
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import platform.Foundation.NSData
import platform.Foundation.NSHTTPURLResponse
import platform.Foundation.NSMutableURLRequest
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSURLSession
import platform.Foundation.NSURLSessionConfiguration
import platform.Foundation.NSURLSessionTask
import platform.Foundation.NSURLSessionTaskDelegateProtocol
import platform.Foundation.dataWithContentsOfFile
import platform.Foundation.setHTTPMethod
import platform.Foundation.setValue
import platform.Foundation.writeToFile
import platform.darwin.NSObject

/**
 * [TaskUploadScheduler] backed by a background `NSURLSession`: the OS carries the upload even if
 * the app is suspended or killed, relaunching it to deliver completion. The multipart body is
 * built once (shared with the Android path), written to a temp file, and handed to the session.
 */
@OptIn(ExperimentalForeignApi::class)
class BackgroundUrlSessionTaskUploadScheduler(
  private val store: PendingUploadStore,
  private val tokenStorage: TokenStorage,
  private val networkConfig: NetworkConfig
) : TaskUploadScheduler {
  private val session: NSURLSession by lazy {
    val configuration = NSURLSessionConfiguration.backgroundSessionConfigurationWithIdentifier(SESSION_ID)
    NSURLSession.sessionWithConfiguration(configuration, UploadDelegate(store), delegateQueue = null)
  }

  override suspend fun enqueue(upload: PendingTaskUpload) {
    store.add(upload)
    start(upload)
  }

  override suspend fun retry(uploadId: String) {
    store.markUploading(uploadId)
    store.get(uploadId)?.let(::start)
  }

  override suspend fun cancel(uploadId: String) {
    store.remove(uploadId)
  }

  /**
   * Recreates the background session (touching the lazy instance) so a relaunched app
   * re-attaches the delegate and the OS re-delivers any completed-while-dead events.
   */
  fun ensureSessionActive() {
    session.getTasksWithCompletionHandler { _, _, _ -> }
  }

  private fun start(upload: PendingTaskUpload) {
    val fileBytes = (NSData.dataWithContentsOfFile(upload.localPath) ?: return).toByteArray()
    val boundary = multipartBoundary()
    val bodyPath = "${NSTemporaryDirectory()}${upload.uploadId}.multipart"
    buildTaskMultipartBody(upload.toCreateRequest(), upload.fileName, upload.contentType, fileBytes, boundary)
      .toNSData()
      .writeToFile(bodyPath, atomically = true)

    val url = NSURL(string = "${networkConfig.baseUrl}/api/v1/tasks")
    val request = NSMutableURLRequest(uRL = url)
    request.setHTTPMethod("POST")
    request.setValue(multipartContentType(boundary), forHTTPHeaderField = "Content-Type")
    request.setValue(upload.idempotencyKey, forHTTPHeaderField = "Idempotency-Key")
    tokenStorage.getAccessToken()?.let {
      request.setValue("Bearer $it", forHTTPHeaderField = "Authorization")
    }

    val task = session.uploadTaskWithRequest(request, fromFile = NSURL.fileURLWithPath(bodyPath))
    task.taskDescription = upload.uploadId
    task.resume()
  }

  private companion object {
    const val SESSION_ID = "com.frame.zero.task-upload"
  }
}

@OptIn(ExperimentalForeignApi::class)
private class UploadDelegate(
  private val store: PendingUploadStore
) : NSObject(),
  NSURLSessionTaskDelegateProtocol {
  // Delegate callbacks aren't suspend; persist the outcome on a background scope.
  private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

  override fun URLSession(
    session: NSURLSession,
    task: NSURLSessionTask,
    didCompleteWithError: platform.Foundation.NSError?
  ) {
    val uploadId = task.taskDescription ?: return
    val status = (task.response as? NSHTTPURLResponse)?.statusCode ?: -1L
    val succeeded = didCompleteWithError == null && status in 200..299
    scope.launch {
      if (succeeded) store.remove(uploadId) else store.markFailed(uploadId)
    }
  }

  override fun URLSessionDidFinishEventsForBackgroundURLSession(session: NSURLSession) {
    // All queued completions for this background session have been delivered; let the OS
    // know we're done so it can stop our background time (handler set by the AppDelegate).
    BackgroundUploadCompletion.complete()
  }
}
