package com.frame.zero.routes

import com.frame.zero.domain.production.Genre
import com.frame.zero.domain.production.ProductionPhase
import com.frame.zero.domain.schedule.ScheduleEventKind
import com.frame.zero.dto.production.CreateProductionRequest
import com.frame.zero.dto.schedule.CreateScheduleEventRequest
import com.frame.zero.dto.schedule.ScheduleEventDto
import com.frame.zero.dto.schedule.ScheduleResponse
import com.frame.zero.routes.testing.TestAppEnv
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
import java.util.UUID
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.context.GlobalContext
import org.koin.core.context.stopKoin

class ScheduleRoutesTest {
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
      wrapDate = LocalDate(2026, 12, 31),
    )

  @Test
  fun `GET schedule day view returns 200`() = testApplication {
    val env = TestAppEnv()
    application { env.configure(this) }
    val userId = UUID.randomUUID()
    val token = env.tokenFor(userId)

    val response =
      client.get("/api/v1/schedule?view=day&date=2026-05-04") {
        header(HttpHeaders.Authorization, "Bearer $token")
      }

    assertEquals(HttpStatusCode.OK, response.status)
    val body = json.decodeFromString<ScheduleResponse>(response.bodyAsText())
    assertEquals(1, body.days.size)
    assertEquals(LocalDate(2026, 5, 4), body.rangeStart)
  }

  @Test
  fun `GET schedule week view returns 7 days`() = testApplication {
    val env = TestAppEnv()
    application { env.configure(this) }
    val userId = UUID.randomUUID()
    val token = env.tokenFor(userId)

    val response =
      client.get("/api/v1/schedule?view=week&date=2026-05-04") {
        header(HttpHeaders.Authorization, "Bearer $token")
      }

    assertEquals(HttpStatusCode.OK, response.status)
    val body = json.decodeFromString<ScheduleResponse>(response.bodyAsText())
    assertEquals(7, body.days.size)
  }

  @Test
  fun `GET schedule without token returns 401`() = testApplication {
    val env = TestAppEnv()
    application { env.configure(this) }

    val response = client.get("/api/v1/schedule?view=day&date=2026-05-04")

    assertEquals(HttpStatusCode.Unauthorized, response.status)
  }

  @Test
  fun `GET schedule missing view returns 400`() = testApplication {
    val env = TestAppEnv()
    application { env.configure(this) }
    val userId = UUID.randomUUID()
    val token = env.tokenFor(userId)

    val response =
      client.get("/api/v1/schedule?date=2026-05-04") {
        header(HttpHeaders.Authorization, "Bearer $token")
      }

    assertEquals(HttpStatusCode.BadRequest, response.status)
  }

  @Test
  fun `POST schedule creates event and returns 201`() = testApplication {
    val env = TestAppEnv()
    application { env.configure(this) }
    val userId = UUID.randomUUID()
    val token = env.tokenFor(userId)
    val prod = env.productionService.create(userId, productionRequest)
    val request =
      CreateScheduleEventRequest(
        productionId = prod.id,
        title = "Table read",
        startsAt = Instant.parse("2026-05-04T10:00:00Z"),
        endsAt = Instant.parse("2026-05-04T12:00:00Z"),
        kind = ScheduleEventKind.MEETING,
      )

    val response =
      client.post("/api/v1/schedule") {
        header(HttpHeaders.Authorization, "Bearer $token")
        contentType(ContentType.Application.Json)
        setBody(json.encodeToString(request))
      }

    assertEquals(HttpStatusCode.Created, response.status)
    val body = json.decodeFromString<ScheduleEventDto>(response.bodyAsText())
    assertEquals("Table read", body.title)
  }

  @Test
  fun `POST schedule for non-member production returns 403`() = testApplication {
    val env = TestAppEnv()
    application { env.configure(this) }
    val ownerId = UUID.randomUUID()
    val strangerId = UUID.randomUUID()
    val strangerToken = env.tokenFor(strangerId)
    val prod = env.productionService.create(ownerId, productionRequest)

    val response =
      client.post("/api/v1/schedule") {
        header(HttpHeaders.Authorization, "Bearer $strangerToken")
        contentType(ContentType.Application.Json)
        setBody(
          json.encodeToString(
            CreateScheduleEventRequest(
              productionId = prod.id,
              title = "Shoot",
              startsAt = Instant.parse("2026-05-04T10:00:00Z"),
              endsAt = Instant.parse("2026-05-04T18:00:00Z"),
              kind = ScheduleEventKind.SHOOT,
            )
          )
        )
      }

    assertEquals(HttpStatusCode.Forbidden, response.status)
  }
}
