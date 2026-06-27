package com.frame.zero.core.upload

import com.frame.zero.dto.task.CreateTaskRequest
import kotlin.random.Random

fun multipartBoundary(): String = "FrameZeroBoundary${Random.nextLong().toULong().toString(16)}"

fun multipartContentType(boundary: String): String = "multipart/form-data; boundary=$boundary"

/**
 * The part of the `multipart/form-data` body that precedes the raw file bytes: the text fields
 * followed by the file part's headers. Split out from the closing boundary ([taskMultipartSuffix])
 * so the iOS `NSURLSession` uploader can write the body file incrementally — prefix, then the
 * source file streamed in chunks, then suffix — without ever holding the attachment in memory.
 */
fun taskMultipartPrefix(
  request: CreateTaskRequest,
  fileName: String,
  contentType: String,
  boundary: String
): ByteArray {
  val builder = StringBuilder()

  fun textField(
    name: String,
    value: String
  ) {
    builder
      .append("--$boundary\r\n")
      .append("Content-Disposition: form-data; name=\"$name\"\r\n\r\n")
      .append("$value\r\n")
  }

  textField("productionId", request.productionId)
  textField("title", request.title)
  request.description?.let { textField("description", it) }
  request.dueDate?.let { textField("dueDate", it.toString()) }
  request.assigneeUserId?.let { textField("assigneeUserId", it) }
  textField("priority", request.priority.name)

  builder
    .append("--$boundary\r\n")
    .append("Content-Disposition: form-data; name=\"file\"; filename=\"$fileName\"\r\n")
    .append("Content-Type: $contentType\r\n\r\n")

  return builder.toString().encodeToByteArray()
}

/** The closing boundary that follows the raw file bytes (see [taskMultipartPrefix]). */
fun taskMultipartSuffix(boundary: String): ByteArray = "\r\n--$boundary--\r\n".encodeToByteArray()

/**
 * Builds the raw `multipart/form-data` body for a task-create request with one file part, as a
 * single in-memory `ByteArray`. The Android path streams the file part via Ktor's
 * `MultiPartFormDataContent` (see `UploadTaskUseCase`) and iOS streams the body file from
 * [taskMultipartPrefix]/[taskMultipartSuffix], so this whole-body form is kept mainly as the
 * canonical, unit-tested definition of the wire framing both platforms share.
 */
fun buildTaskMultipartBody(
  request: CreateTaskRequest,
  fileName: String,
  contentType: String,
  fileBytes: ByteArray,
  boundary: String
): ByteArray {
  val prefix = taskMultipartPrefix(request, fileName, contentType, boundary)
  val suffix = taskMultipartSuffix(boundary)
  val out = ByteArray(prefix.size + fileBytes.size + suffix.size)
  prefix.copyInto(out, 0)
  fileBytes.copyInto(out, prefix.size)
  suffix.copyInto(out, prefix.size + fileBytes.size)
  return out
}
