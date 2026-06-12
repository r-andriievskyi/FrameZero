package com.frame.zero.dashboard

import com.frame.zero.common.userId
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

fun Route.dashboardRoutes() {
  authenticate("auth-jwt") {
    route("/api/v1") {
      get("/dashboard") {
        val service by call.inject<DashboardService>()
        val userId = call.userId()
        call.respond(service.get(userId))
      }
    }
  }
}
