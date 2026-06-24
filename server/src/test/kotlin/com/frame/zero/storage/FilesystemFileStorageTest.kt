package com.frame.zero.storage

import com.frame.zero.AppError
import com.frame.zero.AppException
import kotlinx.coroutines.runBlocking
import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class FilesystemFileStorageTest {
  private val root = Files.createTempDirectory("framezero-storage-test").toFile()
  private val storage = FilesystemFileStorage(root.absolutePath)

  @AfterTest
  fun tearDown() {
    root.deleteRecursively()
  }

  @Test
  fun `store within the cap persists the bytes and reports the size`() =
    runBlocking {
      val bytes = byteArrayOf(1, 2, 3, 4, 5, 6)
      val blob = storage.store(bytes.inputStream(), maxBytes = 100)

      assertEquals(bytes.size.toLong(), blob.sizeBytes)
      storage.openStream(blob.storageKey).use {
        assertContentEquals(bytes, it.readBytes())
      }
    }

  @Test
  fun `store over the cap throws PayloadTooLarge and leaves no file behind`() =
    runBlocking {
      val bytes = ByteArray(50) { 1 }

      val error = assertFailsWith<AppException> {
        storage.store(bytes.inputStream(), maxBytes = 10)
      }
      assertEquals(AppError.PayloadTooLarge, error.error)
      assertTrue(root.listFiles().orEmpty().none { it.isFile }, "partial upload must be cleaned up")
    }

  @Test
  fun `delete removes the blob`() =
    runBlocking {
      val blob = storage.store(byteArrayOf(9).inputStream(), maxBytes = 100)
      storage.delete(blob.storageKey)
      assertTrue(!File(root, blob.storageKey).exists())
    }
}
