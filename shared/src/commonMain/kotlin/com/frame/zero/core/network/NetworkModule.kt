package com.frame.zero.core.network

import com.frame.zero.auth.dto.RefreshRequest
import com.frame.zero.auth.dto.RefreshResponse
import com.frame.zero.core.logging.Logger as AppLogger
import com.frame.zero.core.network.connectivity.ConnectivityObserver
import com.frame.zero.domain.OfflineException
import com.frame.zero.domain.ServerErrorException
import com.frame.zero.core.session.LogoutSignal
import com.frame.zero.core.session.TokenStorage
import com.frame.zero.dto.common.ErrorResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.accept
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.dsl.module

private val UNAUTHENTICATED_PATHS = setOf("/auth/login", "/auth/register", "/auth/refresh", "/auth/logout")

internal fun connectivityGuard(connectivityObserver: ConnectivityObserver) =
  createClientPlugin("ConnectivityGuard") {
    onRequest { _, _ ->
      if (!connectivityObserver.isCurrentlyOnline()) {
        throw OfflineException()
      }
    }
  }

val networkModule: Module = module {
  single { NetworkConfig.fromBuildConfig() }
  single { provideHttpClient(get(), get(), get(), get(), get(), isDebug = BuildKonfig.DEBUG) }
  single { ChatSocketClient(httpClient = get(), networkConfig = get(), logger = get()) }
}

private fun provideHttpClient(
  config: NetworkConfig,
  tokenStorage: TokenStorage,
  logoutSignal: LogoutSignal,
  connectivityObserver: ConnectivityObserver,
  appLogger: AppLogger,
  isDebug: Boolean
): HttpClient =
  httpClient(
    clientConfig(config, tokenStorage, logoutSignal, connectivityObserver, appLogger, isDebug)
  )

/**
 * The full production HttpClient plugin stack, extracted so tests can apply the exact
 * same configuration to a `MockEngine`-backed client (see `HttpClientStackTest`).
 * [provideHttpClient] applies it over the real platform engine.
 */
internal fun clientConfig(
  config: NetworkConfig,
  tokenStorage: TokenStorage,
  logoutSignal: LogoutSignal,
  connectivityObserver: ConnectivityObserver,
  appLogger: AppLogger,
  isDebug: Boolean
): HttpClientConfig<*>.() -> Unit =
  {
    expectSuccess = true

    install(connectivityGuard(connectivityObserver))
    install(WebSockets)

    defaultRequest {
      contentType(ContentType.Application.Json)
      accept(ContentType.Application.Json)
    }

    HttpResponseValidator {
      handleResponseExceptionWithRequest { exception, request ->
        val responseException = exception as? ResponseException ?: return@handleResponseExceptionWithRequest
        val status = responseException.response.status
        appLogger.e(
          tag = "Network",
          message = "Server error [$status] ${request.method.value} ${request.url.encodedPath}",
          // Only 5xx are app-actionable non-fatals; routine 4xx aren't bugs.
          throwable = if (status.value >= 500) responseException else null
        )
        // Never log the raw body — it may carry PII/credentials (see the leak this replaces).
        val errorBody =
          try {
            responseException.response.body<ErrorResponseDto>()
          } catch (cancellation: CancellationException) {
            throw cancellation
          } catch (_: Exception) {
            // Not an ErrorResponse (proxy HTML, empty body, …) — fall back to status mapping.
            null
          }
        if (errorBody != null) {
          throw ServerErrorException(code = errorBody.error, status = status.value, fields = errorBody.fields)
        }
      }
    }

    install(ContentNegotiation) {
      json(
        Json {
          ignoreUnknownKeys = true
          isLenient = true
        }
      )
    }
    install(Logging) {
      logger = Logger.DEFAULT
      level = if (isDebug) LogLevel.ALL else LogLevel.NONE
    }
    install(HttpRequestRetry) {
      maxRetries = 3
      retryOnExceptionIf(maxRetries = 3) { _, cause ->
        cause !is ResponseException && cause !is ServerErrorException
      }
      retryIf { request, response ->
        // Only retry GET, HEAD, PUT, DELETE - not POST or PATCH
        val safeMethod = request.method in listOf(HttpMethod.Get, HttpMethod.Head, HttpMethod.Put, HttpMethod.Delete)
        safeMethod && (response.status.value >= 500)
      }
      exponentialDelay( // wait 2s, 4s, 8s between attempts
        base = 2.0,
        maxDelayMs = 10_000
      )
    }
    install(Auth) {
      bearer {
        loadTokens {
          val access = tokenStorage.getAccessToken() ?: return@loadTokens null
          val refresh = tokenStorage.getRefreshToken() ?: return@loadTokens null
          BearerTokens(accessToken = access, refreshToken = refresh)
        }
        refreshTokens {
          val refresh = tokenStorage.getRefreshToken()
          if (refresh == null) {
            handleRefreshFailure(tokenStorage, logoutSignal)
            return@refreshTokens null
          }
          val response = runCatching {
            client.post("${config.baseUrl}/auth/refresh") { setBody(RefreshRequest(refresh)) }
          }.getOrNull()
          if (response == null || !response.status.isSuccess()) {
            handleRefreshFailure(tokenStorage, logoutSignal)
            return@refreshTokens null
          }
          val refreshBody = runCatching { response.body<RefreshResponse>() }.getOrNull()
          if (refreshBody == null) {
            handleRefreshFailure(tokenStorage, logoutSignal)
            return@refreshTokens null
          }
          tokenStorage.saveTokens(refreshBody.accessToken, refreshBody.refreshToken)
          BearerTokens(refreshBody.accessToken, refreshBody.refreshToken)
        }
        sendWithoutRequest { request ->
          val path = request.url.pathSegments.filter { it.isNotEmpty() }.joinToString("/", prefix = "/")
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
