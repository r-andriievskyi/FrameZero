package com.frame.zero.core.upload

import com.frame.zero.core.files.toNSData
import com.frame.zero.core.logging.Logger
import com.frame.zero.core.network.NetworkConfig
import com.frame.zero.core.session.TokenStorage
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import platform.Foundation.NSFileHandle
import platform.Foundation.NSFileManager
import platform.Foundation.NSHTTPURLResponse
import platform.Foundation.NSMutableURLRequest
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSURLSession
import platform.Foundation.NSURLSessionConfiguration
import platform.Foundation.NSURLSessionTask
import platform.Foundation.NSURLSessionTaskDelegateProtocol
import platform.Foundation.closeFile
import platform.Foundation.fileHandleForReadingAtPath
import platform.Foundation.fileHandleForWritingAtPath
import platform.Foundation.readDataOfLength
import platform.Foundation.setHTTPMethod
import platform.Foundation.setValue
import platform.Foundation.writeData
import platform.darwin.NSObject
import kotlin.time.Duration.Companion.milliseconds

/**
 * [TaskUploadScheduler] backed by a background `NSURLSession`: the OS carries the upload even if
 * the app is suspended or killed, relaunching it to deliver completion. The multipart body is
 * streamed to a temp file (prefix, then the source file in chunks, then the closing boundary) so
 * the attachment is never held whole in memory, and the session uploads from that file.
 */
@OptIn(ExperimentalForeignApi::class)
class BackgroundUrlSessionTaskUploadScheduler(
  private val store: PendingUploadStore,
  private val tokenStorage: TokenStorage,
  private val networkConfig: NetworkConfig,
  private val logger: Logger
) : TaskUploadScheduler {
  private val session: NSURLSession by lazy {
    val configuration = NSURLSessionConfiguration.backgroundSessionConfigurationWithIdentifier(SESSION_ID)
    configuration.waitsForConnectivity = true
    NSURLSession.sessionWithConfiguration(configuration, UploadDelegate(store, logger, ::start), delegateQueue = null)
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
    val boundary = multipartBoundary()
    val bodyPath = "${NSTemporaryDirectory()}${upload.uploadId}.multipart"
    if (!writeMultipartBodyFile(upload, boundary, bodyPath)) return

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

  /**
   * Writes the multipart body to [bodyPath] in constant memory: the prefix, then the source file
   * copied in [UPLOAD_CHUNK_BYTES] chunks, then the closing boundary. Returns false if the source
   * file or temp body file can't be opened (the record stays pending for retry).
   */
  internal fun writeMultipartBodyFile(
    upload: PendingTaskUpload,
    boundary: String,
    bodyPath: String
  ): Boolean {
    val fileManager = NSFileManager.defaultManager
    fileManager.createFileAtPath(bodyPath, null, null)
    val out = NSFileHandle.fileHandleForWritingAtPath(bodyPath) ?: return false
    val reader = NSFileHandle.fileHandleForReadingAtPath(upload.localPath)
    if (reader == null) {
      out.closeFile()
      return false
    }
    try {
      out.writeData(
        taskMultipartPrefix(upload.toCreateRequest(), upload.fileName, upload.contentType, boundary).toNSData()
      )
      while (true) {
        val chunk = reader.readDataOfLength(UPLOAD_CHUNK_BYTES)
        if (chunk.length == 0UL) break
        out.writeData(chunk)
      }
      out.writeData(taskMultipartSuffix(boundary).toNSData())
    } finally {
      reader.closeFile()
      out.closeFile()
    }
    return true
  }

  private companion object {
    const val SESSION_ID = "com.frame.zero.task-upload"
    const val UPLOAD_CHUNK_BYTES: ULong = 65_536UL
  }
}

@OptIn(ExperimentalForeignApi::class)
private class UploadDelegate(
  private val store: PendingUploadStore,
  private val logger: Logger,
  private val retry: (PendingTaskUpload) -> Unit
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
    val status = (task.response as? NSHTTPURLResponse)?.statusCode?.toInt()
    val succeeded = didCompleteWithError == null && status != null && status in 200..299
    scope.launch {
      if (succeeded) {
        store.remove(uploadId)
        return@launch
      }
      val reason = classifyUploadFailure(status)
      val updated = store.recordFailure(uploadId, reason)
      logger.w(
        tag = "Upload",
        message = "Upload $uploadId failed [$status] reason=$reason " +
          "terminal=${updated?.status == PendingUploadStatus.Failed}",
        throwable = if (status == null) didCompleteWithError?.let { Exception(it.localizedDescription) } else null
      )
      if (updated != null && updated.status == PendingUploadStatus.Uploading) {
        delay(retryBackoffMillis(updated.attemptCount).milliseconds)
        retry(updated)
      }
    }
  }

  override fun URLSessionDidFinishEventsForBackgroundURLSession(session: NSURLSession) {
    BackgroundUploadCompletion.complete()
  }

  /** 2s / 4s / 8s, matching Android's WorkManager backoff — [attemptCount] is already the
   *  count *after* this failure, so attempt 1 waits 2s, attempt 2 waits 4s, etc. */
  private fun retryBackoffMillis(attemptCount: Int): Long = 2_000L shl (attemptCount - 1).coerceIn(0, 2)
}
