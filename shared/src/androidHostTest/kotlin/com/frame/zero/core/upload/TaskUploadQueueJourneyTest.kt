package com.frame.zero.core.upload

import com.frame.zero.core.files.AttachmentFileManager
import com.frame.zero.core.network.NetworkConfig
import com.frame.zero.domain.Outcome
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * End-to-end offline-first **write** journey for a queued task-create-with-attachment, tying
 * together the three pieces that are otherwise only unit-tested in isolation: the persistent
 * [PendingUploadStore] (survives process death), the [UploadTaskUseCase] the background worker
 * runs, and the server response. Walks the realistic path: enqueue while offline → the record and
 * file survive a transient server failure → a later attempt succeeds and drains both.
 */
class TaskUploadQueueJourneyTest {
  @Test
  fun `a queued upload survives a transient failure and drains on a later success`() =
    runTest {
      val file = tempAttachment()
      val store = PendingUploadStore(FakePendingUploadDao())
      val files = RecordingAttachmentFileManager()

      // 1. Created while offline: the record is persisted and the local file is kept.
      store.add(upload("u1", file.absolutePath))
      assertEquals(PendingUploadStatus.Uploading, store.get("u1")?.status)
      assertTrue(file.exists())

      // 2. A fresh process picks the record up (nothing in memory carried it) and the first
      //    attempt hits a server error — the record and file must remain for a retry.
      val failing = uploadUseCase(store, files) { respond("boom", HttpStatusCode.InternalServerError) }
      assertIs<Outcome.Failure>(failing("u1"))
      assertEquals("u1", store.get("u1")?.uploadId, "record retained after a failed attempt")
      assertTrue(file.exists(), "local file retained after a failed attempt")
      assertTrue(files.deleted.isEmpty())

      // 3. The worker's terminal-failure bookkeeping marks it Failed; it is still queued.
      store.markFailed("u1")
      assertEquals(PendingUploadStatus.Failed, store.get("u1")?.status)

      // 4. A later attempt (retry / reconnect) succeeds: the record is cleared and the file removed.
      val requests = mutableListOf<String>()
      val succeeding = uploadUseCase(store, files, requests) { respond("", HttpStatusCode.Created) }
      assertIs<Outcome.Success<Unit>>(succeeding("u1"))

      assertNull(store.get("u1"), "record drained on success")
      assertFalse(file.exists(), "local file cleaned up on success")
      assertEquals(listOf("/api/v1/tasks"), requests, "exactly one create POST on the successful attempt")
      assertEquals(listOf(file.absolutePath), files.deleted)
    }

  private fun tempAttachment(): File =
    File.createTempFile("attachment", ".bin").apply {
      writeBytes(ByteArray(2_048) { (it % 256).toByte() })
      deleteOnExit()
    }

  private fun uploadUseCase(
    store: PendingUploadStore,
    files: AttachmentFileManager,
    requests: MutableList<String> = mutableListOf(),
    responder: MockRequestHandleScope.(HttpRequestData) -> io.ktor.client.request.HttpResponseData
  ): UploadTaskUseCase {
    val client = HttpClient(
      MockEngine { request ->
        requests += request.url.encodedPath
        responder(request)
      }
    ) {
      defaultRequest { contentType(ContentType.Application.Json) }
    }
    return UploadTaskUseCase(store, client, NetworkConfig(baseUrl = "http://test", isDebug = false), files)
  }

  private fun upload(
    id: String,
    localPath: String
  ) = PendingTaskUpload(
    uploadId = id,
    productionId = "p1",
    title = "Lock the schedule",
    fileName = "callsheet.pdf",
    contentType = "application/pdf",
    localPath = localPath,
    idempotencyKey = "key-$id"
  )

  private class RecordingAttachmentFileManager : AttachmentFileManager {
    val deleted: MutableList<String> = mutableListOf()

    override fun cachedAttachment(
      taskId: String,
      fileName: String
    ): String? = null

    override suspend fun saveDownloaded(
      taskId: String,
      fileName: String,
      channel: ByteReadChannel
    ): String = ""

    override fun delete(localPath: String) {
      deleted += localPath
      File(localPath).delete()
    }

    override fun openWith(
      localPath: String,
      contentType: String
    ) = Unit

    override fun availableBytes(): Long = Long.MAX_VALUE
  }
}
