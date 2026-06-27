package com.frame.zero.core.files

import android.content.Context
import android.content.Intent
import android.os.StatFs
import androidx.core.content.FileProvider
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readRemaining
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.readByteArray
import java.io.File

/**
 * Stores downloaded attachments under app-private `filesDir/attachments/<taskId>/` and
 * opens them with another app via a `FileProvider` content URI (no storage permission).
 */
class AndroidAttachmentFileManager(
  private val context: Context
) : AttachmentFileManager {
  private val attachmentsRoot: File
    get() = File(context.filesDir, ATTACHMENTS_DIR)

  override fun cachedAttachment(
    taskId: String,
    fileName: String
  ): String? {
    val file = File(File(attachmentsRoot, taskId), fileName)
    return if (file.isFile) file.absolutePath else null
  }

  override suspend fun saveDownloaded(
    taskId: String,
    fileName: String,
    channel: ByteReadChannel
  ): String =
    withContext(Dispatchers.IO) {
      val dir = File(attachmentsRoot, taskId).apply { mkdirs() }
      val target = File(dir, fileName)
      target.outputStream().use { out ->
        while (!channel.isClosedForRead) {
          val packet = channel.readRemaining(DOWNLOAD_CHUNK_BYTES)
          while (!packet.exhausted()) {
            out.write(packet.readByteArray())
          }
        }
      }
      target.absolutePath
    }

  override fun delete(localPath: String) {
    File(localPath).delete()
  }

  override fun openWith(
    localPath: String,
    contentType: String
  ) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", File(localPath))
    val view = Intent(Intent.ACTION_VIEW).apply {
      setDataAndType(uri, contentType)
      addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    val chooser = Intent.createChooser(view, null).apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(chooser)
  }

  override fun availableBytes(): Long = StatFs(context.filesDir.absolutePath).availableBytes

  private companion object {
    const val ATTACHMENTS_DIR = "attachments"
    const val DOWNLOAD_CHUNK_BYTES = 64L * 1024
  }
}
