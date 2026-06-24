package com.frame.zero.core.files

interface AttachmentFileManager {
  fun cachedAttachment(
    taskId: String,
    fileName: String
  ): String?

  suspend fun saveDownloaded(
    taskId: String,
    fileName: String,
    bytes: ByteArray
  ): String

  fun readBytes(localPath: String): ByteArray

  fun delete(localPath: String)

  fun openWith(
    localPath: String,
    contentType: String
  )

  fun availableBytes(): Long
}
