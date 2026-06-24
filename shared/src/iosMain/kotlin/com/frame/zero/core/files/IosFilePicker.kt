package com.frame.zero.core.files

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.Foundation.dataWithContentsOfURL
import platform.Foundation.writeToFile
import platform.UIKit.UIDocumentPickerDelegateProtocol
import platform.UIKit.UIDocumentPickerViewController
import platform.UniformTypeIdentifiers.UTType
import platform.UniformTypeIdentifiers.UTTypeItem
import platform.darwin.NSObject
import kotlin.coroutines.resume

/**
 * [FilePicker] backed by `UIDocumentPickerViewController` in copy mode (`asCopy = true`),
 * an out-of-process picker that needs no permission and no Info.plist usage string. The
 * system hands back a temporary in-sandbox copy, which we move into app storage so the
 * path stays valid for a later background upload.
 */
@OptIn(ExperimentalForeignApi::class)
class IosFilePicker : FilePicker {
  // Held strongly while a pick is in flight; UIDocumentPickerViewController.delegate is weak.
  private var delegate: PickerDelegate? = null

  override suspend fun pickFile(): PickedFile? =
    suspendCancellableCoroutine { continuation ->
      val root = topViewController()
      if (root == null) {
        if (continuation.isActive) continuation.resume(null)
        return@suspendCancellableCoroutine
      }
      val picker = UIDocumentPickerViewController(
        forOpeningContentTypes = listOf(UTTypeItem),
        asCopy = true
      )
      val pickerDelegate = PickerDelegate(continuation)
      delegate = pickerDelegate
      picker.delegate = pickerDelegate
      root.presentViewController(picker, animated = true, completion = null)
    }

  private inner class PickerDelegate(
    private val continuation: CancellableContinuation<PickedFile?>
  ) : NSObject(),
    UIDocumentPickerDelegateProtocol {
    override fun documentPicker(
      controller: UIDocumentPickerViewController,
      didPickDocumentsAtURLs: List<*>
    ) {
      delegate = null
      if (!continuation.isActive) return
      val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL
      continuation.resume(url?.let { copyToAppStorage(it) })
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
      delegate = null
      if (continuation.isActive) continuation.resume(null)
    }
  }

  private fun copyToAppStorage(url: NSURL): PickedFile? {
    val name = url.lastPathComponent ?: DEFAULT_NAME
    val data = NSData.dataWithContentsOfURL(url) ?: return null
    val outgoing = "${documentsDir()}/$OUTGOING_DIR/${NSUUID().UUIDString}"
    NSFileManager.defaultManager.createDirectoryAtPath(outgoing, true, null, null)
    val target = "$outgoing/$name"
    data.writeToFile(target, true)
    return PickedFile(
      name = name,
      sizeBytes = data.length.toLong(),
      contentType = mimeType(name),
      localPath = target
    )
  }

  private fun mimeType(name: String): String {
    val extension = name.substringAfterLast('.', "")
    if (extension.isEmpty()) return DEFAULT_CONTENT_TYPE
    return UTType.typeWithFilenameExtension(extension)?.preferredMIMEType ?: DEFAULT_CONTENT_TYPE
  }

  private companion object {
    const val OUTGOING_DIR = "outgoing"
    const val DEFAULT_NAME = "attachment"
    const val DEFAULT_CONTENT_TYPE = "application/octet-stream"
  }
}
