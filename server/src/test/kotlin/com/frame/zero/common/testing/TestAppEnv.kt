package com.frame.zero.common.testing

import com.frame.zero.AppException
import com.frame.zero.ErrorResponse
import com.frame.zero.auth.JwtService
import com.frame.zero.auth.testing.FakeUserRepository
import com.frame.zero.chat.CHAT_SEND_RATE_LIMIT_NAME
import com.frame.zero.chat.ChatHub
import com.frame.zero.chat.ChatProductionMemberRevoker
import com.frame.zero.chat.ChatService
import com.frame.zero.chat.ChatTaskCircleRevoker
import com.frame.zero.chat.TaskCircleAccessService
import com.frame.zero.chat.chatRoutes
import com.frame.zero.chat.chatWebSocket
import com.frame.zero.chat.testing.FakeConversationRepository
import com.frame.zero.chat.testing.FakeMessageRepository
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
import com.frame.zero.task.testing.FakeTaskRepository
import com.frame.zero.toResponse
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.ratelimit.RateLimit
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.serialization.SerializationException
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
  val conversations = FakeConversationRepository()
  val chatMessages = FakeMessageRepository()
  val chatHub = ChatHub()

  val jwtService = JwtService(testJwtConfig)
  val transactor = NoopTransactor()
  val access = ProductionAccessService(productions, productionMembers)
  val productionService =
    ProductionService(
      productions,
      productionMembers,
      users,
      access,
      transactor,
      ChatProductionMemberRevoker(conversations, chatHub)
    )
  val dashboardService = DashboardService(users, productions, tasks, transactor)

  // Unconfined scope so the fire-and-forget push runs synchronously within the
  // calling test (the fakes never really suspend), keeping assertions deterministic.
  // todo dispatcher
  val assignmentNotifier = TaskAssignmentNotifier(deviceTokens, pushSender, CoroutineScope(UnconfinedTestDispatcher()))
  val fileStorage: FileStorage =
    FilesystemFileStorage(
      java.nio.file.Files.createTempDirectory("framezero-test-uploads").toFile().absolutePath
    )
  val taskCircleAccess = TaskCircleAccessService(tasks, access)
  val chatService = ChatService(conversations, chatMessages, taskCircleAccess, transactor, chatHub)
  val chatRevoker = ChatTaskCircleRevoker(conversations, chatHub)
  val taskService =
    TaskService(
      tasks,
      access,
      productionMembers,
      transactor,
      notificationsRepo,
      assignmentNotifier,
      fileStorage,
      chatRevoker
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
          single { chatService }
          single { chatHub }
        }
      )
    }
    app.install(ContentNegotiation) { json() }
    app.install(WebSockets)
    app.install(RateLimit) {
      register(CHAT_SEND_RATE_LIMIT_NAME) {
        rateLimiter(limit = 1_000, refillPeriod = 1.minutes)
      }
    }
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
      deviceTokenRoutes()
      chatRoutes()
      chatWebSocket()
    }
  }
}
