package com.frame.zero.routes

import com.frame.zero.dto.dashboard.DashboardResponse
import com.frame.zero.routes.testing.TestAppEnv
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import java.util.UUID
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.json.Json
import org.koin.core.context.GlobalContext
import org.koin.core.context.stopKoin

class DashboardRoutesTest {
  private val json = Json { ignoreUnknownKeys = true }

  @AfterTest
  fun cleanup() {
    if (GlobalContext.getOrNull() != null) stopKoin()
  }

  @Test
  fun `GET dashboard returns 200 with aggregated response`() = testApplication {
    val env = TestAppEnv()
    application { env.configure(this) }
    val userId = UUID.randomUUID()
    val token = env.tokenFor(userId)

    val response = client.get("/api/v1/dashboard") {
      header(HttpHeaders.Authorization, "Bearer $token")
    }

    assertEquals(HttpStatusCode.OK, response.status)
    val body = json.decodeFromString<DashboardResponse>(response.bodyAsText())
    assertEquals(0, body.stats.activeProjects)
    assertEquals(0, body.stats.openTasks)
  }

  @Test
  fun `GET dashboard without token returns 401`() = testApplication {
    val env = TestAppEnv()
    application { env.configure(this) }

    val response = client.get("/api/v1/dashboard")

    assertEquals(HttpStatusCode.Unauthorized, response.status)
  }

  @Test
  fun `GET dashboard with invalid token returns 401`() = testApplication {
    val env = TestAppEnv()
    application { env.configure(this) }

    val response = client.get("/api/v1/dashboard") {
      header(HttpHeaders.Authorization, "Bearer not-a-jwt")
    }

    assertEquals(HttpStatusCode.Unauthorized, response.status)
  }
}
