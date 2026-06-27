package com.frame.zero.core.upload

import com.frame.zero.core.files.AttachmentFileManager
import com.frame.zero.core.network.NetworkConfig
import com.frame.zero.domain.UseCase
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.ChannelProvider
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import io.ktor.util.cio.readChannel
import java.io.File

/**
 * Performs one background upload: POSTs the multipart create, and on success removes both the
 * pending record and the local file.
 *
 * The file part is streamed straight off disk via [ChannelProvider] so the attachment (up to the
 * 50 MB cap) is never held whole in the worker's heap.
 */
class UploadTaskUseCase(
  private val store: PendingUploadStore,
  private val httpClient: HttpClient,
  private val networkConfig: NetworkConfig,
  private val attachmentFileManager: AttachmentFileManager
) : UseCase<String, Unit>() {
  override suspend fun execute(params: String) {
    val upload = store.get(params) ?: return
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
    val response = httpClient.post("${networkConfig.baseUrl}/api/v1/tasks") {
      header("Idempotency-Key", upload.idempotencyKey)
      setBody(body)
    }
    check(response.status.isSuccess()) { "Task upload failed: ${response.status}" }
    attachmentFileManager.delete(upload.localPath)
    store.remove(params)
  }
}
