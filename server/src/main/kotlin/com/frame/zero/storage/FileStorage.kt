package com.frame.zero.storage

import com.frame.zero.AppError
import com.frame.zero.AppException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.util.UUID

data class StoredBlob(
  val storageKey: String,
  val sizeBytes: Long
)

/**
 * Persists attachment bytes outside the database. The DB stores only the returned
 * [StoredBlob.storageKey]; the bytes live wherever the implementation puts them.
 */
interface FileStorage {
  suspend fun store(
    input: InputStream,
    maxBytes: Long
  ): StoredBlob

  fun openStream(key: String): InputStream

  /** Whether a blob is actually present, so callers can fail before committing a response. */
  fun exists(key: String): Boolean

  fun delete(key: String)
}

class FilesystemFileStorage(
  rootDir: String
) : FileStorage {
  private val root = File(rootDir).apply { mkdirs() }

  override suspend fun store(
    input: InputStream,
    maxBytes: Long
  ): StoredBlob =
    withContext(Dispatchers.IO) {
      val key = UUID.randomUUID().toString()
      val target = File(root, key)
      var total = 0L
      var committed = false
      try {
        target.outputStream().use { out ->
          val buffer = ByteArray(BUFFER_SIZE)
          while (true) {
            val read = input.read(buffer)
            if (read == -1) break
            total += read
            if (total > maxBytes) throw AppException(AppError.PayloadTooLarge)
            out.write(buffer, 0, read)
          }
        }
        committed = true
      } finally {
        input.close()
        // any failure (oversize, I/O) must not leave a partial blob behind.
        if (!committed) target.delete()
      }
      StoredBlob(storageKey = key, sizeBytes = total)
    }

  override fun openStream(key: String): InputStream = File(root, key).inputStream()

  override fun exists(key: String): Boolean = File(root, key).isFile

  override fun delete(key: String) {
    File(root, key).delete()
  }

  private companion object {
    const val BUFFER_SIZE = 8 * 1024
  }
}
