package com.frame.zero.feature.auth.data

import com.frame.zero.domain.OfflineException
import com.frame.zero.domain.DomainError
import com.frame.zero.feature.auth.domain.toDomainError
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

class AuthErrorMapperTest {
  @Test
  fun `401 ResponseException maps to InvalidCredentials`() =
    runTest {
      val error = exceptionFor(HttpStatusCode.Unauthorized).toDomainError()

      assertEquals(DomainError.InvalidCredentials, error)
    }

  @Test
  fun `409 ResponseException maps to EmailAlreadyExists`() =
    runTest {
      val error = exceptionFor(HttpStatusCode.Conflict).toDomainError()

      assertEquals(DomainError.EmailAlreadyExists, error)
    }

  @Test
  fun `500 ResponseException maps to Unknown`() =
    runTest {
      val error = exceptionFor(HttpStatusCode.InternalServerError).toDomainError()

      assertIs<DomainError.Unknown>(error)
    }

  @Test
  fun `400 ResponseException maps to Unknown`() =
    runTest {
      val error = exceptionFor(HttpStatusCode.BadRequest).toDomainError()

      assertIs<DomainError.Unknown>(error)
    }

  @Test
  fun `OfflineException maps to Network with the original message`() {
    val error = OfflineException("No internet connection").toDomainError()

    assertEquals(DomainError.Offline("No internet connection"), error)
  }

  @Test
  fun `IOException maps to Server server unreachable while online`() {
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
  fun `generic Throwable maps to Unknown carrying the message`() {
    val error = RuntimeException("kaboom").toDomainError()

    val unknown = assertIs<DomainError.Unknown>(error)
    assertEquals("kaboom", unknown.message)
  }

  private suspend fun exceptionFor(status: HttpStatusCode): Throwable {
    val client =
      HttpClient(MockEngine { respond(content = "{}", status = status) }) { expectSuccess = true }
    val captured =
      runCatching { client.get("http://test/x") }.exceptionOrNull()
        ?: error("Expected a thrown exception for status $status")
    client.close()
    return captured
  }
}
