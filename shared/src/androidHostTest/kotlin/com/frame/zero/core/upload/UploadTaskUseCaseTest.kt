package com.frame.zero.core.upload

import com.frame.zero.core.files.AttachmentFileManager
import com.frame.zero.core.logging.Logger
import com.frame.zero.core.network.NetworkConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UploadTaskUseCaseTest {
  @Test
  fun `successful upload streams a multipart body and clears the record and local file`() =
    runTest {
      val file = File.createTempFile("upload", ".bin").apply {
        writeBytes(byteArrayOf(1, 2, 3, 4))
        deleteOnExit()
      }
      val requests = mutableListOf<HttpRequestData>()
      val store = PendingUploadStore(FakePendingUploadDao())
      store.add(upload("u1", file.absolutePath))
      val files = FakeAttachmentFileManager()
      val useCase = useCase(store, files, requests) { respond("", HttpStatusCode.Created) }

      useCase("u1")

      val request = requests.single()
      assertEquals("/api/v1/tasks", request.url.encodedPath)
      assertEquals("key-u1", request.headers["Idempotency-Key"])
      assertEquals(request.body.contentType?.match(ContentType.MultiPart.FormData), true)
      assertIs<MultiPartFormDataContent>(request.body)
      assertNull(store.get("u1"), "record cleared on success")
      assertEquals(listOf(file.absolutePath), files.deleted, "local file cleaned up")
    }

  @Test
  fun `5xx is classified Transient and keeps the record so it can be retried`() =
    runTest {
      val store = PendingUploadStore(FakePendingUploadDao())
      store.add(upload("u2"))
      val files = FakeAttachmentFileManager()
      val useCase = useCase(store, files) { respond("err", HttpStatusCode.InternalServerError) }

      useCase("u2")

      val record = store.get("u2")
      assertEquals(PendingUploadStatus.Uploading, record?.status, "5xx must not park the upload")
      assertEquals(UploadFailureReason.Transient, record?.failureReason)
      assertEquals(1, record?.attemptCount)
      assertTrue(files.deleted.isEmpty())
    }

  @Test
  fun `422 is classified Permanent and parks the upload on the first attempt`() =
    runTest {
      val store = PendingUploadStore(FakePendingUploadDao())
      store.add(upload("u3"))
      val useCase = useCase(store, FakeAttachmentFileManager()) {
        respond("err", HttpStatusCode.UnprocessableEntity)
      }

      useCase("u3")

      val record = store.get("u3")
      assertEquals(PendingUploadStatus.Failed, record?.status, "a client error can't be fixed by retrying")
      assertEquals(UploadFailureReason.Permanent, record?.failureReason)
    }

  @Test
  fun `429 is classified Transient despite being a 4xx`() =
    runTest {
      val store = PendingUploadStore(FakePendingUploadDao())
      store.add(upload("u4"))
      val useCase = useCase(store, FakeAttachmentFileManager()) {
        respond("err", HttpStatusCode.TooManyRequests)
      }

      useCase("u4")

      assertEquals(UploadFailureReason.Transient, store.get("u4")?.failureReason)
    }

  @Test
  fun `unknown upload id is a no-op`() =
    runTest {
      val store = PendingUploadStore(FakePendingUploadDao())
      val requests = mutableListOf<HttpRequestData>()
      val useCase = useCase(store, FakeAttachmentFileManager(), requests) { respond("", HttpStatusCode.Created) }

      useCase("missing")

      assertTrue(requests.isEmpty(), "must not call out for an upload that no longer exists")
    }

  private fun useCase(
    store: PendingUploadStore,
    files: AttachmentFileManager,
    requests: MutableList<HttpRequestData> = mutableListOf(),
    responder: MockRequestHandleScope.(HttpRequestData) -> HttpResponseData
  ): UploadTaskUseCase {
    val client = HttpClient(
      MockEngine { request ->
        requests += request
        responder(request)
      }
    ) {
      // Mirrors the production client (`clientConfig`): a non-2xx throws at the response
      // validator, which is what UploadTaskUseCase classifies and records.
      expectSuccess = true
      defaultRequest { contentType(ContentType.Application.Json) }
    }
    return UploadTaskUseCase(
      store,
      client,
      NetworkConfig(baseUrl = "http://test", isDebug = false),
      files,
      NoopLogger
    )
  }

  private fun upload(
    id: String,
    localPath: String = "/tmp/$id"
  ) = PendingTaskUpload(
    uploadId = id,
    productionId = "p1",
    title = "T",
    fileName = "f.bin",
    contentType = "application/octet-stream",
    localPath = localPath,
    idempotencyKey = "key-$id"
  )

  private class FakeAttachmentFileManager : AttachmentFileManager {
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
    }

    override fun openWith(
      localPath: String,
      contentType: String
    ) = Unit

    override fun availableBytes(): Long = Long.MAX_VALUE
  }

  private object NoopLogger : Logger {
    override fun v(
      tag: String,
      message: String,
      throwable: Throwable?
    ) = Unit

    override fun d(
      tag: String,
      message: String,
      throwable: Throwable?
    ) = Unit

    override fun i(
      tag: String,
      message: String,
      throwable: Throwable?
    ) = Unit

    override fun w(
      tag: String,
      message: String,
      throwable: Throwable?
    ) = Unit

    override fun e(
      tag: String,
      message: String,
      throwable: Throwable?
    ) = Unit
  }
}
