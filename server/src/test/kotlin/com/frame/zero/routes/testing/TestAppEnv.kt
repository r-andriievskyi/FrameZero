package com.frame.zero.routes.testing

import com.frame.zero.AppException
import com.frame.zero.ErrorResponse
import com.frame.zero.auth.JwtService
import com.frame.zero.auth.testing.FakeUserRepository
import com.frame.zero.config.JwtConfig
import com.frame.zero.routes.dashboardRoutes
import com.frame.zero.routes.notificationRoutes
import com.frame.zero.routes.productionRoutes
import com.frame.zero.routes.scheduleRoutes
import com.frame.zero.routes.taskRoutes
import com.frame.zero.services.DashboardService
import com.frame.zero.services.NotificationService
import com.frame.zero.services.ProductionAccessService
import com.frame.zero.services.ProductionService
import com.frame.zero.services.ScheduleService
import com.frame.zero.services.TaskService
import com.frame.zero.toResponse
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import kotlinx.serialization.SerializationException
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import java.util.UUID
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

internal val testJwtConfig =
  JwtConfig(
    secret = "test-secret-must-be-long-enough-for-hmac256",
    issuer = "test-issuer",
    audience = "test-audience",
    realm = "test-realm",
    accessTokenTtl = 15.minutes,
    refreshTokenTtl = 30.days
  )

internal class TestAppEnv {
  val users = FakeUserRepository()
  val productions = FakeProductionRepository()
  val productionMembers = FakeProductionMemberRepository()
  val tasks = FakeTaskRepository()
  val scheduleEvents = FakeScheduleEventRepository()
  val notificationsRepo = FakeNotificationRepository()

  val jwtService = JwtService(testJwtConfig)
  val access = ProductionAccessService(productions, productionMembers)
  val productionService = ProductionService(productions, productionMembers, users, access)
  val dashboardService = DashboardService(users, productions, tasks)
  val taskService = TaskService(tasks, access)
  val scheduleService = ScheduleService(scheduleEvents, tasks, access)
  val notificationService = NotificationService(notificationsRepo)

  fun tokenFor(userId: UUID): String = jwtService.createAccessToken(userId, "test@test.com")

  fun configure(app: Application) {
    app.install(Koin) {
      modules(
        module {
          single { productionService }
          single { dashboardService }
          single { taskService }
          single { scheduleService }
          single { notificationService }
        }
      )
    }
    app.install(ContentNegotiation) { json() }
    app.install(Authentication) {
      jwt("auth-jwt") {
        realm = testJwtConfig.realm
        verifier(jwtService.tokenVerifier)
        validate { credential ->
          if (credential.payload.subject.isNullOrBlank()) null else JWTPrincipal(credential.payload)
        }
      }
    }
    app.install(StatusPages) {
      exception<AppException> { call, cause ->
        call.respond(cause.error.status, cause.error.toResponse())
      }
      exception<SerializationException> { call, _ ->
        call.respond(
          HttpStatusCode.BadRequest,
          ErrorResponse(error = "VALIDATION_ERROR", message = "Malformed request body")
        )
      }
    }
    app.routing {
      dashboardRoutes()
      productionRoutes()
      taskRoutes()
      scheduleRoutes()
      notificationRoutes()
    }
  }
}
