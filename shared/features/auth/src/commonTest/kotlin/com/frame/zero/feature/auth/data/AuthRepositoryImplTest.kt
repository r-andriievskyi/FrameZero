package com.frame.zero.feature.auth.data

import com.frame.zero.core.network.NetworkConfig
import com.frame.zero.core.session.TokenStorage
import com.frame.zero.domain.DomainError
import com.frame.zero.domain.Outcome
import com.frame.zero.domain.User
import com.russhwolf.settings.MapSettings
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.content.TextContent
import io.ktor.http.contentType
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlinx.serialization.json.Json

class AuthRepositoryImplTest {

  // -- register --------------------------------------------------------------

  @Test
  fun `register success returns User and persists tokens`() = runTest {
    val env = TestEnv { _ ->
      respondJson(
        body =
          """{"accessToken":"a-tok","refreshToken":"r-tok","user":{"id":"u1","email":"new@x.com"}}""",
        status = HttpStatusCode.Created,
      )
    }

    val outcome = env.repository.register(email = "new@x.com", password = "secret")

    val success = assertIs<Outcome.Success<User>>(outcome)
    assertEquals(User(id = "u1", email = "new@x.com"), success.data)
    assertEquals("a-tok", env.storage.getAccessToken())
    assertEquals("r-tok", env.storage.getRefreshToken())
  }

  @Test
  fun `register sends POST with serialized body to register endpoint`() = runTest {
    val env = TestEnv { _ ->
      respondJson(
        body = """{"accessToken":"a","refreshToken":"r","user":{"id":"u1","email":"new@x.com"}}""",
        status = HttpStatusCode.Created,
      )
    }

    env.repository.register(email = "new@x.com", password = "secret")

    val request = env.requests.single()
    assertEquals(HttpMethod.Post, request.method)
    assertEquals("http://test/auth/register", request.url.toString())
    assertEquals("""{"email":"new@x.com","password":"secret"}""", request.body.bodyText())
  }

  @Test
  fun `register 409 returns EmailAlreadyExists and does not persist tokens`() = runTest {
    val env = TestEnv { _ -> respond(content = "conflict", status = HttpStatusCode.Conflict) }

    val outcome = env.repository.register(email = "dup@x.com", password = "secret")

    val failure = assertIs<Outcome.Failure>(outcome)
    assertEquals(DomainError.EmailAlreadyExists, failure.error)
    assertFalse(env.storage.hasTokens())
  }

  // -- login -----------------------------------------------------------------

  @Test
  fun `login success returns User and persists tokens`() = runTest {
    val env = TestEnv { _ ->
      respondJson(
        body =
          """{"accessToken":"a-tok","refreshToken":"r-tok","user":{"id":"u1","email":"u@x.com"}}""",
        status = HttpStatusCode.OK,
      )
    }

    val outcome = env.repository.login(email = "u@x.com", password = "secret")

    val success = assertIs<Outcome.Success<User>>(outcome)
    assertEquals(User(id = "u1", email = "u@x.com"), success.data)
    assertEquals("a-tok", env.storage.getAccessToken())
    assertEquals("r-tok", env.storage.getRefreshToken())
  }

  @Test
  fun `login posts to login endpoint with credentials`() = runTest {
    val env = TestEnv { _ ->
      respondJson(
        body = """{"accessToken":"a","refreshToken":"r","user":{"id":"u1","email":"u@x.com"}}""",
        status = HttpStatusCode.OK,
      )
    }

    env.repository.login(email = "u@x.com", password = "secret")

    val request = env.requests.single()
    assertEquals(HttpMethod.Post, request.method)
    assertEquals("http://test/auth/login", request.url.toString())
    assertEquals("""{"email":"u@x.com","password":"secret"}""", request.body.bodyText())
  }

  @Test
  fun `login 401 returns InvalidCredentials and does not persist tokens`() = runTest {
    val env = TestEnv { _ ->
      respond(content = "unauthorized", status = HttpStatusCode.Unauthorized)
    }

    val outcome = env.repository.login(email = "u@x.com", password = "wrong")

    val failure = assertIs<Outcome.Failure>(outcome)
    assertEquals(DomainError.InvalidCredentials, failure.error)
    assertNull(env.storage.getAccessToken())
    assertNull(env.storage.getRefreshToken())
  }

  @Test
  fun `login 500 returns Unknown`() = runTest {
    val env = TestEnv { _ ->
      respond(content = "boom", status = HttpStatusCode.InternalServerError)
    }

    val outcome = env.repository.login(email = "u@x.com", password = "p")

    val failure = assertIs<Outcome.Failure>(outcome)
    assertIs<DomainError.Unknown>(failure.error)
  }

  @Test
  fun `login network failure returns Network error`() = runTest {
    val env = TestEnv { _ -> throw IOException("connection refused") }

    val outcome = env.repository.login(email = "u@x.com", password = "p")

    val failure = assertIs<Outcome.Failure>(outcome)
    assertEquals(DomainError.Network("connection refused"), failure.error)
  }

  @Test
  fun `login malformed JSON returns Unknown`() = runTest {
    val env = TestEnv { _ ->
      respondJson(body = """{"unexpected":"shape"}""", status = HttpStatusCode.OK)
    }

    val outcome = env.repository.login(email = "u@x.com", password = "p")

    val failure = assertIs<Outcome.Failure>(outcome)
    assertIs<DomainError.Unknown>(failure.error)
  }

  // -- logout ----------------------------------------------------------------

  @Test
  fun `logout posts refresh token then clears storage`() = runTest {
    val env = TestEnv { _ -> respond(content = "", status = HttpStatusCode.OK) }
    env.storage.saveTokens(accessToken = "a", refreshToken = "r-existing")

    val outcome = env.repository.logout()

    assertIs<Outcome.Success<Unit>>(outcome)
    val request = env.requests.single()
    assertEquals(HttpMethod.Post, request.method)
    assertEquals("http://test/auth/logout", request.url.toString())
    assertEquals("""{"refreshToken":"r-existing"}""", request.body.bodyText())
    assertFalse(env.storage.hasTokens())
  }

  @Test
  fun `logout with no refresh token only clears storage`() = runTest {
    val env = TestEnv { _ ->
      error("logout endpoint should not be called when there is no refresh token")
    }

    val outcome = env.repository.logout()

    assertIs<Outcome.Success<Unit>>(outcome)
    assertEquals(0, env.requests.size)
    assertFalse(env.storage.hasTokens())
  }

  // -- getCurrentUser --------------------------------------------------------

  @Test
  fun `getCurrentUser returns User on 200`() = runTest {
    val env = TestEnv { _ ->
      respondJson(body = """{"id":"u1","email":"u@x.com"}""", status = HttpStatusCode.OK)
    }

    val outcome = env.repository.getCurrentUser()

    val success = assertIs<Outcome.Success<User>>(outcome)
    assertEquals(User(id = "u1", email = "u@x.com"), success.data)
  }

  @Test
  fun `getCurrentUser issues GET to auth me`() = runTest {
    val env = TestEnv { _ ->
      respondJson(body = """{"id":"u1","email":"u@x.com"}""", status = HttpStatusCode.OK)
    }

    env.repository.getCurrentUser()

    val request = env.requests.single()
    assertEquals(HttpMethod.Get, request.method)
    assertEquals("http://test/auth/me", request.url.toString())
  }

  @Test
  fun `getCurrentUser 401 returns InvalidCredentials`() = runTest {
    val env = TestEnv { _ ->
      respond(content = "unauthorized", status = HttpStatusCode.Unauthorized)
    }

    val outcome = env.repository.getCurrentUser()

    val failure = assertIs<Outcome.Failure>(outcome)
    assertEquals(DomainError.InvalidCredentials, failure.error)
  }

  @Test
  fun `fetchCurrentUser delegates to getCurrentUser endpoint`() = runTest {
    val env = TestEnv { _ ->
      respondJson(body = """{"id":"u1","email":"u@x.com"}""", status = HttpStatusCode.OK)
    }

    val outcome = env.repository.fetchCurrentUser()

    val success = assertIs<Outcome.Success<User>>(outcome)
    assertEquals(User(id = "u1", email = "u@x.com"), success.data)
    assertEquals("http://test/auth/me", env.requests.single().url.toString())
  }

  @Test
  fun `signOutRemote delegates to logout endpoint`() = runTest {
    val env = TestEnv { _ -> respond(content = "", status = HttpStatusCode.OK) }
    env.storage.saveTokens(accessToken = "a", refreshToken = "r")

    val outcome = env.repository.signOutRemote()

    assertIs<Outcome.Success<Unit>>(outcome)
    assertEquals("http://test/auth/logout", env.requests.single().url.toString())
    assertFalse(env.storage.hasTokens())
  }

  // -- helpers ---------------------------------------------------------------

  private class TestEnv(
    handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData
  ) {
    val requests: MutableList<HttpRequestData> = mutableListOf()
    val storage: TokenStorage = TokenStorage(MapSettings())
    val repository: AuthRepositoryImpl

    init {
      val client =
        HttpClient(
          MockEngine { request ->
            requests += request
            handler(request)
          }
        ) {
          install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
          defaultRequest { contentType(ContentType.Application.Json) }
          expectSuccess = true
        }
      repository =
        AuthRepositoryImpl(
          httpClient = client,
          tokenStorage = storage,
          networkConfig = NetworkConfig(baseUrl = "http://test"),
        )
    }
  }

  private companion object {
    fun MockRequestHandleScope.respondJson(body: String, status: HttpStatusCode): HttpResponseData =
      respond(
        content = body,
        status = status,
        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
      )

    fun OutgoingContent.bodyText(): String =
      when (this) {
        is TextContent -> text
        is OutgoingContent.ByteArrayContent -> bytes().decodeToString()
        else -> error("Unsupported request body type: ${this::class}")
      }
  }
}
