package com.frame.zero.feature.task.details.data

import com.frame.zero.core.files.AttachmentFileManager
import com.frame.zero.core.network.NetworkConfig
import com.frame.zero.core.network.connectivity.ConnectivityObserver
import com.frame.zero.domain.DomainError
import com.frame.zero.domain.Outcome
import com.frame.zero.dto.task.CreateTaskRequest
import com.frame.zero.dto.task.TaskPriority
import com.frame.zero.dto.task.TaskStatus
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.HttpRequestData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.content.TextContent
import io.ktor.http.contentType
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class TasksRepositoryImplTest {
  @Test
  fun `getTask issues a GET to the task endpoint and deserializes the detail`() =
    runTest {
      val requests = mutableListOf<HttpRequestData>()
      val repo = repository(requests) { taskDetailJson(id = "t1") }

      val task = repo.getTask("t1")

      val request = requests.single()
      assertEquals(HttpMethod.Get, request.method)
      assertEquals("/api/v1/tasks/t1", request.url.encodedPath)
      assertEquals("t1", task.id)
    }

  @Test
  fun `completeTask PATCHes the task with a DONE status body`() =
    runTest {
      val requests = mutableListOf<HttpRequestData>()
      val repo = repository(requests) { taskDetailJson(id = "t1", status = "DONE") }

      val task = repo.completeTask("t1")

      val request = requests.single()
      assertEquals(HttpMethod.Patch, request.method)
      assertEquals("/api/v1/tasks/t1", request.url.encodedPath)
      assertTrue((request.body as TextContent).text.contains("\"status\":\"DONE\""))
      assertEquals(TaskStatus.DONE, task.status)
    }

  @Test
  fun `createTask POSTs the request body`() =
    runTest {
      val requests = mutableListOf<HttpRequestData>()
      val repo = repository(requests) { taskDetailJson(id = "t9") }

      val created = repo.createTask(
        CreateTaskRequest(productionId = "p1", title = "Storyboard", priority = TaskPriority.HIGH)
      )

      val request = requests.single()
      assertEquals(HttpMethod.Post, request.method)
      assertEquals("/api/v1/tasks", request.url.encodedPath)
      val sentBody = (request.body as TextContent).text
      assertTrue(sentBody.contains("\"productionId\":\"p1\""))
      assertTrue(sentBody.contains("\"title\":\"Storyboard\""))
      assertEquals("t9", created.id)
    }

  @Test
  fun `downloadAttachment returns the cached path without hitting the network`() =
    runTest {
      val requests = mutableListOf<HttpRequestData>()
      val attachments = FakeAttachmentFileManager(cached = "/cache/t1/doc.pdf")
      val repo = repository(requests, attachments = attachments) { "" }

      val outcome = repo.downloadAttachment("t1", "doc.pdf", expectedBytes = 10)

      assertEquals(Outcome.Success("/cache/t1/doc.pdf"), outcome)
      assertTrue(requests.isEmpty(), "a cached attachment must not trigger a request")
    }

  @Test
  fun `downloadAttachment fails offline before any request`() =
    runTest {
      val requests = mutableListOf<HttpRequestData>()
      val repo = repository(requests, connectivity = FakeConnectivityObserver(online = false)) { "" }

      val outcome = repo.downloadAttachment("t1", "doc.pdf", expectedBytes = 10)

      assertIs<Outcome.Failure>(outcome)
      assertIs<DomainError.Offline>(outcome.error)
      assertTrue(requests.isEmpty())
    }

  @Test
  fun `downloadAttachment fails when free space is below the expected size`() =
    runTest {
      val requests = mutableListOf<HttpRequestData>()
      val repo = repository(requests, attachments = FakeAttachmentFileManager(available = 5)) { "" }

      val outcome = repo.downloadAttachment("t1", "doc.pdf", expectedBytes = 100)

      assertEquals(Outcome.Failure(DomainError.InsufficientStorage), outcome)
      assertTrue(requests.isEmpty())
    }

  @Test
  fun `downloadAttachment streams the body into storage and returns the saved path`() =
    runTest {
      val requests = mutableListOf<HttpRequestData>()
      val attachments = FakeAttachmentFileManager(available = Long.MAX_VALUE)
      val repo = repository(requests, attachments = attachments) { "file-bytes" }

      val outcome = repo.downloadAttachment("t1", "doc.pdf", expectedBytes = 5)

      assertEquals(Outcome.Success("/saved/t1/doc.pdf"), outcome)
      assertEquals("/api/v1/tasks/t1/attachment", requests.single().url.encodedPath)
      assertContentEquals("file-bytes".encodeToByteArray(), attachments.savedBytes)
    }

  private fun repository(
    requests: MutableList<HttpRequestData> = mutableListOf(),
    connectivity: ConnectivityObserver = FakeConnectivityObserver(online = true),
    attachments: AttachmentFileManager = FakeAttachmentFileManager(),
    body: () -> String
  ): TasksRepositoryImpl {
    val client = HttpClient(
      MockEngine { request ->
        requests += request
        respond(
          content = body(),
          headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        )
      }
    ) {
      install(ContentNegotiation) { json() }
      defaultRequest { contentType(ContentType.Application.Json) }
    }
    return TasksRepositoryImpl(
      client,
      NetworkConfig(baseUrl = "http://test", isDebug = false),
      connectivity,
      attachments
    )
  }

  private fun taskDetailJson(
    id: String,
    status: String = "OPEN"
  ): String =
    """
    {
      "id":"$id",
      "productionId":"p1",
      "productionTitle":"Pilot",
      "title":"Storyboard",
      "description":null,
      "dueDate":null,
      "status":"$status",
      "priority":"MEDIUM",
      "assigneeUserId":null,
      "assignee":null,
      "createdAt":"2026-01-01T00:00:00Z"
    }
    """.trimIndent()

  private class FakeConnectivityObserver(
    private val online: Boolean
  ) : ConnectivityObserver {
    override val isOnline: Flow<Boolean> = flowOf(online)

    override fun isCurrentlyOnline(): Boolean = online
  }

  private class FakeAttachmentFileManager(
    private val cached: String? = null,
    private val available: Long = Long.MAX_VALUE
  ) : AttachmentFileManager {
    var savedBytes: ByteArray? = null

    override fun cachedAttachment(
      taskId: String,
      fileName: String
    ): String? = cached

    override suspend fun saveDownloaded(
      taskId: String,
      fileName: String,
      bytes: ByteArray
    ): String {
      savedBytes = bytes
      return "/saved/$taskId/$fileName"
    }

    override fun readBytes(localPath: String): ByteArray = ByteArray(0)

    override fun delete(localPath: String) = Unit

    override fun openWith(
      localPath: String,
      contentType: String
    ) = Unit

    override fun availableBytes(): Long = available
  }
}
