package com.frame.zero.domain

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException
import kotlin.test.Test
import kotlin.test.assertEquals

class DomainErrorMapperTest {
  @Test
  fun `DomainException unwraps to its carried error`() {
    val error = DomainException(DomainError.EmailAlreadyExists).toDomainError()

    assertEquals(DomainError.EmailAlreadyExists, error)
  }

  @Test
  fun `OfflineException maps to Offline`() {
    val error = OfflineException("No internet connection").toDomainError()

    assertEquals(DomainError.Offline, error)
  }

  @Test
  fun `IOException maps to Server`() {
    val error = IOException("connection refused").toDomainError()

    assertEquals(DomainError.Server, error)
  }

  @Test
  fun `SerializationException maps to Unknown`() {
    val error = SerializationException("malformed json").toDomainError()

    assertEquals(DomainError.Unknown, error)
  }

  @Test
  fun `generic Throwable maps to Unknown`() {
    assertEquals(DomainError.Unknown, RuntimeException("kaboom").toDomainError())
  }

  // -- ServerErrorException: server code drives the mapping, not bare status ------------------

  @Test
  fun `INVALID_CREDENTIALS maps to InvalidCredentials`() {
    val error = ServerErrorException(code = "INVALID_CREDENTIALS", status = 401).toDomainError()

    assertEquals(DomainError.InvalidCredentials, error)
  }

  @Test
  fun `EMAIL_ALREADY_EXISTS maps to EmailAlreadyExists`() {
    val error = ServerErrorException(code = "EMAIL_ALREADY_EXISTS", status = 409).toDomainError()

    assertEquals(DomainError.EmailAlreadyExists, error)
  }

  @Test
  fun `NOT_FOUND maps to NotFound`() {
    val error = ServerErrorException(code = "NOT_FOUND", status = 404).toDomainError()

    assertEquals(DomainError.NotFound, error)
  }

  @Test
  fun `UNAUTHORIZED maps to Forbidden, not InvalidCredentials`() {
    val error = ServerErrorException(code = "UNAUTHORIZED", status = 401).toDomainError()

    assertEquals(DomainError.Forbidden, error)
  }

  @Test
  fun `INVALID_REFRESH_TOKEN maps to Forbidden`() {
    val error = ServerErrorException(code = "INVALID_REFRESH_TOKEN", status = 401).toDomainError()

    assertEquals(DomainError.Forbidden, error)
  }

  @Test
  fun `FORBIDDEN maps to Forbidden`() {
    val error = ServerErrorException(code = "FORBIDDEN", status = 403).toDomainError()

    assertEquals(DomainError.Forbidden, error)
  }

  @Test
  fun `CONFLICT maps to Conflict`() {
    val error = ServerErrorException(code = "CONFLICT", status = 409).toDomainError()

    assertEquals(DomainError.Conflict, error)
  }

  @Test
  fun `PAYLOAD_TOO_LARGE maps to PayloadTooLarge`() {
    val error = ServerErrorException(code = "PAYLOAD_TOO_LARGE", status = 413).toDomainError()

    assertEquals(DomainError.PayloadTooLarge, error)
  }

  @Test
  fun `INVALID_PHASE_TRANSITION maps to InvalidPhaseTransition`() {
    val error = ServerErrorException(code = "INVALID_PHASE_TRANSITION", status = 409).toDomainError()

    assertEquals(DomainError.InvalidPhaseTransition, error)
  }

  @Test
  fun `VALIDATION_ERROR maps to Validation carrying the field map`() {
    val fields = mapOf("email" to "must not be blank")
    val error = ServerErrorException(code = "VALIDATION_ERROR", status = 400, fields = fields).toDomainError()

    assertEquals(DomainError.Validation(fields), error)
  }

  @Test
  fun `VALIDATION_ERROR with no fields maps to Validation with an empty map`() {
    val error = ServerErrorException(code = "VALIDATION_ERROR", status = 400).toDomainError()

    assertEquals(DomainError.Validation(emptyMap()), error)
  }

  @Test
  fun `INTERNAL maps to Server`() {
    val error = ServerErrorException(code = "INTERNAL", status = 500).toDomainError()

    assertEquals(DomainError.Server, error)
  }

  @Test
  fun `unrecognized code falls back to status bucketing`() {
    val error = ServerErrorException(code = "SOMETHING_NEW", status = 503).toDomainError()

    assertEquals(DomainError.Server, error)
  }

  // -- Raw ResponseException fallback (validator couldn't parse an ErrorResponseDto) ----------

  @Test
  fun `404 ResponseException maps to NotFound`() =
    runTest {
      assertEquals(DomainError.NotFound, exceptionFor(HttpStatusCode.NotFound).toDomainError())
    }

  @Test
  fun `401 ResponseException maps to Forbidden`() =
    runTest {
      assertEquals(DomainError.Forbidden, exceptionFor(HttpStatusCode.Unauthorized).toDomainError())
    }

  @Test
  fun `403 ResponseException maps to Forbidden`() =
    runTest {
      assertEquals(DomainError.Forbidden, exceptionFor(HttpStatusCode.Forbidden).toDomainError())
    }

  @Test
  fun `409 ResponseException maps to Conflict`() =
    runTest {
      assertEquals(DomainError.Conflict, exceptionFor(HttpStatusCode.Conflict).toDomainError())
    }

  @Test
  fun `5xx ResponseException maps to Server`() =
    runTest {
      assertEquals(DomainError.Server, exceptionFor(HttpStatusCode.InternalServerError).toDomainError())
    }

  @Test
  fun `other 4xx ResponseException maps to Unknown`() =
    runTest {
      assertEquals(DomainError.Unknown, exceptionFor(HttpStatusCode.BadRequest).toDomainError())
    }

  private suspend fun exceptionFor(status: HttpStatusCode): Throwable {
    val client = HttpClient(MockEngine { respond(content = "{}", status = status) }) { expectSuccess = true }
    val captured = runCatching { client.get("http://test/x") }.exceptionOrNull()
      ?: error("Expected a thrown exception for status $status")
    client.close()
    return captured
  }
}
