package com.frame.zero.notification

import com.frame.zero.common.userId
import com.frame.zero.dto.notification.MarkReadRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

fun Route.notificationRoutes() {
  authenticate("auth-jwt") {
    route("/api/v1/notifications") {
      get {
        val service by call.inject<NotificationService>()
        val userId = call.userId()
        val limit =
          call.request.queryParameters["limit"]
            ?.toIntOrNull()
            ?.coerceIn(1, 100) ?: 20
        val cursor = call.request.queryParameters["cursor"]
        call.respond(service.list(userId, limit, cursor))
      }

      post("/read") {
        val service by call.inject<NotificationService>()
        val userId = call.userId()
        val request = call.receive<MarkReadRequest>()
        service.markRead(userId, request)
        call.respond(HttpStatusCode.NoContent)
      }
    }
  }
}
