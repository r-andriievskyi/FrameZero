package com.frame.zero.notification

import com.frame.zero.common.userId
import com.frame.zero.dto.device.RegisterDeviceTokenRequest
import com.frame.zero.dto.device.UnregisterDeviceTokenRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

fun Route.deviceTokenRoutes() {
  authenticate("auth-jwt") {
    route("/api/v1/device-tokens") {
      post {
        val service by call.inject<DeviceTokenService>()
        val userId = call.userId()
        val request = call.receive<RegisterDeviceTokenRequest>()
        service.register(userId, request)
        call.respond(HttpStatusCode.NoContent)
      }

      delete {
        val service by call.inject<DeviceTokenService>()
        val userId = call.userId()
        val request = call.receive<UnregisterDeviceTokenRequest>()
        service.unregister(userId, request)
        call.respond(HttpStatusCode.NoContent)
      }
    }
  }
}
