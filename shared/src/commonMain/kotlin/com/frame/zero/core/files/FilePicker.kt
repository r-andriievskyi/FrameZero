package com.frame.zero.core.files

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
