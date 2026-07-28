package com.frame.zero.core.network

import com.frame.zero.core.session.LogoutSignal
import com.frame.zero.core.session.TokenStorage
import com.frame.zero.domain.DomainError
import com.frame.zero.domain.toDomainError
import com.russhwolf.settings.MapSettings
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Exercises the real production [clientConfig] over a [MockEngine] to prove the network
 * boundary — `expectSuccess`, the error-body validator, and `HttpRequestRetry` — compose
 * correctly, not just each piece in isolation (see `HttpClientStackTest` for the rest of
 * the plugin stack). In particular this guards the exact regression flagged in the error
 * handling audit: `HttpRequestRetry`'s `retryOnException` could turn every 4xx into 4
 * requests with multi-second backoff once `expectSuccess` started throwing.
 */
class ErrorMappingIntegrationTest {
  @Test
  fun `a JSON error body round-trips through the validator to the right DomainError`() =
    runTest {
      val env =
        TestEnv {
          respondJson(
            """{"error":"CONFLICT","message":"Phase already active"}""",
            HttpStatusCode.Conflict
          )
        }

      val thrown = runCatching { env.client.post("$BASE_URL/x") }.exceptionOrNull()

      assertEquals(DomainError.Conflict, thrown?.toDomainError())
    }

  @Test
  fun `a VALIDATION_ERROR body round-trips carrying its field map`() =
    runTest {
      val env =
        TestEnv {
          respondJson(
            """{"error":"VALIDATION_ERROR","message":"Validation failed","fields":{"email":"required"}}""",
            HttpStatusCode.BadRequest
          )
        }

      val thrown = runCatching { env.client.post("$BASE_URL/x") }.exceptionOrNull()

      assertEquals(DomainError.Validation(mapOf("email" to "required")), thrown?.toDomainError())
    }

  @Test
  fun `a non-JSON 500 body falls back to Server instead of Unknown`() =
    runTest {
      val env =
        TestEnv {
          respond(
            content = "<html>Internal Server Error</html>",
            status = HttpStatusCode.InternalServerError,
            headers = headersOf(HttpHeaders.ContentType, ContentType.Text.Html.toString())
          )
        }

      val thrown = runCatching { env.client.get("$BASE_URL/x") }.exceptionOrNull()

      assertEquals(DomainError.Server, thrown?.toDomainError())
    }

  @Test
  fun `a 409 on an unauthenticated path invokes the engine exactly once`() =
    runTest {
      val env =
        TestEnv {
          respondJson("""{"error":"EMAIL_ALREADY_EXISTS","message":"dup"}""", HttpStatusCode.Conflict)
        }

      runCatching { env.client.post("$BASE_URL/auth/register") }

      assertEquals(1, env.requests.size, "a 4xx must not be amplified by HttpRequestRetry")
    }

  @Test
  fun `a 401 on an unauthenticated path is not amplified by HttpRequestRetry's backoff`() =
    runTest {
      val env =
        TestEnv {
          respondJson("""{"error":"INVALID_CREDENTIALS","message":"bad password"}""", HttpStatusCode.Unauthorized)
        }

      runCatching { env.client.post("$BASE_URL/auth/login") }

      // Ktor's own Auth plugin always attempts one refresh-then-retry on a 401 challenge,
      // independent of expectSuccess/sendWithoutRequest — that's the "2", not a bug. What
      // this guards against is HttpRequestRetry's retryOnException piling exponential-backoff
      // attempts (4 total, ~14s) on top once the response validator starts throwing.
      assertEquals(2, env.requests.size, "a 401 must only pay Ktor's single built-in reauth retry")
    }

  @Test
  fun `a 4xx on a retry-eligible method is still not amplified`() =
    runTest {
      // GET is in `retryIf`'s safe-method set, so this is the case where a ResponseException
      // leaking into the retry loop would cost 4 requests and ~14s of exponential backoff.
      val env =
        TestEnv { respondJson("""{"error":"NOT_FOUND","message":"gone"}""", HttpStatusCode.NotFound) }

      val thrown = runCatching { env.client.get("$BASE_URL/productions/1") }.exceptionOrNull()

      assertEquals(1, env.requests.size, "a 4xx on GET must not be retried")
      assertEquals(DomainError.NotFound, thrown?.toDomainError())
    }

  // -- helpers ---------------------------------------------------------------

  private class TestEnv(
    handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData
  ) {
    val requests: MutableList<HttpRequestData> = mutableListOf()
    val client: HttpClient =
      HttpClient(
        MockEngine { request ->
          requests += request
          handler(request)
        }
      ) {
        clientConfig(
          config = NetworkConfig(baseUrl = BASE_URL, isDebug = false),
          tokenStorage = TokenStorage(MapSettings()),
          logoutSignal = LogoutSignal(),
          connectivityObserver = AlwaysOnlineConnectivityObserver,
          appLogger = NoopLogger,
          isDebug = false
        )()
      }
  }

  private object AlwaysOnlineConnectivityObserver : com.frame.zero.core.network.connectivity.ConnectivityObserver {
    private val state = MutableStateFlow(true)
    override val isOnline = state.asStateFlow()

    override fun isCurrentlyOnline(): Boolean = true

    override val isMetered = MutableStateFlow(false).asStateFlow()

    override fun isCurrentlyMetered(): Boolean = false
  }

  private object NoopLogger : com.frame.zero.core.logging.Logger {
    override fun v(
      tag: String,
      message: String,
      throwable: Throwable?
    ) = Unit

    override fun d(
      tag: String,
      message: String,
      throwable: Throwable?
    ) = Unit

    override fun i(
      tag: String,
      message: String,
      throwable: Throwable?
    ) = Unit

    override fun w(
      tag: String,
      message: String,
      throwable: Throwable?
    ) = Unit

    override fun e(
      tag: String,
      message: String,
      throwable: Throwable?
    ) = Unit
  }

  private companion object {
    const val BASE_URL = "http://test"

    fun MockRequestHandleScope.respondJson(
      body: String,
      status: HttpStatusCode
    ) = respond(
      content = body,
      status = status,
      headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
    )
  }
}
