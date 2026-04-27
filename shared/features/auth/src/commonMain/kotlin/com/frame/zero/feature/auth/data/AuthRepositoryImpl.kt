package com.frame.zero.feature.auth.data

import com.frame.zero.auth.dto.AuthResponse
import com.frame.zero.auth.dto.LoginRequest
import com.frame.zero.auth.dto.LogoutRequest
import com.frame.zero.auth.dto.RegisterRequest
import com.frame.zero.auth.dto.UserDto
import com.frame.zero.core.network.NetworkConfig
import com.frame.zero.core.session.SessionAuthOperations
import com.frame.zero.core.session.TokenStorage
import com.frame.zero.domain.Outcome
import com.frame.zero.domain.User
import com.frame.zero.repository.auth.AuthRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody

class AuthRepositoryImpl(
  private val httpClient: HttpClient,
  private val tokenStorage: TokenStorage,
  private val networkConfig: NetworkConfig,
) : AuthRepository, SessionAuthOperations {

  override suspend fun register(email: String, password: String): Outcome<User> = runOutcome {
    val response: AuthResponse =
      httpClient
        .post("${networkConfig.baseUrl}/auth/register") {
          setBody(RegisterRequest(email = email, password = password))
        }
        .body()
    tokenStorage.saveTokens(response.accessToken, response.refreshToken)
    response.user.toDomain()
  }

  override suspend fun login(email: String, password: String): Outcome<User> = runOutcome {
    val response: AuthResponse =
      httpClient
        .post("${networkConfig.baseUrl}/auth/login") {
          setBody(LoginRequest(email = email, password = password))
        }
        .body()
    tokenStorage.saveTokens(response.accessToken, response.refreshToken)
    response.user.toDomain()
  }

  override suspend fun logout(): Outcome<Unit> = runOutcome {
    val refresh = tokenStorage.getRefreshToken()
    if (refresh != null) {
      httpClient.post("${networkConfig.baseUrl}/auth/logout") {
        setBody(LogoutRequest(refreshToken = refresh))
      }
    }
    tokenStorage.clearTokens()
  }

  override suspend fun getCurrentUser(): Outcome<User> = runOutcome {
    val dto: UserDto = httpClient.get("${networkConfig.baseUrl}/auth/me").body()
    dto.toDomain()
  }

  override suspend fun fetchCurrentUser(): Outcome<User> = getCurrentUser()

  override suspend fun signOutRemote(): Outcome<Unit> = logout()

  private inline fun <T> runOutcome(block: () -> T): Outcome<T> =
    runCatching(block)
      .fold(
        onSuccess = { Outcome.Success(it) },
        onFailure = { Outcome.Failure(it.toDomainError()) },
      )

  private fun UserDto.toDomain(): User = User(id = id, email = email)
}
