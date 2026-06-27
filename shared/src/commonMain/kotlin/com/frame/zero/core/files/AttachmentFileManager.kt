package com.frame.zero.core.files

import io.ktor.utils.io.ByteReadChannel

interface AttachmentFileManager {
  fun cachedAttachment(
    taskId: String,
    fileName: String
  ): String?

  /** Streams [channel] straight to disk in chunks so a large attachment never sits whole in the heap. */
  suspend fun saveDownloaded(
    taskId: String,
    fileName: String,
    channel: ByteReadChannel
  ): String

  fun readBytes(localPath: String): ByteArray

  fun delete(localPath: String)

  fun openWith(
    localPath: String,
    contentType: String
  )

  fun availableBytes(): Long
}
