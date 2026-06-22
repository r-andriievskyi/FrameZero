package com.frame.zero.feature.task.details.data

import com.frame.zero.core.network.NetworkConfig
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
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
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
  fun `listForProduction GETs the first page and returns the cursor-paged items`() =
    runTest {
      val requests = mutableListOf<HttpRequestData>()
      val repo = repository(requests) {
        """
        {
          "items":[
            {"id":"t1","title":"A","productionTitle":"Pilot","dueDate":null,"status":"OPEN"},
            {"id":"t2","title":"B","productionTitle":"Pilot","dueDate":"2026-06-24","status":"DONE"}
          ],
          "nextCursor":"next"
        }
        """.trimIndent()
      }

      val tasks = repo.listForProduction("p1")

      val request = requests.single()
      assertEquals(HttpMethod.Get, request.method)
      assertEquals("/api/v1/tasks", request.url.encodedPath)
      assertEquals("p1", request.url.parameters["productionId"])
      assertEquals("50", request.url.parameters["limit"])
      assertEquals(listOf("t1", "t2"), tasks.map { it.id })
    }

  private fun repository(
    requests: MutableList<HttpRequestData> = mutableListOf(),
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
    return TasksRepositoryImpl(client, NetworkConfig(baseUrl = "http://test", isDebug = false))
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
}
