package com.frame.zero.core.network

import com.frame.zero.auth.dto.RefreshRequest
import com.frame.zero.auth.dto.RefreshResponse
import com.frame.zero.core.session.LogoutSignal
import com.frame.zero.core.session.TokenStorage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.dsl.module

private val UNAUTHENTICATED_PATHS =
  setOf("/auth/login", "/auth/register", "/auth/refresh", "/auth/logout")

val networkModule: Module =
  module {
    single { NetworkConfig.localhost() }
    single { provideHttpClient(get(), get(), get()) }
  }

private fun provideHttpClient(
  config: NetworkConfig,
  tokenStorage: TokenStorage,
  logoutSignal: LogoutSignal
): HttpClient =
  httpClient {
    install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    install(Logging) { level = LogLevel.INFO }
    defaultRequest { contentType(ContentType.Application.Json) }
    install(Auth) {
      bearer {
        loadTokens {
          val access = tokenStorage.getAccessToken() ?: return@loadTokens null
          val refresh = tokenStorage.getRefreshToken() ?: return@loadTokens null
          BearerTokens(access, refresh)
        }
        refreshTokens {
          val refresh = tokenStorage.getRefreshToken()
          if (refresh == null) {
            handleRefreshFailure(tokenStorage, logoutSignal)
            return@refreshTokens null
          }
          val response =
            runCatching {
              client.post("${config.baseUrl}/auth/refresh") { setBody(RefreshRequest(refresh)) }
            }.getOrNull()
          if (response == null || !response.status.isSuccess()) {
            handleRefreshFailure(tokenStorage, logoutSignal)
            return@refreshTokens null
          }
          val body = runCatching { response.body<RefreshResponse>() }.getOrNull()
          if (body == null) {
            handleRefreshFailure(tokenStorage, logoutSignal)
            return@refreshTokens null
          }
          tokenStorage.saveTokens(body.accessToken, body.refreshToken)
          BearerTokens(body.accessToken, body.refreshToken)
        }
        sendWithoutRequest { request ->
          val path =
            "/" +
              request.url.pathSegments
                .filter { it.isNotEmpty() }
                .joinToString("/")
          path !in UNAUTHENTICATED_PATHS
        }
      }
    }
  }

private fun handleRefreshFailure(
  tokenStorage: TokenStorage,
  logoutSignal: LogoutSignal
) {
  tokenStorage.clearTokens()
  logoutSignal.emit()
}
