package com.frame.zero.core.network

import com.frame.zero.core.logging.Logger
import com.frame.zero.core.network.connectivity.ConnectivityObserver
import com.frame.zero.core.session.LogoutSignal
import com.frame.zero.core.session.TokenStorage
import com.russhwolf.settings.MapSettings
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Integration test for the real production HTTP client stack ([clientConfig]) —
 * bearer attach/skip, 401 -> refresh -> retry with token rotation, refresh-failure
 * logout, the offline [connectivityGuard], and the retry policy — exercised over a
 * [MockEngine] so the same plugin configuration the app ships is verified end-to-end.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HttpClientStackTest {
  @Test
  fun `attaches bearer token to protected requests but not to unauthenticated paths`() =
    runTest {
      val env = TestEnv { respondJson("{}", HttpStatusCode.OK) }
      env.storage.saveTokens(accessToken = "acc", refreshToken = "ref")

      env.client.get("$BASE_URL/productions")
      env.client.post("$BASE_URL/auth/login")

      val protected = env.requests.first { it.url.encodedPath == "/productions" }
      val login = env.requests.first { it.url.encodedPath == "/auth/login" }
      assertEquals("Bearer acc", protected.headers[HttpHeaders.Authorization])
      assertNull(login.headers[HttpHeaders.Authorization])
    }

  @Test
  fun `401 triggers refresh and retries the original request with rotated tokens`() =
    runTest {
      val env =
        TestEnv { request ->
          when {
            request.url.encodedPath == "/auth/refresh" ->
              respondJson(
                """{"accessToken":"new-acc","refreshToken":"new-ref"}""",
                HttpStatusCode.OK
              )

            request.headers[HttpHeaders.Authorization] == "Bearer old-acc" -> respond401Challenge()
            else -> respondJson("{}", HttpStatusCode.OK)
          }
        }
      env.storage.saveTokens(accessToken = "old-acc", refreshToken = "old-ref")

      val response: HttpResponse = env.client.get("$BASE_URL/productions")

      assertEquals(HttpStatusCode.OK, response.status)
      // Rotated tokens persisted.
      assertEquals("new-acc", env.storage.getAccessToken())
      assertEquals("new-ref", env.storage.getRefreshToken())
      // The original request was retried carrying the rotated access token.
      val retried = env.requests.last { it.url.encodedPath == "/productions" }
      assertEquals("Bearer new-acc", retried.headers[HttpHeaders.Authorization])
      // Refresh used the stored refresh token and skipped the bearer header itself.
      val refresh = env.requests.single { it.url.encodedPath == "/auth/refresh" }
      assertNull(refresh.headers[HttpHeaders.Authorization])
    }

  @Test
  fun `failed refresh clears tokens and emits a logout signal`() =
    runTest {
      val env =
        TestEnv { request ->
          when (request.url.encodedPath) {
            "/auth/refresh" -> respond("", HttpStatusCode.Unauthorized)
            else -> respond401Challenge()
          }
        }
      env.storage.saveTokens(accessToken = "old-acc", refreshToken = "old-ref")
      val logoutEvents = mutableListOf<Unit>()
      // Unconfined so the emission is delivered inline with emit() — which completes
      // before get() returns — instead of racing the virtual test clock across the
      // engine's background dispatcher.
      backgroundScope.launch(Dispatchers.Unconfined) {
        env.logoutSignal.events.collect { logoutEvents += Unit }
      }

      env.client.get("$BASE_URL/productions")
      advanceUntilIdle()

      assertFalse(env.storage.hasTokens())
      assertTrue(logoutEvents.isNotEmpty(), "expected a logout signal after refresh failure")
    }

  @Test
  fun `offline requests fail fast without reaching the engine`() =
    runTest {
      val env = TestEnv(online = false) { respondJson("{}", HttpStatusCode.OK) }

      assertFailsWith<IOException> { env.client.get("$BASE_URL/productions") }
      assertEquals(0, env.requests.size)
    }

  @Test
  fun `retries 5xx on GET but not on POST`() =
    runTest {
      val env = TestEnv { respond("", HttpStatusCode.InternalServerError) }

      env.client.get("$BASE_URL/productions")
      val getAttempts = env.requests.size
      env.requests.clear()
      env.client.post("$BASE_URL/productions")
      val postAttempts = env.requests.size

      assertEquals(MAX_RETRIES + 1, getAttempts, "GET should retry on 5xx")
      assertEquals(1, postAttempts, "POST should not be retried")
    }

  // -- helpers ---------------------------------------------------------------

  private class TestEnv(
    online: Boolean = true,
    handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData
  ) {
    val requests: MutableList<HttpRequestData> = mutableListOf()
    val storage: TokenStorage = TokenStorage(MapSettings())
    val logoutSignal: LogoutSignal = LogoutSignal()
    val client: HttpClient =
      HttpClient(
        MockEngine { request ->
          requests += request
          handler(request)
        }
      ) {
        clientConfig(
          config = NetworkConfig(baseUrl = BASE_URL, isDebug = false),
          tokenStorage = storage,
          logoutSignal = logoutSignal,
          connectivityObserver = FakeConnectivityObserver(online),
          appLogger = NoopLogger,
          isDebug = false
        )()
      }
  }

  private class FakeConnectivityObserver(
    private val online: Boolean
  ) : ConnectivityObserver {
    private val state = MutableStateFlow(online)
    override val isOnline = state.asStateFlow()

    override fun isCurrentlyOnline(): Boolean = online
  }

  private object NoopLogger : Logger {
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
    const val MAX_RETRIES = 3

    fun MockRequestHandleScope.respondJson(
      body: String,
      status: HttpStatusCode
    ) = respond(
      content = body,
      status = status,
      headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
    )

    fun MockRequestHandleScope.respond401Challenge() =
      respond(
        content = "",
        status = HttpStatusCode.Unauthorized,
        headers = headersOf(HttpHeaders.WWWAuthenticate, "Bearer")
      )
  }
}
