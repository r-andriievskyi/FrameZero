package com.frame.zero.core.upload

import com.frame.zero.core.files.AttachmentFileManager
import com.frame.zero.core.network.NetworkConfig
import com.frame.zero.domain.UseCase
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

/**
 * Performs one background upload: reads the picked file, POSTs the multipart create, and on
 * success removes both the pending record and the local file. Driven by the Android WorkManager
 * worker (the iOS path drives the request through `NSURLSession` instead).
 */
class UploadTaskUseCase(
  private val store: PendingUploadStore,
  private val httpClient: HttpClient,
  private val networkConfig: NetworkConfig,
  private val attachmentFileManager: AttachmentFileManager
) : UseCase<String, Unit>() {
  override suspend fun execute(params: String) {
    val upload = store.get(params) ?: return
    val bytes = attachmentFileManager.readBytes(upload.localPath)
    val boundary = multipartBoundary()
    val body = buildTaskMultipartBody(
      request = upload.toCreateRequest(),
      fileName = upload.fileName,
      contentType = upload.contentType,
      fileBytes = bytes,
      boundary = boundary
    )
    val response = httpClient.post("${networkConfig.baseUrl}/api/v1/tasks") {
      header("Idempotency-Key", upload.idempotencyKey)
      contentType(ContentType.parse(multipartContentType(boundary)))
      setBody(body)
    }
    check(response.status.isSuccess()) { "Task upload failed: ${response.status}" }
    attachmentFileManager.delete(upload.localPath)
    store.remove(params)
  }
}
