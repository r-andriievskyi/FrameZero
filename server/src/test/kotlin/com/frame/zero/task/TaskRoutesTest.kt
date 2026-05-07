package com.frame.zero.task

import com.frame.zero.common.testing.TestAppEnv
import com.frame.zero.domain.production.Genre
import com.frame.zero.domain.production.ProductionPhase
import com.frame.zero.dto.production.CreateProductionRequest
import com.frame.zero.dto.task.CreateTaskRequest
import com.frame.zero.dto.task.TaskDetailDto
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import kotlinx.datetime.LocalDate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.context.GlobalContext
import org.koin.core.context.stopKoin
import java.util.UUID
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

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
      phase = ProductionPhase.PRODUCTION,
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

      val response =
        client.post("/api/v1/tasks") {
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

      val response =
        client.post("/api/v1/tasks") {
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

      val response =
        client.post("/api/v1/tasks") {
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

      val response =
        client.post("/api/v1/tasks") {
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
        CreateTaskRequest(productionId = prod.id, title = "T1"),
        java.time.ZoneId.of("UTC")
      )

      val response =
        client.get(
          "/api/v1/tasks?assignee=me"
        ) { header(HttpHeaders.Authorization, "Bearer $token") }

      assertEquals(HttpStatusCode.OK, response.status)
    }

  @Test
  fun `DELETE task returns 204 for owner`() =
    testApplication {
      val env = TestAppEnv()
      application { env.configure(this) }
      val userId = UUID.randomUUID()
      val token = env.tokenFor(userId)
      val prod = env.productionService.createProduction(userId, productionRequest)
      val task =
        env.taskService.create(
          userId,
          CreateTaskRequest(productionId = prod.id, title = "T"),
          java.time.ZoneId.of("UTC")
        )

      val response =
        client.delete("/api/v1/tasks/${task.id}") {
          header(HttpHeaders.Authorization, "Bearer $token")
        }

      assertEquals(HttpStatusCode.NoContent, response.status)
    }
}
