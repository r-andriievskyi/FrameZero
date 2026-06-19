package com.frame.zero.notification

import com.frame.zero.common.testing.TestAppEnv
import com.frame.zero.dto.notification.MarkReadRequest
import com.frame.zero.dto.notification.NotificationsResponse
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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.context.GlobalContext
import org.koin.core.context.stopKoin
import java.util.UUID
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class NotificationRoutesTest {
  private val json = Json { ignoreUnknownKeys = true }

  @AfterTest
  fun cleanup() {
    if (GlobalContext.getOrNull() != null) stopKoin()
  }

  @Test
  fun `GET notifications returns 200 with empty list`() =
    testApplication {
      val env = TestAppEnv()
      application { env.configure(this) }
      val userId = UUID.randomUUID()
      val token = env.tokenFor(userId)

      val response =
        client.get("/api/v1/notifications") { header(HttpHeaders.Authorization, "Bearer $token") }

      assertEquals(HttpStatusCode.OK, response.status)
      val body = json.decodeFromString<NotificationsResponse>(response.bodyAsText())
      assertEquals(0, body.items.size)
      assertEquals(0, body.unreadCount)
    }

  @Test
  fun `GET notifications without token returns 401`() =
    testApplication {
      val env = TestAppEnv()
      application { env.configure(this) }

      val response = client.get("/api/v1/notifications")

      assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

  @Test
  fun `GET notifications returns unread count`() =
    testApplication {
      val env = TestAppEnv()
      application { env.configure(this) }
      val userId = UUID.randomUUID()
      val token = env.tokenFor(userId)
      env.notificationsRepo.create(userId, "World")
      env.notificationsRepo.create(userId, null)

      val response =
        client.get("/api/v1/notifications") { header(HttpHeaders.Authorization, "Bearer $token") }

      assertEquals(HttpStatusCode.OK, response.status)
      val body = json.decodeFromString<NotificationsResponse>(response.bodyAsText())
      assertEquals(2, body.items.size)
      assertEquals(2, body.unreadCount)
    }

  @Test
  fun `POST notifications read marks all read returns 204`() =
    testApplication {
      val env = TestAppEnv()
      application { env.configure(this) }
      val userId = UUID.randomUUID()
      val token = env.tokenFor(userId)
      env.notificationsRepo.create(userId, "Hello")

      val response =
        client.post("/api/v1/notifications/read") {
          header(HttpHeaders.Authorization, "Bearer $token")
          contentType(ContentType.Application.Json)
          setBody(json.encodeToString(MarkReadRequest(all = true)))
        }

      assertEquals(HttpStatusCode.NoContent, response.status)
    }

  @Test
  fun `POST notifications read without token returns 401`() =
    testApplication {
      val env = TestAppEnv()
      application { env.configure(this) }

      val response =
        client.post("/api/v1/notifications/read") {
          contentType(ContentType.Application.Json)
          setBody(json.encodeToString(MarkReadRequest(all = true)))
        }

      assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

  @Test
  fun `POST notifications read with empty ids and all false returns 400`() =
    testApplication {
      val env = TestAppEnv()
      application { env.configure(this) }
      val userId = UUID.randomUUID()
      val token = env.tokenFor(userId)

      val response =
        client.post("/api/v1/notifications/read") {
          header(HttpHeaders.Authorization, "Bearer $token")
          contentType(ContentType.Application.Json)
          setBody(json.encodeToString(MarkReadRequest(ids = emptyList(), all = false)))
        }

      assertEquals(HttpStatusCode.BadRequest, response.status)
    }
}
