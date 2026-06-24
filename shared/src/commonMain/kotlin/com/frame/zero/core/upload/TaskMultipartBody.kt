package com.frame.zero.core.upload

import com.frame.zero.dto.task.CreateTaskRequest
import kotlin.random.Random

fun multipartBoundary(): String = "FrameZeroBoundary${Random.nextLong().toULong().toString(16)}"

fun multipartContentType(boundary: String): String = "multipart/form-data; boundary=$boundary"

/**
 * Builds the raw `multipart/form-data` body for a task-create request with one file part.
 * Shared by the Ktor client (Android path) and the iOS background `NSURLSession` uploader so
 * both produce an identical wire body. Kept as a single in-memory `ByteArray` — fine within the
 * 50 MB attachment cap.
 */
fun buildTaskMultipartBody(
  request: CreateTaskRequest,
  fileName: String,
  contentType: String,
  fileBytes: ByteArray,
  boundary: String
): ByteArray {
  val segments = mutableListOf<ByteArray>()

  fun textField(
    name: String,
    value: String
  ) {
    segments += (
      "--$boundary\r\n" +
        "Content-Disposition: form-data; name=\"$name\"\r\n\r\n" +
        "$value\r\n"
    ).encodeToByteArray()
  }

  textField("productionId", request.productionId)
  textField("title", request.title)
  request.description?.let { textField("description", it) }
  request.dueDate?.let { textField("dueDate", it.toString()) }
  request.assigneeUserId?.let { textField("assigneeUserId", it) }
  textField("priority", request.priority.name)

  segments += (
    "--$boundary\r\n" +
      "Content-Disposition: form-data; name=\"file\"; filename=\"$fileName\"\r\n" +
      "Content-Type: $contentType\r\n\r\n"
  ).encodeToByteArray()
  segments += fileBytes
  segments += "\r\n--$boundary--\r\n".encodeToByteArray()

  val out = ByteArray(segments.sumOf { it.size })
  var offset = 0
  for (segment in segments) {
    segment.copyInto(out, offset)
    offset += segment.size
  }
  return out
}
