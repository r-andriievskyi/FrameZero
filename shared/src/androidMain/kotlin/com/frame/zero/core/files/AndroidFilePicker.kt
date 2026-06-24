package com.frame.zero.core.files

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/**
 * [FilePicker] backed by the Storage Access Framework (`ACTION_OPEN_DOCUMENT`), which
 * needs no runtime permission.
 */
class AndroidFilePicker(
  private val context: Context
) : FilePicker {
  private var launcher: ActivityResultLauncher<Array<String>>? = null
  private var pending: CompletableDeferred<Uri?>? = null

  fun attach(activity: FragmentActivity) {
    launcher = activity.registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
      pending?.complete(uri)
      pending = null
    }
  }

  override suspend fun pickFile(): PickedFile? {
    val launcher = launcher ?: return null
    val deferred = CompletableDeferred<Uri?>()
    pending = deferred
    launcher.launch(arrayOf("*/*"))
    val uri = deferred.await() ?: return null
    return withContext(Dispatchers.IO) { copyToAppStorage(uri) }
  }

  private fun copyToAppStorage(uri: Uri): PickedFile? {
    val resolver = context.contentResolver
    val (name, size) = queryNameAndSize(uri)
    val contentType = resolver.getType(uri) ?: DEFAULT_CONTENT_TYPE
    val dir = File(context.filesDir, "$OUTGOING_DIR/${UUID.randomUUID()}").apply { mkdirs() }
    val target = File(dir, name)
    resolver.openInputStream(uri)?.use { input ->
      target.outputStream().use { input.copyTo(it) }
    } ?: return null
    return PickedFile(
      name = name,
      sizeBytes = if (size >= 0) size else target.length(),
      contentType = contentType,
      localPath = target.absolutePath
    )
  }

  private fun queryNameAndSize(uri: Uri): Pair<String, Long> {
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
      if (cursor.moveToFirst()) {
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
        val name = if (nameIndex >= 0) cursor.getString(nameIndex) else null
        val size = if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) cursor.getLong(sizeIndex) else -1L
        return (name ?: DEFAULT_NAME) to size
      }
    }
    return DEFAULT_NAME to -1L
  }

  private companion object {
    const val OUTGOING_DIR = "outgoing"
    const val DEFAULT_NAME = "attachment"
    const val DEFAULT_CONTENT_TYPE = "application/octet-stream"
  }
}
