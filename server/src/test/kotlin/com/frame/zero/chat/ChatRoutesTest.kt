package com.frame.zero.chat

import com.frame.zero.common.testing.TestAppEnv
import com.frame.zero.domain.production.Genre
import com.frame.zero.dto.chat.ChatMessageDto
import com.frame.zero.dto.chat.ConversationDto
import com.frame.zero.dto.chat.SendMessageRequest
import com.frame.zero.dto.common.CursorPagedResponse
import com.frame.zero.dto.production.CreateProductionRequest
import com.frame.zero.dto.task.CreateTaskRequest
import io.ktor.client.HttpClient
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
import kotlinx.serialization.json.Json
import org.koin.core.context.GlobalContext
import org.koin.core.context.stopKoin
import java.util.UUID
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ChatRoutesTest {
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

  private suspend fun HttpClient.conversationId(
    token: String,
    taskId: String
  ): String {
    val response = get("/api/v1/tasks/$taskId/conversation") {
      header(HttpHeaders.Authorization, "Bearer $token")
    }
    return json.decodeFromString<ConversationDto>(response.bodyAsText()).id
  }

  private suspend fun HttpClient.postMessage(
    token: String,
    conversationId: String,
    clientMessageId: String,
    body: String
  ) = post("/api/v1/conversations/$conversationId/messages") {
    header(HttpHeaders.Authorization, "Bearer $token")
    contentType(ContentType.Application.Json)
    setBody(json.encodeToString(SendMessageRequest(clientMessageId = clientMessageId, body = body)))
  }

  @Test
  fun `GET conversation without token returns 401`() =
    testApplication {
      val env = TestAppEnv()
      application { env.configure(this) }

      val response = client.get("/api/v1/tasks/${UUID.randomUUID()}/conversation")

      assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

  @Test
  fun `GET conversation as the task creator returns 200 with a TASK conversation`() =
    testApplication {
      val env = TestAppEnv()
      application { env.configure(this) }
      val owner = UUID.randomUUID()
      val token = env.tokenFor(owner)
      val prod = env.productionService.createProduction(owner, productionRequest)
      val task = env.taskService.create(owner, CreateTaskRequest(productionId = prod.id, title = "T"))

      val response = client.get("/api/v1/tasks/${task.id}/conversation") {
        header(HttpHeaders.Authorization, "Bearer $token")
      }

      assertEquals(HttpStatusCode.OK, response.status)
      val conversation = json.decodeFromString<ConversationDto>(response.bodyAsText())
      assertIs<ConversationDto.Task>(conversation)
      assertEquals(task.id, conversation.taskId)
    }

  @Test
  fun `POST message returns 201 with the canonical message`() =
    testApplication {
      val env = TestAppEnv()
      application { env.configure(this) }
      val owner = UUID.randomUUID()
      val token = env.tokenFor(owner)
      val prod = env.productionService.createProduction(owner, productionRequest)
      val task = env.taskService.create(owner, CreateTaskRequest(productionId = prod.id, title = "T"))
      val conversationId = client.conversationId(token, task.id)

      val response = client.postMessage(token, conversationId, "c1", "hello")

      assertEquals(HttpStatusCode.Created, response.status)
      val message = json.decodeFromString<ChatMessageDto>(response.bodyAsText())
      assertEquals("hello", message.body)
      assertEquals(1L, message.ordinal)
    }

  @Test
  fun `GET messages returns the conversation history newest-first`() =
    testApplication {
      val env = TestAppEnv()
      application { env.configure(this) }
      val owner = UUID.randomUUID()
      val token = env.tokenFor(owner)
      val prod = env.productionService.createProduction(owner, productionRequest)
      val task = env.taskService.create(owner, CreateTaskRequest(productionId = prod.id, title = "T"))
      val conversationId = client.conversationId(token, task.id)
      client.postMessage(token, conversationId, "c1", "one")
      client.postMessage(token, conversationId, "c2", "two")

      val response = client.get("/api/v1/conversations/$conversationId/messages") {
        header(HttpHeaders.Authorization, "Bearer $token")
      }

      assertEquals(HttpStatusCode.OK, response.status)
      val page = json.decodeFromString<CursorPagedResponse<ChatMessageDto>>(response.bodyAsText())
      assertEquals(listOf(2L, 1L), page.items.map { it.ordinal })
    }

  @Test
  fun `GET conversation for a task the caller is not in the circle of returns 403`() =
    testApplication {
      val env = TestAppEnv()
      application { env.configure(this) }
      val owner = UUID.randomUUID()
      val strangerToken = env.tokenFor(UUID.randomUUID())
      val prod = env.productionService.createProduction(owner, productionRequest)
      val task = env.taskService.create(owner, CreateTaskRequest(productionId = prod.id, title = "T"))

      val response = client.get("/api/v1/tasks/${task.id}/conversation") {
        header(HttpHeaders.Authorization, "Bearer $strangerToken")
      }

      assertEquals(HttpStatusCode.Forbidden, response.status)
    }
}
