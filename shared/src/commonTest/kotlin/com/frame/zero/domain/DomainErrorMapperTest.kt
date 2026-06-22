package com.frame.zero.domain

import com.frame.zero.core.network.connectivity.OfflineException
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
import kotlin.test.assertIs

class DomainErrorMapperTest {
  @Test
  fun `DomainException unwraps to its carried error`() {
    val error = DomainException(DomainError.EmailAlreadyExists).toDomainError()

    assertEquals(DomainError.EmailAlreadyExists, error)
  }

  @Test
  fun `OfflineException maps to Offline with the original message`() {
    val error = OfflineException("No internet connection").toDomainError()

    assertEquals(DomainError.Offline("No internet connection"), error)
  }

  @Test
  fun `IOException maps to Server carrying the message`() {
    val error = IOException("connection refused").toDomainError()

    val server = assertIs<DomainError.Server>(error)
    assertEquals("connection refused", server.message)
  }

  @Test
  fun `SerializationException maps to Unknown carrying the message`() {
    val error = SerializationException("malformed json").toDomainError()

    val unknown = assertIs<DomainError.Unknown>(error)
    assertEquals("malformed json", unknown.message)
  }

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
      assertIs<DomainError.Server>(exceptionFor(HttpStatusCode.InternalServerError).toDomainError())
    }

  @Test
  fun `other 4xx ResponseException maps to Unknown`() =
    runTest {
      assertIs<DomainError.Unknown>(exceptionFor(HttpStatusCode.BadRequest).toDomainError())
    }

  @Test
  fun `generic Throwable maps to Unknown carrying the message`() {
    val unknown = assertIs<DomainError.Unknown>(RuntimeException("kaboom").toDomainError())

    assertEquals("kaboom", unknown.message)
  }

  private suspend fun exceptionFor(status: HttpStatusCode): Throwable {
    val client = HttpClient(MockEngine { respond(content = "{}", status = status) }) { expectSuccess = true }
    val captured = runCatching { client.get("http://test/x") }.exceptionOrNull()
      ?: error("Expected a thrown exception for status $status")
    client.close()
    return captured
  }
}
