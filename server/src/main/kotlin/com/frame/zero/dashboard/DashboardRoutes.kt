package com.frame.zero.dashboard

import com.frame.zero.common.userId
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject
import java.time.ZoneId

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
