package com.frame.zero

import com.frame.zero.auth.JwtService
import com.frame.zero.auth.authModule
import com.frame.zero.auth.authRoutes
import com.frame.zero.config.AppConfig
import com.frame.zero.config.DatabaseFactory
import com.frame.zero.config.pingDatabase
import com.frame.zero.dashboard.dashboardModule
import com.frame.zero.dashboard.dashboardRoutes
import com.frame.zero.notification.notificationModule
import com.frame.zero.notification.notificationRoutes
import com.frame.zero.production.productionModule
import com.frame.zero.production.productionRoutes
import com.frame.zero.schedule.scheduleModule
import com.frame.zero.schedule.scheduleRoutes
import com.frame.zero.task.taskModule
import com.frame.zero.task.taskRoutes
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.engine.embeddedServer
import io.ktor.server.metrics.micrometer.MicrometerMetrics
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.callid.CallId
import io.ktor.server.plugins.callid.callIdMdc
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.forwardedheaders.XForwardedHeaders
import io.ktor.server.plugins.origin
import io.ktor.server.plugins.ratelimit.RateLimit
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import kotlinx.serialization.SerializationException
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.slf4j.event.Level
import java.util.UUID
import kotlin.time.Duration.Companion.minutes

private const val MAX_CALL_ID_LENGTH = 128
private const val AUTH_RATE_LIMIT = 10
val AUTH_RATE_LIMIT_NAME = RateLimitName("auth")

fun main() {
  val config = AppConfig.fromEnv()
  val metricsRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
  DatabaseFactory.init(config.database, metricsRegistry)
  embeddedServer(
    Netty,
    port = 8080,
    host = "0.0.0.0"
  ) {
    module(config, metricsRegistry)
  }.start(wait = true)
}

fun Application.module(
  config: AppConfig,
  metricsRegistry: PrometheusMeterRegistry
) {
  install(Koin) {
    slf4jLogger()
    modules(
      authModule(config),
      productionModule(),
      taskModule(),
      scheduleModule(),
      notificationModule(),
      dashboardModule()
    )
  }

  install(CallId) {
    header(HttpHeaders.XRequestId)
    generate { UUID.randomUUID().toString() }
    verify { it.isNotEmpty() && it.length <= MAX_CALL_ID_LENGTH }
  }

  // Trust X-Forwarded-* so request.origin.remoteHost is the real client IP rather
  // than the proxy/LB, which the per-client auth rate limit below keys on. Only
  // safe when the server sits behind a proxy that sets these headers; do not
  // expose this listener directly to untrusted clients.
  install(XForwardedHeaders)

  install(CallLogging) {
    level = Level.INFO
    callIdMdc("callId")
  }

  install(ContentNegotiation) { json() }

  installMetrics(metricsRegistry)

  installCors(config)

  install(RateLimit) {
    register(AUTH_RATE_LIMIT_NAME) {
      rateLimiter(limit = AUTH_RATE_LIMIT, refillPeriod = 1.minutes)
      // Key the limit per client so one caller can't exhaust the bucket for
      // everyone (the default key is global). Behind a proxy, install
      // XForwardedHeaders so origin.remoteHost reflects the real client IP.
      requestKey { call -> call.request.origin.remoteHost }
    }
  }

  install(Authentication) {
    jwt("auth-jwt") {
      val jwtService = JwtService(config.jwt)
      realm = config.jwt.realm
      verifier(jwtService.tokenVerifier)
      validate { credential ->
        if (credential.payload.subject.isNullOrBlank()) null else JWTPrincipal(credential.payload)
      }
    }
  }

  installStatusPages()

  routing {
    healthRoutes()
    authRoutes()
    dashboardRoutes()
    productionRoutes()
    taskRoutes()
    scheduleRoutes()
    notificationRoutes()
  }
}

private fun Application.installMetrics(metricsRegistry: PrometheusMeterRegistry) {
  install(MicrometerMetrics) {
    registry = metricsRegistry
  }
  routing {
    // Prometheus scrape endpoint. Pair with a request latency/throughput
    // dashboard; Hikari pool stats are bound in DatabaseFactory.
    get("/metrics") {
      call.respond(metricsRegistry.scrape())
    }
  }
}

private fun Route.healthRoutes() {
  get("/health") {
    call.respond(HttpStatusCode.OK, mapOf("status" to "ok"))
  }
  // only "ready" when the database is actually reachable, so
  // orchestrators stop routing traffic here if Postgres is down.
  get("/health/ready") {
    if (pingDatabase()) {
      call.respond(HttpStatusCode.OK, mapOf("status" to "ready"))
    } else {
      call.respond(HttpStatusCode.ServiceUnavailable, mapOf("status" to "unavailable"))
    }
  }
}

private fun Application.installStatusPages() {
  install(StatusPages) {
    exception<AppException> { call, cause ->
      call.application.environment.log
        .debug("AppException: {}", cause.error.humanMessage)
      call.respond(cause.error.status, cause.error.toResponse())
    }
    exception<SerializationException> { call, cause ->
      call.application.environment.log
        .debug("SerializationException: {}", cause.message)
      call.respond(
        HttpStatusCode.BadRequest,
        ErrorResponse(error = "VALIDATION_ERROR", message = "Malformed request body")
      )
    }
    exception<IllegalArgumentException> { call, cause ->
      call.application.environment.log
        .debug("IllegalArgumentException: {}", cause.message)
      call.respond(
        HttpStatusCode.BadRequest,
        ErrorResponse(error = "VALIDATION_ERROR", message = cause.message ?: "Invalid request")
      )
    }
    exception<Throwable> { call, cause ->
      call.application.environment.log
        .error("Unhandled exception", cause)
      call.respond(
        HttpStatusCode.InternalServerError,
        ErrorResponse(error = "INTERNAL", message = "Internal server error")
      )
    }
  }
}

private fun Application.installCors(config: AppConfig) {
  val appLog = environment.log
  install(CORS) {
    allowMethod(HttpMethod.Get)
    allowMethod(HttpMethod.Post)
    allowMethod(HttpMethod.Patch)
    allowMethod(HttpMethod.Delete)
    allowMethod(HttpMethod.Options)
    allowHeader(HttpHeaders.Authorization)
    allowHeader(HttpHeaders.ContentType)
    allowHeader(HttpHeaders.XRequestId)
    allowHeader("X-Timezone")
    when {
      config.corsAllowedOrigins.isNotEmpty() ->
        config.corsAllowedOrigins.forEach { origin ->
          val scheme = origin.substringBefore("://", missingDelimiterValue = "")
          val host = origin.substringAfter("://")
          if (scheme.isEmpty()) {
            allowHost(host, schemes = listOf("https"))
          } else {
            allowHost(host, schemes = listOf(scheme))
          }
        }
      config.isDevelopment -> anyHost()
      else ->
        // No origins configured outside dev: allow no cross-origin browser
        // callers. Requests without an Origin header (mobile clients, curl)
        // are unaffected by CORS.
        appLog.warn(
          "CORS_ALLOWED_ORIGINS is not set; all cross-origin browser requests will be rejected"
        )
    }
  }
}
