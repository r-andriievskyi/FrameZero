package com.frame.zero.auth.dto

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AuthDtoSerializationTest {

  private val json = Json { ignoreUnknownKeys = true }

  @Test
  fun `RegisterRequest round-trips`() {
    val original =
      RegisterRequest(
        email = "user@example.com",
        password = "hunter2",
        firstName = "Jane",
        lastName = "Doe",
      )

    val decoded = json.decodeFromString<RegisterRequest>(json.encodeToString(original))

    assertEquals(original, decoded)
  }

  @Test
  fun `LoginRequest round-trips`() {
    val original = LoginRequest(email = "user@example.com", password = "hunter2")

    val decoded = json.decodeFromString<LoginRequest>(json.encodeToString(original))

    assertEquals(original, decoded)
  }

  @Test
  fun `RefreshRequest round-trips`() {
    val original = RefreshRequest(refreshToken = "r-token-1")

    val decoded = json.decodeFromString<RefreshRequest>(json.encodeToString(original))

    assertEquals(original, decoded)
  }

  @Test
  fun `LogoutRequest round-trips`() {
    val original = LogoutRequest(refreshToken = "r-token-1")

    val decoded = json.decodeFromString<LogoutRequest>(json.encodeToString(original))

    assertEquals(original, decoded)
  }

  @Test
  fun `UserDto round-trips`() {
    val original = UserDto(
      id = "u1", email = "user@example.com", firstName = "Jane", lastName = "Doe"
    )

    val decoded = json.decodeFromString<UserDto>(json.encodeToString(original))

    assertEquals(original, decoded)
  }

  @Test
  fun `AuthResponse round-trips`() {
    val original =
      AuthResponse(
        accessToken = "a-token",
        refreshToken = "r-token",
        user = UserDto(id = "u1", email = "user@example.com", firstName = "Jane", lastName = "Doe"),
      )

    val decoded = json.decodeFromString<AuthResponse>(json.encodeToString(original))

    assertEquals(original, decoded)
  }

  @Test
  fun `RefreshResponse round-trips`() {
    val original = RefreshResponse(accessToken = "a-new", refreshToken = "r-new")

    val decoded = json.decodeFromString<RefreshResponse>(json.encodeToString(original))

    assertEquals(original, decoded)
  }

  @Test
  fun `AuthResponse decodes wire-compatible field names from server`() {
    val raw =
      """{"accessToken":"a","refreshToken":"r","user":{"id":"u1","email":"u@x.com","firstName":"Jane","lastName":"Doe"}}"""

    val decoded = json.decodeFromString<AuthResponse>(raw)

    assertEquals(
      AuthResponse(
        accessToken = "a",
        refreshToken = "r",
        user = UserDto(id = "u1", email = "u@x.com", firstName = "Jane", lastName = "Doe"),
      ),
      decoded,
    )
  }

  @Test
  fun `AuthResponse decoder ignores unknown fields`() {
    val raw =
      """{"accessToken":"a","refreshToken":"r","user":{"id":"u1","email":"u@x.com","firstName":"","lastName":""},"extra":"ignored"}"""

    val decoded = json.decodeFromString<AuthResponse>(raw)

    assertEquals("a", decoded.accessToken)
  }
}
