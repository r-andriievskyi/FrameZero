package com.frame.zero.core.files

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSFileSystemFreeSize
import platform.Foundation.NSHomeDirectory
import platform.Foundation.NSNumber
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfFile
import platform.Foundation.writeToFile
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
    bytes: ByteArray
  ): String {
    val dir = "$attachmentsRoot/$taskId"
    fileManager.createDirectoryAtPath(dir, true, null, null)
    val path = "$dir/$fileName"
    bytes.toNSData().writeToFile(path, true)
    return path
  }

  override fun readBytes(localPath: String): ByteArray =
    (NSData.dataWithContentsOfFile(localPath) ?: NSData()).toByteArray()

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
  }
}
