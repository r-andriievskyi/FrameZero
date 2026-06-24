package com.frame.zero.task

import com.frame.zero.common.testing.TestAppEnv
import com.frame.zero.domain.production.Genre
import com.frame.zero.dto.production.CreateProductionRequest
import com.frame.zero.dto.task.CreateTaskRequest
import com.frame.zero.dto.task.TaskDetailDto
import io.ktor.client.request.delete
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.readRawBytes
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import org.koin.core.context.GlobalContext
import org.koin.core.context.stopKoin
import java.util.UUID
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class TaskRoutesTest {
  private val json = Json { ignoreUnknownKeys = true }

  @AfterTest
  fun cleanup() {
    if (GlobalContext.getOrNull() != null) stopKoin()
  }

  private val productionRequest =
    CreateProductionRequest(
      title = "Film",
      genre = Genre.DRAMA,
      startDate = LocalDate(2026, 1, 1),
      wrapDate = LocalDate(2026, 12, 31)
    )

  @Test
  fun `POST tasks returns 201 with created task`() =
    testApplication {
      val env = TestAppEnv()
      application { env.configure(this) }
      val userId = UUID.randomUUID()
      val token = env.tokenFor(userId)
      val prod = env.productionService.createProduction(userId, productionRequest)
      val request = CreateTaskRequest(productionId = prod.id, title = "Lock script")

      val response = client.post("/api/v1/tasks") {
        header(HttpHeaders.Authorization, "Bearer $token")
        contentType(ContentType.Application.Json)
        setBody(json.encodeToString(request))
      }

      assertEquals(HttpStatusCode.Created, response.status)
      val body = json.decodeFromString<TaskDetailDto>(response.bodyAsText())
      assertEquals("Lock script", body.title)
    }

  @Test
  fun `POST tasks without token returns 401`() =
    testApplication {
      val env = TestAppEnv()
      application { env.configure(this) }
      val userId = UUID.randomUUID()
      val prod = env.productionService.createProduction(userId, productionRequest)

      val response = client.post("/api/v1/tasks") {
        contentType(ContentType.Application.Json)
        setBody(json.encodeToString(CreateTaskRequest(productionId = prod.id, title = "Task")))
      }

      assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

  @Test
  fun `POST tasks for non-member production returns 403`() =
    testApplication {
      val env = TestAppEnv()
      application { env.configure(this) }
      val ownerId = UUID.randomUUID()
      val strangerId = UUID.randomUUID()
      val strangerToken = env.tokenFor(strangerId)
      val prod = env.productionService.createProduction(ownerId, productionRequest)

      val response = client.post("/api/v1/tasks") {
        header(HttpHeaders.Authorization, "Bearer $strangerToken")
        contentType(ContentType.Application.Json)
        setBody(json.encodeToString(CreateTaskRequest(productionId = prod.id, title = "Task")))
      }

      assertEquals(HttpStatusCode.Forbidden, response.status)
    }

  @Test
  fun `POST tasks with blank title returns 400`() =
    testApplication {
      val env = TestAppEnv()
      application { env.configure(this) }
      val userId = UUID.randomUUID()
      val token = env.tokenFor(userId)
      val prod = env.productionService.createProduction(userId, productionRequest)

      val response = client.post("/api/v1/tasks") {
        header(HttpHeaders.Authorization, "Bearer $token")
        contentType(ContentType.Application.Json)
        setBody(json.encodeToString(CreateTaskRequest(productionId = prod.id, title = "   ")))
      }

      assertEquals(HttpStatusCode.BadRequest, response.status)
    }

  @Test
  fun `GET tasks without token returns 401`() =
    testApplication {
      val env = TestAppEnv()
      application { env.configure(this) }

      val response = client.get("/api/v1/tasks")

      assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

  @Test
  fun `GET tasks returns paginated list`() =
    testApplication {
      val env = TestAppEnv()
      application { env.configure(this) }
      val userId = UUID.randomUUID()
      val token = env.tokenFor(userId)
      val prod = env.productionService.createProduction(userId, productionRequest)
      env.taskService.create(
        userId,
        CreateTaskRequest(productionId = prod.id, title = "T1")
      )

      val response = client.get(
        "/api/v1/tasks?assignee=me"
      ) { header(HttpHeaders.Authorization, "Bearer $token") }

      assertEquals(HttpStatusCode.OK, response.status)
    }

  @Test
  fun `POST tasks multipart with a file returns 201 and exposes the attachment`() =
    testApplication {
      val env = TestAppEnv()
      application { env.configure(this) }
      val userId = UUID.randomUUID()
      val token = env.tokenFor(userId)
      val prod = env.productionService.createProduction(userId, productionRequest)
      val bytes = byteArrayOf(1, 2, 3, 4, 5)

      val response = client.post("/api/v1/tasks") {
        header(HttpHeaders.Authorization, "Bearer $token")
        setBody(
          MultiPartFormDataContent(
            formData {
              append("productionId", prod.id)
              append("title", "Storyboard")
              append(
                "file",
                bytes,
                Headers.build {
                  append(HttpHeaders.ContentType, "application/pdf")
                  append(HttpHeaders.ContentDisposition, "filename=\"board.pdf\"")
                }
              )
            }
          )
        )
      }

      assertEquals(HttpStatusCode.Created, response.status)
      val body = json.decodeFromString<TaskDetailDto>(response.bodyAsText())
      val attachment = assertNotNull(body.attachment)
      assertEquals("board.pdf", attachment.fileName)
      assertEquals(bytes.size.toLong(), attachment.sizeBytes)
      assertEquals("application/pdf", attachment.contentType)
    }

  @Test
  fun `GET attachment streams back the uploaded bytes`() =
    testApplication {
      val env = TestAppEnv()
      application { env.configure(this) }
      val userId = UUID.randomUUID()
      val token = env.tokenFor(userId)
      val prod = env.productionService.createProduction(userId, productionRequest)
      val bytes = byteArrayOf(10, 20, 30, 40)

      val created = client.post("/api/v1/tasks") {
        header(HttpHeaders.Authorization, "Bearer $token")
        setBody(
          MultiPartFormDataContent(
            formData {
              append("productionId", prod.id)
              append("title", "Has file")
              append(
                "file",
                bytes,
                Headers.build { append(HttpHeaders.ContentDisposition, "filename=\"data.bin\"") }
              )
            }
          )
        )
      }
      val taskId = json.decodeFromString<TaskDetailDto>(created.bodyAsText()).id

      val download = client.get("/api/v1/tasks/$taskId/attachment") {
        header(HttpHeaders.Authorization, "Bearer $token")
      }

      assertEquals(HttpStatusCode.OK, download.status)
      assertContentEquals(bytes, download.readRawBytes())
    }

  @Test
  fun `POST tasks multipart keeps only the first file part`() =
    testApplication {
      val env = TestAppEnv()
      application { env.configure(this) }
      val userId = UUID.randomUUID()
      val token = env.tokenFor(userId)
      val prod = env.productionService.createProduction(userId, productionRequest)

      val response = client.post("/api/v1/tasks") {
        header(HttpHeaders.Authorization, "Bearer $token")
        setBody(
          MultiPartFormDataContent(
            formData {
              append("productionId", prod.id)
              append("title", "Two files")
              append(
                "file",
                byteArrayOf(1, 2, 3),
                Headers.build { append(HttpHeaders.ContentDisposition, "filename=\"first.bin\"") }
              )
              append(
                "file",
                byteArrayOf(9, 9, 9, 9, 9, 9),
                Headers.build { append(HttpHeaders.ContentDisposition, "filename=\"second.bin\"") }
              )
            }
          )
        )
      }

      assertEquals(HttpStatusCode.Created, response.status)
      val attachment = assertNotNull(json.decodeFromString<TaskDetailDto>(response.bodyAsText()).attachment)
      assertEquals("first.bin", attachment.fileName)
      assertEquals(3, attachment.sizeBytes)
    }

  @Test
  fun `POST tasks with an over-long Idempotency-Key returns 400`() =
    testApplication {
      val env = TestAppEnv()
      application { env.configure(this) }
      val userId = UUID.randomUUID()
      val token = env.tokenFor(userId)
      val prod = env.productionService.createProduction(userId, productionRequest)

      val response = client.post("/api/v1/tasks") {
        header(HttpHeaders.Authorization, "Bearer $token")
        header("Idempotency-Key", "x".repeat(65))
        contentType(ContentType.Application.Json)
        setBody(json.encodeToString(CreateTaskRequest(productionId = prod.id, title = "T")))
      }

      assertEquals(HttpStatusCode.BadRequest, response.status)
    }

  @Test
  fun `GET attachment returns 404 when the task has none`() =
    testApplication {
      val env = TestAppEnv()
      application { env.configure(this) }
      val userId = UUID.randomUUID()
      val token = env.tokenFor(userId)
      val prod = env.productionService.createProduction(userId, productionRequest)
      val task = env.taskService.create(userId, CreateTaskRequest(productionId = prod.id, title = "No file"))

      val response = client.get("/api/v1/tasks/${task.id}/attachment") {
        header(HttpHeaders.Authorization, "Bearer $token")
      }

      assertEquals(HttpStatusCode.NotFound, response.status)
    }

  @Test
  fun `POST tasks with a repeated Idempotency-Key returns the same task`() =
    testApplication {
      val env = TestAppEnv()
      application { env.configure(this) }
      val userId = UUID.randomUUID()
      val token = env.tokenFor(userId)
      val prod = env.productionService.createProduction(userId, productionRequest)
      val request = CreateTaskRequest(productionId = prod.id, title = "Once")

      suspend fun postWithKey() =
        client.post("/api/v1/tasks") {
          header(HttpHeaders.Authorization, "Bearer $token")
          header("Idempotency-Key", "key-123")
          contentType(ContentType.Application.Json)
          setBody(json.encodeToString(request))
        }

      val first = postWithKey()
      val second = postWithKey()

      val firstId = json.decodeFromString<TaskDetailDto>(first.bodyAsText()).id
      val secondId = json.decodeFromString<TaskDetailDto>(second.bodyAsText()).id
      assertEquals(firstId, secondId, "replay must not create a second task")
      assertEquals(1, env.tasks.tasks.size)
      assertNull(env.tasks.tasks.single().attachment)
    }

  @Test
  fun `DELETE task returns 204 for owner`() =
    testApplication {
      val env = TestAppEnv()
      application { env.configure(this) }
      val userId = UUID.randomUUID()
      val token = env.tokenFor(userId)
      val prod = env.productionService.createProduction(userId, productionRequest)
      val task = env.taskService.create(
        userId,
        CreateTaskRequest(productionId = prod.id, title = "T")
      )

      val response = client.delete("/api/v1/tasks/${task.id}") {
        header(HttpHeaders.Authorization, "Bearer $token")
      }

      assertEquals(HttpStatusCode.NoContent, response.status)
    }
}
