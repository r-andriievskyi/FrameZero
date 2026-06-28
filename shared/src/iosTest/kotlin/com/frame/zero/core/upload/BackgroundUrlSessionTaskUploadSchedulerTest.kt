package com.frame.zero.core.upload

import com.frame.zero.core.files.toByteArray
import com.frame.zero.core.files.toNSData
import com.frame.zero.core.network.NetworkConfig
import com.frame.zero.core.session.TokenStorage
import com.russhwolf.settings.MapSettings
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUUID
import platform.Foundation.dataWithContentsOfFile
import platform.Foundation.writeToFile
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Verifies the background-upload scheduler's
 * constant-memory multipart streaming ([BackgroundUrlSessionTaskUploadScheduler.writeMultipartBodyFile])
 * — prefix, then the source file copied in chunks, then the closing boundary — produces a body
 * file byte-identical to the shared, commonTest-verified [buildTaskMultipartBody]. Touches only
 * NSFileManager/NSFileHandle, never the background `NSURLSession`.
 */
@OptIn(ExperimentalForeignApi::class)
class BackgroundUrlSessionTaskUploadSchedulerTest {
  private val boundary = "FrameZeroBoundaryTEST"
  private val createdPaths = mutableListOf<String>()

  @AfterTest
  fun cleanUp() = createdPaths.forEach { NSFileManager.defaultManager.removeItemAtPath(it, null) }

  @Test
  fun `streamed body file matches the canonical multipart body`() {
    val fileBytes = ByteArray(1_000) { (it % 256).toByte() }
    val sourcePath = writeTempFile(fileBytes)
    val upload = upload(localPath = sourcePath)
    val bodyPath = tempPath("body")

    val wrote = scheduler().writeMultipartBodyFile(upload, boundary, bodyPath)

    assertTrue(wrote)
    val expected = buildTaskMultipartBody(
      request = upload.toCreateRequest(),
      fileName = upload.fileName,
      contentType = upload.contentType,
      fileBytes = fileBytes,
      boundary = boundary
    )
    assertContentEquals(expected, readFile(bodyPath))
  }

  @Test
  fun `a file larger than the chunk size is streamed without corruption`() {
    // 200 KB spans several 64 KB read chunks, exercising the copy loop and its boundary.
    val fileBytes = ByteArray(200_000) { ((it * 31) % 256).toByte() }
    val sourcePath = writeTempFile(fileBytes)
    val upload = upload(localPath = sourcePath)
    val bodyPath = tempPath("body-large")

    assertTrue(scheduler().writeMultipartBodyFile(upload, boundary, bodyPath))

    val expected = buildTaskMultipartBody(
      request = upload.toCreateRequest(),
      fileName = upload.fileName,
      contentType = upload.contentType,
      fileBytes = fileBytes,
      boundary = boundary
    )
    val actual = readFile(bodyPath)
    assertEquals(expected.size, actual.size)
    assertContentEquals(expected, actual)
  }

  @Test
  fun `a missing source file yields no body and leaves the record pending`() {
    val upload = upload(localPath = "${NSTemporaryDirectory()}does-not-exist.bin")
    val bodyPath = tempPath("body-missing")

    val wrote = scheduler().writeMultipartBodyFile(upload, boundary, bodyPath)

    assertFalse(wrote)
    // Nothing readable was produced (the empty placeholder file is opened then abandoned).
    assertNull(NSData.dataWithContentsOfFile(bodyPath)?.let { if (it.length == 0UL) null else it })
  }

  private fun scheduler(): BackgroundUrlSessionTaskUploadScheduler =
    BackgroundUrlSessionTaskUploadScheduler(
      store = PendingUploadStore(FakePendingUploadDao()),
      tokenStorage = TokenStorage(MapSettings()),
      networkConfig = NetworkConfig(baseUrl = "https://test.local", isDebug = false)
    )

  private fun upload(localPath: String) =
    PendingTaskUpload(
      uploadId = "u1",
      productionId = "p1",
      title = "Lock the schedule",
      description = "with detail",
      fileName = "callsheet.pdf",
      contentType = "application/pdf",
      localPath = localPath,
      idempotencyKey = "idem-u1"
    )

  private fun writeTempFile(bytes: ByteArray): String {
    val path = tempPath("source")
    bytes.toNSData().writeToFile(path, atomically = true)
    return path
  }

  private fun tempPath(prefix: String): String =
    "${NSTemporaryDirectory()}$prefix-${NSUUID().UUIDString}".also { createdPaths += it }

  private fun readFile(path: String): ByteArray =
    requireNotNull(NSData.dataWithContentsOfFile(path)) { "no file at $path" }.toByteArray()
}
