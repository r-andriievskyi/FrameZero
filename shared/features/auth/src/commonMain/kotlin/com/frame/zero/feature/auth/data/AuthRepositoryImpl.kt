package com.frame.zero.feature.auth.data

import com.frame.zero.auth.dto.AuthResponse
import com.frame.zero.auth.dto.LoginRequest
import com.frame.zero.auth.dto.LogoutRequest
import com.frame.zero.auth.dto.RegisterRequest
import com.frame.zero.auth.dto.UserDto
import com.frame.zero.core.network.NetworkConfig
import com.frame.zero.core.session.SessionAuthOperations
import com.frame.zero.core.session.TokenStorage
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

  override suspend fun register(
    email: String,
    password: String,
    firstName: String,
    lastName: String,
  ): UserDto {
    val response: AuthResponse =
      httpClient
        .post("${networkConfig.baseUrl}/auth/register") {
          setBody(
            RegisterRequest(
              email = email,
              password = password,
              firstName = firstName,
              lastName = lastName,
            ))
        }
        .body()
    tokenStorage.saveTokens(response.accessToken, response.refreshToken)
    return response.user
  }

  override suspend fun login(email: String, password: String): UserDto {
    val response: AuthResponse =
      httpClient
        .post("${networkConfig.baseUrl}/auth/login") {
          setBody(LoginRequest(email = email, password = password))
        }
        .body()
    tokenStorage.saveTokens(response.accessToken, response.refreshToken)
    return response.user
  }

  override suspend fun logout() {
    val refresh = tokenStorage.getRefreshToken()
    if (refresh != null) {
      httpClient.post("${networkConfig.baseUrl}/auth/logout") {
        setBody(LogoutRequest(refreshToken = refresh))
      }
    }
    tokenStorage.clearTokens()
  }

  override suspend fun getCurrentUser(): UserDto =
    httpClient.get("${networkConfig.baseUrl}/auth/me").body()

  override suspend fun fetchCurrentUser(): UserDto = getCurrentUser()

  override suspend fun signOutRemote() = logout()
}
