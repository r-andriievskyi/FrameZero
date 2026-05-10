package com.frame.zero.auth

import com.frame.zero.auth.dto.AuthResponse
import com.frame.zero.auth.dto.LoginRequest
import com.frame.zero.auth.dto.LogoutRequest
import com.frame.zero.auth.dto.RefreshRequest
import com.frame.zero.auth.dto.RefreshResponse
import com.frame.zero.auth.dto.RegisterRequest
import com.frame.zero.auth.dto.UserDto
import com.frame.zero.auth.testing.FakeRefreshTokenRepository
import com.frame.zero.auth.testing.FakeUserRepository
import com.frame.zero.config.JwtConfig
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.ratelimit.RateLimit
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.context.GlobalContext
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

class AuthRoutesTest {
  private val json = Json { ignoreUnknownKeys = true }

  private val jwtConfig =
    JwtConfig(
      secret = "test-secret-must-be-long-enough-for-hmac256",
      issuer = "test-issuer",
      audience = "test-audience",
      realm = "test-realm",
      accessTokenTtl = 15.minutes,
      refreshTokenTtl = 30.days
    )

  @AfterTest
  fun ensureKoinStopped() {
    if (GlobalContext.getOrNull() != null) stopKoin()
  }

  // -- register --------------------------------------------------------------

  @Test
  fun `POST auth register returns 201 with auth response`() =
    testApplication {
      val env = TestEnv()
      application { env.configure(this) }

      val response =
        client.post("/auth/register") {
          contentType(ContentType.Application.Json)
          setBody(
            json.encodeToString(
              RegisterRequest(
                email = "u@x.com",
                password = "password123",
                firstName = "Jane",
                lastName = "Doe"
              )
            )
          )
        }

      assertEquals(HttpStatusCode.Created, response.status)
      val body = json.decodeFromString<AuthResponse>(response.bodyAsText())
      assertEquals("u@x.com", body.user.email)
      assertTrue(body.accessToken.isNotBlank())
      assertTrue(body.refreshToken.isNotBlank())
    }

  @Test
  fun `POST auth register with malformed JSON returns 400`() =
    testApplication {
      val env = TestEnv()
      application { env.configure(this) }

      val response =
        client.post("/auth/register") {
          contentType(ContentType.Application.Json)
          setBody("{not valid json")
        }

      assertEquals(HttpStatusCode.BadRequest, response.status)
    }

  @Test
  fun `POST auth register with invalid email returns 400`() =
    testApplication {
      val env = TestEnv()
      application { env.configure(this) }

      val response =
        client.post("/auth/register") {
          contentType(ContentType.Application.Json)
          setBody(json.encodeToString(RegisterRequest("not-an-email", "password123", "", "")))
        }

      assertEquals(HttpStatusCode.BadRequest, response.status)
    }

  @Test
  fun `POST auth register with duplicate email returns 409`() =
    testApplication {
      val env = TestEnv()
      application { env.configure(this) }
      env.service.register("u@x.com", "password123", "", "")

      val response =
        client.post("/auth/register") {
          contentType(ContentType.Application.Json)
          setBody(json.encodeToString(RegisterRequest("u@x.com", "password456", "", "")))
        }

      assertEquals(HttpStatusCode.Conflict, response.status)
    }

  // -- login -----------------------------------------------------------------

  @Test
  fun `POST auth login with correct credentials returns 200`() =
    testApplication {
      val env = TestEnv()
      application { env.configure(this) }
      env.service.register("u@x.com", "password123", "", "")

      val response =
        client.post("/auth/login") {
          contentType(ContentType.Application.Json)
          setBody(json.encodeToString(LoginRequest("u@x.com", "password123")))
        }

      assertEquals(HttpStatusCode.OK, response.status)
      val body = json.decodeFromString<AuthResponse>(response.bodyAsText())
      assertEquals("u@x.com", body.user.email)
    }

  @Test
  fun `POST auth login with wrong password returns 401`() =
    testApplication {
      val env = TestEnv()
      application { env.configure(this) }
      env.service.register("u@x.com", "password123", "", "")

      val response =
        client.post("/auth/login") {
          contentType(ContentType.Application.Json)
          setBody(json.encodeToString(LoginRequest("u@x.com", "wrong-password")))
        }

      assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

  // -- refresh ---------------------------------------------------------------

  @Test
  fun `POST auth refresh with valid token returns a new pair`() =
    testApplication {
      val env = TestEnv()
      application { env.configure(this) }
      val auth = env.service.register("u@x.com", "password123", "", "")

      val response =
        client.post("/auth/refresh") {
          contentType(ContentType.Application.Json)
          setBody(json.encodeToString(RefreshRequest(auth.refreshToken)))
        }

      assertEquals(HttpStatusCode.OK, response.status)
      val body = json.decodeFromString<RefreshResponse>(response.bodyAsText())
      assertNotEquals(auth.refreshToken, body.refreshToken)
      assertTrue(body.accessToken.isNotBlank())
    }

  @Test
  fun `POST auth refresh with unknown token returns 401`() =
    testApplication {
      val env = TestEnv()
      application { env.configure(this) }

      val response =
        client.post("/auth/refresh") {
          contentType(ContentType.Application.Json)
          setBody(json.encodeToString(RefreshRequest("never-issued")))
        }

      assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

  // -- logout ----------------------------------------------------------------

  @Test
  fun `POST auth logout returns 204`() =
    testApplication {
      val env = TestEnv()
      application { env.configure(this) }
      val auth = env.service.register("u@x.com", "password123", "", "")

      val response =
        client.post("/auth/logout") {
          contentType(ContentType.Application.Json)
          setBody(json.encodeToString(LogoutRequest(auth.refreshToken)))
        }

      assertEquals(HttpStatusCode.NoContent, response.status)
    }

  // -- me --------------------------------------------------------------------

  @Test
  fun `GET auth me without bearer token returns 401`() =
    testApplication {
      val env = TestEnv()
      application { env.configure(this) }

      val response = client.get("/auth/me")

      assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

  @Test
  fun `GET auth me with valid bearer token returns the user`() =
    testApplication {
      val env = TestEnv()
      application { env.configure(this) }
      val auth = env.service.register("u@x.com", "password123", "", "")

      val response =
        client.get("/auth/me") { header(HttpHeaders.Authorization, "Bearer ${auth.accessToken}") }

      assertEquals(HttpStatusCode.OK, response.status)
      val body = json.decodeFromString<UserDto>(response.bodyAsText())
      assertEquals("u@x.com", body.email)
    }

  @Test
  fun `GET auth me with malformed bearer token returns 401`() =
    testApplication {
      val env = TestEnv()
      application { env.configure(this) }

      val response =
        client.get(
          "/auth/me"
        ) { header(HttpHeaders.Authorization, "Bearer not-a-jwt") }

      assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

  // -- StatusPages mappings --------------------------------------------------

  @Test
  fun `body containing only an opening brace yields a 400 with malformed body error`() =
    testApplication {
      val env = TestEnv()
      application { env.configure(this) }

      val response =
        client.post("/auth/register") {
          contentType(ContentType.Application.Json)
          setBody("{")
        }

      assertEquals(HttpStatusCode.BadRequest, response.status)
    }

  // -- helpers ---------------------------------------------------------------

  private inner class TestEnv {
    val users = FakeUserRepository()
    val tokens = FakeRefreshTokenRepository()
    val jwtService = JwtService(jwtConfig)
    val service =
      AuthService(
        users = users,
        refreshTokens = tokens,
        passwordHasher = PasswordHasher(),
        tokenHasher = TokenHasher(),
        jwtService = jwtService,
        jwtConfig = jwtConfig
      )

    fun configure(app: Application) {
      app.install(Koin) { modules(module { single { service } }) }
      app.install(ContentNegotiation) { json() }
      app.install(RateLimit) {
        register(com.frame.zero.AUTH_RATE_LIMIT_NAME) {
          rateLimiter(limit = 100, refillPeriod = 1.minutes)
        }
      }
      app.install(Authentication) {
        jwt("auth-jwt") {
          realm = jwtConfig.realm
          verifier(jwtService.tokenVerifier)
          validate { credential ->
            if (credential.payload.subject.isNullOrBlank()) {
              null
            } else {
              JWTPrincipal(credential.payload)
            }
          }
        }
      }
      app.install(StatusPages) {
        exception<AuthException> { call, cause ->
          call.respond(cause.error.status, mapOf("error" to cause.error.message))
        }
        exception<SerializationException> { call, _ ->
          call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Malformed request body"))
        }
        exception<IllegalArgumentException> { call, cause ->
          call.respond(
            HttpStatusCode.BadRequest,
            mapOf("error" to (cause.message ?: "Invalid request"))
          )
        }
      }
      app.routing { authRoutes() }
    }
  }
}
