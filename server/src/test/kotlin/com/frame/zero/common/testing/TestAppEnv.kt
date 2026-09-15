package com.frame.zero.common.testing

import com.frame.zero.auth.JwtService
import com.frame.zero.auth.testing.FakeUserRepository
import com.frame.zero.config.JwtConfig
import com.frame.zero.dashboard.DashboardService
import com.frame.zero.dashboard.dashboardRoutes
import com.frame.zero.notification.DeviceTokenService
import com.frame.zero.notification.NotificationService
import com.frame.zero.notification.TaskAssignmentNotifier
import com.frame.zero.notification.deviceTokenRoutes
import com.frame.zero.notification.notificationRoutes
import com.frame.zero.notification.testing.FakeDeviceTokenRepository
import com.frame.zero.notification.testing.FakeNotificationRepository
import com.frame.zero.notification.testing.FakePushSender
import com.frame.zero.production.ProductionAccessService
import com.frame.zero.production.ProductionService
import com.frame.zero.production.productionRoutes
import com.frame.zero.production.testing.FakeProductionMemberRepository
import com.frame.zero.production.testing.FakeProductionRepository
import com.frame.zero.schedule.ScheduleService
import com.frame.zero.schedule.scheduleRoutes
import com.frame.zero.schedule.testing.FakeScheduleEventRepository
import com.frame.zero.storage.FileStorage
import com.frame.zero.storage.FilesystemFileStorage
import com.frame.zero.task.TaskService
import com.frame.zero.task.taskRoutes
import com.frame.zero.installStatusPages
import com.frame.zero.task.testing.FakeTaskRepository
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import java.util.UUID
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

internal val testJwtConfig = JwtConfig(
  secret = "test-secret-must-be-long-enough-for-hmac256",
  issuer = "test-issuer",
  audience = "test-audience",
  realm = "test-realm",
  accessTokenTtl = 15.minutes,
  refreshTokenTtl = 30.days
)

internal class TestAppEnv {
  val users = FakeUserRepository()
  val productionMembers = FakeProductionMemberRepository()
  val productions = FakeProductionRepository(productionMembers)
  val tasks = FakeTaskRepository()
  val scheduleEvents = FakeScheduleEventRepository()
  val notificationsRepo = FakeNotificationRepository()
  val deviceTokens = FakeDeviceTokenRepository()
  val pushSender = FakePushSender()

  val jwtService = JwtService(testJwtConfig)
  val transactor = NoopTransactor()
  val access = ProductionAccessService(productions, productionMembers)
  val productionService = ProductionService(productions, productionMembers, users, access, transactor)
  val dashboardService = DashboardService(users, productions, tasks, transactor)

  // Unconfined scope so the fire-and-forget push runs synchronously within the
  // calling test (the fakes never really suspend), keeping assertions deterministic.
  // todo dispatcher
  val assignmentNotifier = TaskAssignmentNotifier(deviceTokens, pushSender, CoroutineScope(UnconfinedTestDispatcher()))
  val fileStorage: FileStorage =
    FilesystemFileStorage(
      java.nio.file.Files.createTempDirectory("framezero-test-uploads").toFile().absolutePath
    )
  val taskService =
    TaskService(
      tasks,
      access,
      productionMembers,
      transactor,
      notificationsRepo,
      assignmentNotifier,
      fileStorage
    )
  val scheduleService = ScheduleService(scheduleEvents, tasks, access, transactor)
  val notificationService = NotificationService(notificationsRepo, transactor)
  val deviceTokenService = DeviceTokenService(deviceTokens, transactor)

  fun tokenFor(userId: UUID): String = jwtService.createAccessToken(userId, "test@test.com")

  fun configure(app: Application) {
    app.install(Koin) {
      modules(
        module {
          single { productionService }
          single { dashboardService }
          single { taskService }
          single { fileStorage }
          single { scheduleService }
          single { notificationService }
          single { deviceTokenService }
        }
      )
    }
    app.install(ContentNegotiation) { json() }
    app.install(WebSockets)
    app.install(Authentication) {
      jwt("auth-jwt") {
        realm = testJwtConfig.realm
        verifier(jwtService.tokenVerifier)
        validate { credential ->
          if (credential.payload.subject.isNullOrBlank()) null else JWTPrincipal(credential.payload)
        }
      }
    }
    app.installStatusPages()
    app.routing {
      dashboardRoutes()
      productionRoutes()
      taskRoutes()
      scheduleRoutes()
      notificationRoutes()
      deviceTokenRoutes()
    }
  }
}
