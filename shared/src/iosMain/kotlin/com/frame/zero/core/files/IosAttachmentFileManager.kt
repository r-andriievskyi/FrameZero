package com.frame.zero.core.files

import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readRemaining
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.io.readByteArray
import platform.Foundation.NSFileHandle
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileSystemFreeSize
import platform.Foundation.NSHomeDirectory
import platform.Foundation.NSNumber
import platform.Foundation.NSURL
import platform.Foundation.closeFile
import platform.Foundation.fileHandleForWritingAtPath
import platform.Foundation.writeData
import platform.UIKit.UIDocumentInteractionController

/**
 * Stores downloaded attachments under the app sandbox's `Documents/attachments/<taskId>/`
 * and opens them with another app via `UIDocumentInteractionController` (the iOS "Open in"
 * menu). The sandbox is private to the app and protected at rest — no permission needed.
 */
@OptIn(ExperimentalForeignApi::class)
class IosAttachmentFileManager : AttachmentFileManager {
  private val fileManager = NSFileManager.defaultManager
  private val attachmentsRoot: String = "${documentsDir()}/$ATTACHMENTS_DIR"

  private var interactionController: UIDocumentInteractionController? = null

  override fun cachedAttachment(
    taskId: String,
    fileName: String
  ): String? {
    val path = "$attachmentsRoot/$taskId/$fileName"
    return if (fileManager.fileExistsAtPath(path)) path else null
  }

  override suspend fun saveDownloaded(
    taskId: String,
    fileName: String,
    channel: ByteReadChannel
  ): String {
    val dir = "$attachmentsRoot/$taskId"
    fileManager.createDirectoryAtPath(dir, true, null, null)
    val path = "$dir/$fileName"
    fileManager.createFileAtPath(path, null, null)
    val handle = NSFileHandle.fileHandleForWritingAtPath(path)
      ?: error("Unable to open $path for writing")
    try {
      while (!channel.isClosedForRead) {
        val packet = channel.readRemaining(DOWNLOAD_CHUNK_BYTES)
        while (!packet.exhausted()) {
          val bytes = packet.readByteArray()
          if (bytes.isNotEmpty()) handle.writeData(bytes.toNSData())
        }
      }
    } finally {
      handle.closeFile()
    }
    return path
  }

  override fun delete(localPath: String) {
    fileManager.removeItemAtPath(localPath, null)
  }

  override fun openWith(
    localPath: String,
    contentType: String
  ) {
    val controller = UIDocumentInteractionController.interactionControllerWithURL(
      NSURL.fileURLWithPath(localPath)
    )
    interactionController = controller
    val view = topViewController()?.view ?: return
    controller.presentOpenInMenuFromRect(view.bounds, view, true)
  }

  override fun availableBytes(): Long {
    val attributes = fileManager.attributesOfFileSystemForPath(NSHomeDirectory(), null)
    val freeSize = attributes?.get(NSFileSystemFreeSize) as? NSNumber
    return freeSize?.longLongValue ?: Long.MAX_VALUE
  }

  private companion object {
    const val ATTACHMENTS_DIR = "attachments"
    const val DOWNLOAD_CHUNK_BYTES = 64L * 1024
  }
}
