package com.frame.zero.feature.auth.data

import com.frame.zero.auth.dto.AuthResponse
import com.frame.zero.auth.dto.LoginRequest
import com.frame.zero.auth.dto.LogoutRequest
import com.frame.zero.auth.dto.RegisterRequest
import com.frame.zero.auth.dto.UserDto
import com.frame.zero.core.network.NetworkConfig
import com.frame.zero.core.session.SessionAuthOperations
import com.frame.zero.core.session.TokenStorage
import com.frame.zero.domain.User
import com.frame.zero.domain.toDomain
import com.frame.zero.repository.auth.AuthRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.coroutines.CancellationException

class AuthRepositoryImpl(
  private val httpClient: HttpClient,
  private val tokenStorage: TokenStorage,
  private val networkConfig: NetworkConfig
) : AuthRepository,
  SessionAuthOperations {
  override suspend fun register(
    email: String,
    password: String,
    firstName: String,
    lastName: String
  ): User {
    val response: AuthResponse =
      httpClient
        .post("${networkConfig.baseUrl}/auth/register") {
          setBody(
            RegisterRequest(
              email = email,
              password = password,
              firstName = firstName,
              lastName = lastName
            )
          )
        }.body()
    tokenStorage.saveTokens(response.accessToken, response.refreshToken)
    return response.user.toDomain()
  }

  override suspend fun login(
    email: String,
    password: String
  ): User {
    val response: AuthResponse =
      httpClient
        .post("${networkConfig.baseUrl}/auth/login") {
          setBody(LoginRequest(email = email, password = password))
        }.body()
    tokenStorage.saveTokens(response.accessToken, response.refreshToken)
    return response.user.toDomain()
  }

  override suspend fun logout() {
    val refresh = tokenStorage.getRefreshToken()
    if (refresh != null) {
      // Best-effort: the server-side session must be revoked if reachable, but a 401/5xx/offline
      // failure here must never block clearing local tokens — the caller (SessionManager.logout)
      // is unwinding the local session regardless of whether the remote call succeeds.
      try {
        httpClient.post("${networkConfig.baseUrl}/auth/logout") {
          setBody(LogoutRequest(refreshToken = refresh))
        }
      } catch (cancellation: CancellationException) {
        throw cancellation
      } catch (_: Exception) {
        // Swallowed intentionally — see comment above.
      }
    }
    tokenStorage.clearTokens()
  }

  override suspend fun getCurrentUser(): User =
    httpClient
      .get("${networkConfig.baseUrl}/auth/me")
      .body<UserDto>()
      .toDomain()

  override suspend fun fetchCurrentUser(): User = getCurrentUser()

  override suspend fun signOutRemote() = logout()
}
