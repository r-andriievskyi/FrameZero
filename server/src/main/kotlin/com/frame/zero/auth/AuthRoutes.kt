package com.frame.zero.auth

import com.frame.zero.AUTH_RATE_LIMIT_NAME
import com.frame.zero.auth.dto.LoginRequest
import com.frame.zero.auth.dto.LogoutRequest
import com.frame.zero.auth.dto.RefreshRequest
import com.frame.zero.auth.dto.RegisterRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject
import java.util.UUID

fun Route.authRoutes() {
  val service by inject<AuthService>()

  route("/auth") {
    rateLimit(AUTH_RATE_LIMIT_NAME) {
      post("/register") {
        val body = call.receive<RegisterRequest>()
        call.respond(
          HttpStatusCode.Created,
          service.register(body.email, body.password, body.firstName, body.lastName)
        )
      }

      post("/login") {
        val body = call.receive<LoginRequest>()
        call.respond(service.login(body.email, body.password))
      }

      post("/refresh") {
        val body = call.receive<RefreshRequest>()
        call.respond(service.refresh(body.refreshToken))
      }
    }

    post("/logout") {
      val body = call.receive<LogoutRequest>()
      service.logout(body.refreshToken)
      call.respond(HttpStatusCode.NoContent)
    }

    authenticate("auth-jwt") {
      get("/me") {
        val principal =
          call.principal<JWTPrincipal>() ?: return@get call.respond(HttpStatusCode.Unauthorized)
        val userId =
          principal.subject?.let(UUID::fromString)
            ?: return@get call.respond(HttpStatusCode.Unauthorized)
        val user = service.me(userId) ?: return@get call.respond(HttpStatusCode.Unauthorized)
        call.respond(user)
      }
    }
  }
}
