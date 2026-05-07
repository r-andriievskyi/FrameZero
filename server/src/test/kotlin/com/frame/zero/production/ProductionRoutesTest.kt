package com.frame.zero.production

import com.frame.zero.common.testing.TestAppEnv
import com.frame.zero.domain.production.Genre
import com.frame.zero.domain.production.ProductionPhase
import com.frame.zero.dto.production.CreateProductionRequest
import com.frame.zero.dto.production.ProductionDetailDto
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

class ProductionRoutesTest {
  private val json = Json { ignoreUnknownKeys = true }

  @AfterTest
  fun cleanup() {
    if (GlobalContext.getOrNull() != null) stopKoin()
  }

  private val validRequest =
    CreateProductionRequest(
      title = "Test Film",
      genre = Genre.DRAMA,
      phase = ProductionPhase.DEVELOPMENT,
      startDate = LocalDate(2026, 1, 1),
      wrapDate = LocalDate(2026, 6, 30)
    )

  @Test
  fun `POST productions returns 201 with created production`() =
    testApplication {
      val env = TestAppEnv()
      application { env.configure(this) }
      val userId = UUID.randomUUID()
      val token = env.tokenFor(userId)

      val response =
        client.post("/api/v1/productions") {
          header(HttpHeaders.Authorization, "Bearer $token")
          contentType(ContentType.Application.Json)
          setBody(json.encodeToString(validRequest))
        }

      assertEquals(HttpStatusCode.Created, response.status)
      val body = json.decodeFromString<ProductionDetailDto>(response.bodyAsText())
      assertEquals("Test Film", body.title)
      assertEquals(Genre.DRAMA, body.genre)
    }

  @Test
  fun `POST productions without token returns 401`() =
    testApplication {
      val env = TestAppEnv()
      application { env.configure(this) }

      val response =
        client.post("/api/v1/productions") {
          contentType(ContentType.Application.Json)
          setBody(json.encodeToString(validRequest))
        }

      assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

  @Test
  fun `POST productions with invalid body returns 400`() =
    testApplication {
      val env = TestAppEnv()
      application { env.configure(this) }
      val userId = UUID.randomUUID()
      val token = env.tokenFor(userId)

      val response =
        client.post("/api/v1/productions") {
          header(HttpHeaders.Authorization, "Bearer $token")
          contentType(ContentType.Application.Json)
          setBody("{not valid json")
        }

      assertEquals(HttpStatusCode.BadRequest, response.status)
    }

  @Test
  fun `POST productions with wrapDate before startDate returns 400`() =
    testApplication {
      val env = TestAppEnv()
      application { env.configure(this) }
      val userId = UUID.randomUUID()
      val token = env.tokenFor(userId)
      val badRequest =
        validRequest.copy(startDate = LocalDate(2026, 6, 1), wrapDate = LocalDate(2026, 1, 1))

      val response =
        client.post("/api/v1/productions") {
          header(HttpHeaders.Authorization, "Bearer $token")
          contentType(ContentType.Application.Json)
          setBody(json.encodeToString(badRequest))
        }

      assertEquals(HttpStatusCode.BadRequest, response.status)
    }

  @Test
  fun `GET production by id returns 200 for owner`() =
    testApplication {
      val env = TestAppEnv()
      application { env.configure(this) }
      val userId = UUID.randomUUID()
      val token = env.tokenFor(userId)
      val created = env.productionService.createProduction(userId, validRequest)

      val response =
        client.get("/api/v1/productions/${created.id}") {
          header(HttpHeaders.Authorization, "Bearer $token")
        }

      assertEquals(HttpStatusCode.OK, response.status)
      val body = json.decodeFromString<ProductionDetailDto>(response.bodyAsText())
      assertEquals(created.id, body.id)
    }

  @Test
  fun `GET production by id without token returns 401`() =
    testApplication {
      val env = TestAppEnv()
      application { env.configure(this) }
      val ownerId = UUID.randomUUID()
      val created = env.productionService.createProduction(ownerId, validRequest)

      val response = client.get("/api/v1/productions/${created.id}")

      assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

  @Test
  fun `GET production by id returns 403 for non-member`() =
    testApplication {
      val env = TestAppEnv()
      application { env.configure(this) }
      val ownerId = UUID.randomUUID()
      val strangerToken = env.tokenFor(UUID.randomUUID())
      val created = env.productionService.createProduction(ownerId, validRequest)

      val response =
        client.get("/api/v1/productions/${created.id}") {
          header(HttpHeaders.Authorization, "Bearer $strangerToken")
        }

      assertEquals(HttpStatusCode.Forbidden, response.status)
    }

  @Test
  fun `GET production by id returns 404 for unknown id`() =
    testApplication {
      val env = TestAppEnv()
      application { env.configure(this) }
      val userId = UUID.randomUUID()
      val token = env.tokenFor(userId)

      val response =
        client.get("/api/v1/productions/${UUID.randomUUID()}") {
          header(HttpHeaders.Authorization, "Bearer $token")
        }

      assertEquals(HttpStatusCode.NotFound, response.status)
    }

  @Test
  fun `DELETE production returns 204 for owner`() =
    testApplication {
      val env = TestAppEnv()
      application { env.configure(this) }
      val userId = UUID.randomUUID()
      val token = env.tokenFor(userId)
      val created = env.productionService.createProduction(userId, validRequest)

      val response =
        client.delete("/api/v1/productions/${created.id}") {
          header(HttpHeaders.Authorization, "Bearer $token")
        }

      assertEquals(HttpStatusCode.NoContent, response.status)
    }

  @Test
  fun `DELETE production returns 403 for non-owner`() =
    testApplication {
      val env = TestAppEnv()
      application { env.configure(this) }
      val ownerId = UUID.randomUUID()
      val otherId = UUID.randomUUID()
      val otherToken = env.tokenFor(otherId)
      val created = env.productionService.createProduction(ownerId, validRequest)
      env.productionMembers.add(UUID.fromString(created.id), otherId, "Other", "Producer", null)

      val response =
        client.delete("/api/v1/productions/${created.id}") {
          header(HttpHeaders.Authorization, "Bearer $otherToken")
        }

      assertEquals(HttpStatusCode.Forbidden, response.status)
    }
}
