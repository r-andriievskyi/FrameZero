package com.frame.zero

import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Exercises the real [installStatusPages] directly — [com.frame.zero.auth.AuthRoutesTest] covers
 * the branches reachable through auth's own business logic (`AppException`,
 * `SerializationException`), but nothing in the app naturally throws a bare
 * `IllegalArgumentException` or an unmapped `Throwable` through a real route, so those two
 * branches need their own minimal harness to ever run.
 */
class StatusPagesTest {
  private val json = Json { ignoreUnknownKeys = true }

  @Test
  fun `an IllegalArgumentException maps to 400 with a static message, never the raw cause`() =
    testApplication {
      application {
        install(ContentNegotiation) { json() }
        installStatusPages()
        routing {
          get("/boom") { throw IllegalArgumentException("raw internal detail from Exposed/UUID.fromString") }
        }
      }

      val response = client.get("/boom")

      assertEquals(HttpStatusCode.BadRequest, response.status)
      val body = json.decodeFromString<ErrorResponse>(response.bodyAsText())
      assertEquals("VALIDATION_ERROR", body.error)
      assertEquals("Invalid request", body.message)
    }

  @Test
  fun `an unmapped Throwable maps to an opaque 500, never the stack trace or message`() =
    testApplication {
      application {
        install(ContentNegotiation) { json() }
        installStatusPages()
        routing {
          get("/boom") { throw IllegalStateException("connection string: postgres://user:pw@host/db") }
        }
      }

      val response = client.get("/boom")

      assertEquals(HttpStatusCode.InternalServerError, response.status)
      val body = json.decodeFromString<ErrorResponse>(response.bodyAsText())
      assertEquals("INTERNAL", body.error)
      assertEquals("Internal server error", body.message)
    }

  @Test
  fun `an unparsable JSON body maps to 400, not the 500 a bare Throwable catch-all would give it`() =
    testApplication {
      application {
        install(ContentNegotiation) { json() }
        installStatusPages()
        routing {
          post("/echo") { call.respond(call.receive<Echo>()) }
        }
      }

      val response =
        client.post("/echo") {
          contentType(ContentType.Application.Json)
          setBody("{not valid json")
        }

      assertEquals(HttpStatusCode.BadRequest, response.status)
      val body = json.decodeFromString<ErrorResponse>(response.bodyAsText())
      assertEquals("VALIDATION_ERROR", body.error)
      assertEquals("Malformed request body", body.message)
    }

  @Test
  fun `an AppException maps to its declared status and code`() =
    testApplication {
      application {
        install(ContentNegotiation) { json() }
        installStatusPages()
        routing {
          get("/boom") { throw AppException(AppError.Forbidden) }
        }
      }

      val response = client.get("/boom")

      assertEquals(HttpStatusCode.Forbidden, response.status)
      val body = json.decodeFromString<ErrorResponse>(response.bodyAsText())
      assertEquals("FORBIDDEN", body.error)
    }

  @Serializable
  private data class Echo(
    val value: String
  )
}
