package com.frame.zero.routes

import com.frame.zero.services.DashboardService
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import java.time.ZoneId
import java.util.UUID
import org.koin.ktor.ext.inject

fun Route.dashboardRoutes() {
  val service by inject<DashboardService>()

  authenticate("auth-jwt") {
    route("/api/v1") {
      get("/dashboard") {
        val userId = call.userId()
        val tz = call.request.headers["X-Timezone"]?.let {
          runCatching { ZoneId.of(it) }.getOrDefault(ZoneId.of("UTC"))
        } ?: ZoneId.of("UTC")
        call.respond(service.get(userId, tz))
      }
    }
  }
}
