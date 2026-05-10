package com.frame.zero.dashboard

import com.frame.zero.common.timezone
import com.frame.zero.common.userId
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

fun Route.dashboardRoutes() {
  val service by inject<DashboardService>()

  authenticate("auth-jwt") {
    route("/api/v1") {
      get("/dashboard") {
        val userId = call.userId()
        val tz = call.timezone()
        call.respond(service.get(userId, tz))
      }
    }
  }
}
