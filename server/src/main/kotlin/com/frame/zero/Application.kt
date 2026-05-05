package com.frame.zero

import com.frame.zero.auth.AuthException
import com.frame.zero.auth.JwtService
import com.frame.zero.auth.authModule
import com.frame.zero.auth.authRoutes
import com.frame.zero.config.AppConfig
import com.frame.zero.config.DatabaseFactory
import com.frame.zero.routes.dashboardRoutes
import com.frame.zero.routes.notificationRoutes
import com.frame.zero.routes.productionRoutes
import com.frame.zero.routes.scheduleRoutes
import com.frame.zero.routes.taskRoutes
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.callid.CallId
import io.ktor.server.plugins.callid.callIdMdc
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import kotlinx.serialization.SerializationException
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.slf4j.event.Level
import java.util.UUID

private const val MAX_CALL_ID_LENGTH = 128

fun main() {
  val config = AppConfig.fromEnv()
  DatabaseFactory.init(config.database)
  embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0") { module(config) }.start(wait = true)
}

fun Application.module(config: AppConfig) {
  install(Koin) {
    slf4jLogger()
    modules(authModule(config), appModule())
  }

  install(CallId) {
    header(HttpHeaders.XRequestId)
    generate { UUID.randomUUID().toString() }
    verify { it.isNotEmpty() && it.length <= MAX_CALL_ID_LENGTH }
  }

  install(CallLogging) {
    level = Level.INFO
    callIdMdc("callId")
  }

  install(ContentNegotiation) { json() }

  install(Authentication) {
    jwt("auth-jwt") {
      val jwtService = JwtService(config.jwt)
      realm = config.jwt.realm
      verifier(jwtService.verifier)
      validate { credential ->
        if (credential.payload.subject.isNullOrBlank()) null else JWTPrincipal(credential.payload)
      }
    }
  }

  install(StatusPages) {
    exception<AppException> { call, cause ->
      call.application.environment.log.debug("AppException: {}", cause.error.humanMessage)
      call.respond(cause.error.status, cause.error.toResponse())
    }
    exception<AuthException> { call, cause ->
      call.application.environment.log.debug("AuthException: {}", cause.error.message)
      call.respond(
        cause.error.status,
        ErrorResponse(error = "UNAUTHORIZED", message = cause.error.message),
      )
    }
    exception<SerializationException> { call, cause ->
      call.application.environment.log.debug("SerializationException: {}", cause.message)
      call.respond(
        HttpStatusCode.BadRequest,
        ErrorResponse(error = "VALIDATION_ERROR", message = "Malformed request body"),
      )
    }
    exception<IllegalArgumentException> { call, cause ->
      call.application.environment.log.debug("IllegalArgumentException: {}", cause.message)
      call.respond(
        HttpStatusCode.BadRequest,
        ErrorResponse(error = "VALIDATION_ERROR", message = cause.message ?: "Invalid request"),
      )
    }
    exception<Throwable> { call, cause ->
      call.application.environment.log.error("Unhandled exception", cause)
      call.respond(
        HttpStatusCode.InternalServerError,
        ErrorResponse(error = "INTERNAL", message = "Internal server error"),
      )
    }
  }

  routing {
    authRoutes()
    dashboardRoutes()
    productionRoutes()
    taskRoutes()
    scheduleRoutes()
    notificationRoutes()
  }
}
