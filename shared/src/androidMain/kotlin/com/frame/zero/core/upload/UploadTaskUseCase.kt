package com.frame.zero.core.upload

import com.frame.zero.core.files.AttachmentFileManager
import com.frame.zero.core.logging.Logger
import com.frame.zero.core.network.NetworkConfig
import com.frame.zero.domain.ServerErrorException
import io.ktor.client.HttpClient
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.forms.ChannelProvider
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.util.cio.readChannel
import kotlinx.coroutines.CancellationException
import java.io.File

private const val TAG = "Upload"

/**
 * Performs one background upload: POSTs the multipart create, and on success removes both the
 * pending record and the local file. On failure, classifies and records the failure on
 * [store] instead of throwing — [TaskUploadWorker] decides whether to retry by re-reading the
 * record's status afterward, so this never leaves a failure unrecorded (see the upload-queue
 * gap in the error-handling audit: `PendingUploadStore.uploads` previously had no writer for a
 * permanent failure, only a WorkManager-internal attempt counter no UI could see).
 *
 * The file part is streamed straight off disk via [ChannelProvider] so the attachment (up to the
 * 50 MB cap) is never held whole in the worker's heap.
 */
class UploadTaskUseCase(
  private val store: PendingUploadStore,
  private val httpClient: HttpClient,
  private val networkConfig: NetworkConfig,
  private val attachmentFileManager: AttachmentFileManager,
  private val logger: Logger
) {
  suspend operator fun invoke(uploadId: String) {
    val upload = store.get(uploadId) ?: return
    val request = upload.toCreateRequest()
    val file = File(upload.localPath)
    val body = MultiPartFormDataContent(
      formData {
        append("productionId", request.productionId)
        append("title", request.title)
        request.description?.let { append("description", it) }
        request.dueDate?.let { append("dueDate", it.toString()) }
        request.assigneeUserId?.let { append("assigneeUserId", it) }
        append("priority", request.priority.name)
        request.participantUserIds.forEach { append("participantUserIds", it) }
        append(
          key = "file",
          value = ChannelProvider(size = file.length()) { file.readChannel() },
          headers = Headers.build {
            append(HttpHeaders.ContentType, upload.contentType)
            append(HttpHeaders.ContentDisposition, "filename=\"${upload.fileName}\"")
          }
        )
      }
    )
    try {
      httpClient.post("${networkConfig.baseUrl}/api/v1/tasks") {
        header("Idempotency-Key", upload.idempotencyKey)
        setBody(body)
      }
      attachmentFileManager.delete(upload.localPath)
      store.remove(uploadId)
    } catch (cancellation: CancellationException) {
      throw cancellation
    } catch (failure: Exception) {
      val status = failure.httpStatusOrNull()
      val reason = classifyUploadFailure(status)
      val updated = store.recordFailure(uploadId, reason)
      // Never log the response body (see the network-boundary leak this project already fixed) —
      // status and the derived reason are enough to act on.
      logger.w(
        tag = TAG,
        message = "Upload $uploadId failed [$status] reason=$reason " +
          "terminal=${updated?.status == PendingUploadStatus.Failed}",
        throwable = if (status == null) failure else null
      )
    }
  }

  private fun Throwable.httpStatusOrNull(): Int? =
    when (this) {
      is ServerErrorException -> status
      is ResponseException -> response.status.value
      else -> null
    }
}
