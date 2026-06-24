package com.frame.zero.core.files

/** Maximum attachment size accepted by the backend (must match the server's cap). */
const val MAX_ATTACHMENT_BYTES: Long = 50L * 1024 * 1024

data class PickedFile(
  val name: String,
  val sizeBytes: Long,
  val contentType: String,
  val localPath: String
)

/**
 * Opens the platform document picker (Android Storage Access Framework, iOS
 * `UIDocumentPickerViewController`). Neither platform needs a runtime permission.
 */
interface FilePicker {
  suspend fun pickFile(): PickedFile?
}
