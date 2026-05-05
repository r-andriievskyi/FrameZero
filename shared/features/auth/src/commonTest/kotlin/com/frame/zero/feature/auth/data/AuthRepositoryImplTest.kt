package com.frame.zero.feature.auth.data

import com.frame.zero.auth.dto.UserDto
import com.frame.zero.core.network.NetworkConfig
import com.frame.zero.core.session.TokenStorage
import com.russhwolf.settings.MapSettings
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.ResponseException
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
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

class AuthRepositoryImplTest {
  // -- register --------------------------------------------------------------

  @Test
  fun `register success returns UserDto and persists tokens`() =
    runTest {
      val env =
        TestEnv { _ ->
          respondJson(
            body =
              """{"accessToken":"a-tok","refreshToken":"r-tok",""" +
                """"user":{"id":"u1","email":"new@x.com","firstName":"Jane","lastName":"Doe"}}""",
            status = HttpStatusCode.Created
          )
        }

      val dto =
        env.repository.register(
          email = "new@x.com",
          password = "secret",
          firstName = "Jane",
          lastName = "Doe"
        )

      assertEquals(
        UserDto(id = "u1", email = "new@x.com", firstName = "Jane", lastName = "Doe"),
        dto
      )
      assertEquals("a-tok", env.storage.getAccessToken())
      assertEquals("r-tok", env.storage.getRefreshToken())
    }

  @Test
  fun `register sends POST with serialized body to register endpoint`() =
    runTest {
      val env =
        TestEnv { _ ->
          respondJson(
            body =
              """{"accessToken":"a","refreshToken":"r",""" +
                """"user":{"id":"u1","email":"new@x.com","firstName":"Jane","lastName":"Doe"}}""",
            status = HttpStatusCode.Created
          )
        }

      env.repository.register(
        email = "new@x.com",
        password = "secret",
        firstName = "Jane",
        lastName = "Doe"
      )

      val request = env.requests.single()
      assertEquals(HttpMethod.Post, request.method)
      assertEquals("http://test/auth/register", request.url.toString())
      assertEquals(
        """{"email":"new@x.com","password":"secret","firstName":"Jane","lastName":"Doe"}""",
        request.body.bodyText()
      )
    }

  @Test
  fun `register 409 throws ResponseException and does not persist tokens`() =
    runTest {
      val env = TestEnv { _ -> respond(content = "conflict", status = HttpStatusCode.Conflict) }

      assertFailsWith<ResponseException> {
        env.repository.register(
          email = "dup@x.com",
          password = "secret",
          firstName = "",
          lastName = ""
        )
      }
      assertFalse(env.storage.hasTokens())
    }

  // -- login -----------------------------------------------------------------

  @Test
  fun `login success returns UserDto and persists tokens`() =
    runTest {
      val env =
        TestEnv { _ ->
          respondJson(
            body =
              """{"accessToken":"a-tok","refreshToken":"r-tok",""" +
                """"user":{"id":"u1","email":"u@x.com","firstName":"","lastName":""}}""",
            status = HttpStatusCode.OK
          )
        }

      val dto = env.repository.login(email = "u@x.com", password = "secret")

      assertEquals(UserDto(id = "u1", email = "u@x.com", firstName = "", lastName = ""), dto)
      assertEquals("a-tok", env.storage.getAccessToken())
      assertEquals("r-tok", env.storage.getRefreshToken())
    }

  @Test
  fun `login posts to login endpoint with credentials`() =
    runTest {
      val env =
        TestEnv { _ ->
          respondJson(
            body =
              """{"accessToken":"a","refreshToken":"r",""" +
                """"user":{"id":"u1","email":"u@x.com","firstName":"","lastName":""}}""",
            status = HttpStatusCode.OK
          )
        }

      env.repository.login(email = "u@x.com", password = "secret")

      val request = env.requests.single()
      assertEquals(HttpMethod.Post, request.method)
      assertEquals("http://test/auth/login", request.url.toString())
      assertEquals("""{"email":"u@x.com","password":"secret"}""", request.body.bodyText())
    }

  @Test
  fun `login 401 throws ResponseException and does not persist tokens`() =
    runTest {
      val env =
        TestEnv { _ ->
          respond(content = "unauthorized", status = HttpStatusCode.Unauthorized)
        }

      assertFailsWith<ResponseException> {
        env.repository.login(email = "u@x.com", password = "wrong")
      }
      assertFalse(env.storage.hasTokens())
    }

  @Test
  fun `login 500 throws ResponseException`() =
    runTest {
      val env =
        TestEnv { _ ->
          respond(content = "boom", status = HttpStatusCode.InternalServerError)
        }

      assertFailsWith<ResponseException> { env.repository.login(email = "u@x.com", password = "p") }
    }

  @Test
  fun `login network failure propagates IOException`() =
    runTest {
      val env = TestEnv { _ -> throw IOException("connection refused") }

      assertFailsWith<IOException> { env.repository.login(email = "u@x.com", password = "p") }
    }

  @Test
  fun `login malformed JSON propagates exception`() =
    runTest {
      val env =
        TestEnv { _ ->
          respondJson(body = """{"unexpected":"shape"}""", status = HttpStatusCode.OK)
        }

      assertFailsWith<Exception> { env.repository.login(email = "u@x.com", password = "p") }
    }

  // -- logout ----------------------------------------------------------------

  @Test
  fun `logout posts refresh token then clears storage`() =
    runTest {
      val env = TestEnv { _ -> respond(content = "", status = HttpStatusCode.OK) }
      env.storage.saveTokens(accessToken = "a", refreshToken = "r-existing")

      env.repository.logout()

      val request = env.requests.single()
      assertEquals(HttpMethod.Post, request.method)
      assertEquals("http://test/auth/logout", request.url.toString())
      assertEquals("""{"refreshToken":"r-existing"}""", request.body.bodyText())
      assertFalse(env.storage.hasTokens())
    }

  @Test
  fun `logout with no refresh token only clears storage`() =
    runTest {
      val env =
        TestEnv { _ ->
          error("logout endpoint should not be called when there is no refresh token")
        }

      env.repository.logout()

      assertEquals(0, env.requests.size)
      assertFalse(env.storage.hasTokens())
    }

  // -- getCurrentUser --------------------------------------------------------

  @Test
  fun `getCurrentUser returns UserDto on 200`() =
    runTest {
      val env =
        TestEnv { _ ->
          respondJson(
            body = """{"id":"u1","email":"u@x.com","firstName":"Jane","lastName":"Doe"}""",
            status = HttpStatusCode.OK
          )
        }

      val dto = env.repository.getCurrentUser()

      assertEquals(UserDto(id = "u1", email = "u@x.com", firstName = "Jane", lastName = "Doe"), dto)
    }

  @Test
  fun `getCurrentUser issues GET to auth me`() =
    runTest {
      val env =
        TestEnv { _ ->
          respondJson(
            body = """{"id":"u1","email":"u@x.com","firstName":"","lastName":""}""",
            status = HttpStatusCode.OK
          )
        }

      env.repository.getCurrentUser()

      val request = env.requests.single()
      assertEquals(HttpMethod.Get, request.method)
      assertEquals("http://test/auth/me", request.url.toString())
    }

  @Test
  fun `getCurrentUser 401 throws ResponseException`() =
    runTest {
      val env =
        TestEnv { _ ->
          respond(content = "unauthorized", status = HttpStatusCode.Unauthorized)
        }

      assertFailsWith<ResponseException> { env.repository.getCurrentUser() }
    }

  @Test
  fun `fetchCurrentUser delegates to getCurrentUser endpoint`() =
    runTest {
      val env =
        TestEnv { _ ->
          respondJson(
            body = """{"id":"u1","email":"u@x.com","firstName":"","lastName":""}""",
            status = HttpStatusCode.OK
          )
        }

      val dto = env.repository.fetchCurrentUser()

      assertEquals(UserDto(id = "u1", email = "u@x.com", firstName = "", lastName = ""), dto)
      assertEquals(
        "http://test/auth/me",
        env.requests
          .single()
          .url
          .toString()
      )
    }

  @Test
  fun `signOutRemote delegates to logout endpoint`() =
    runTest {
      val env = TestEnv { _ -> respond(content = "", status = HttpStatusCode.OK) }
      env.storage.saveTokens(accessToken = "a", refreshToken = "r")

      env.repository.signOutRemote()

      assertEquals(
        "http://test/auth/logout",
        env.requests
          .single()
          .url
          .toString()
      )
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
          networkConfig = NetworkConfig(baseUrl = "http://test")
        )
    }
  }

  private companion object {
    fun MockRequestHandleScope.respondJson(
      body: String,
      status: HttpStatusCode
    ): HttpResponseData =
      respond(
        content = body,
        status = status,
        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
      )

    fun OutgoingContent.bodyText(): String =
      when (this) {
        is TextContent -> text
        is OutgoingContent.ByteArrayContent -> bytes().decodeToString()
        else -> error("Unsupported request body type: ${this::class}")
      }
  }
}
