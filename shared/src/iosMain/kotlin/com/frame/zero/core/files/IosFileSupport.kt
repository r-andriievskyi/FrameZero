package com.frame.zero.core.files

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class)
internal fun NSData.toByteArray(): ByteArray {
  val size = length.toInt()
  if (size == 0) return ByteArray(0)
  return ByteArray(size).apply {
    usePinned { pinned -> memcpy(pinned.addressOf(0), this@toByteArray.bytes, this@toByteArray.length) }
  }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal fun ByteArray.toNSData(): NSData {
  if (isEmpty()) return NSData()
  return usePinned { pinned ->
    NSData.create(bytes = pinned.addressOf(0), length = size.toULong())
  }
}

internal fun documentsDir(): String =
  NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true).first() as String

// The top-most presented view controller, used to host the document picker / "open in"
// menu. keyWindow is deprecated but adequate for this single-scene app.
@Suppress("DEPRECATION")
internal fun topViewController(): UIViewController? {
  var top = UIApplication.sharedApplication.keyWindow?.rootViewController
  while (top?.presentedViewController != null) {
    top = top.presentedViewController
  }
  return top
}
