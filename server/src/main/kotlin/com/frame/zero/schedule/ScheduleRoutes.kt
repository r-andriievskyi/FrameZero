package com.frame.zero.schedule

import com.frame.zero.AppError
import com.frame.zero.AppException
import com.frame.zero.common.pathUuid
import com.frame.zero.common.userId
import com.frame.zero.dto.schedule.CreateScheduleEventRequest
import com.frame.zero.dto.schedule.UpdateScheduleEventRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject
import java.time.ZoneId

fun Route.scheduleRoutes() {
  val service by inject<ScheduleService>()

  authenticate("auth-jwt") {
    route("/api/v1/schedule") {
      get {
        val userId = call.userId()
        val tz =
          call.request.headers["X-Timezone"]?.let {
            runCatching { ZoneId.of(it) }.getOrDefault(ZoneId.of("UTC"))
          } ?: ZoneId.of("UTC")
        val view =
          call.request.queryParameters["view"]
            ?: throw AppException(
              AppError.ValidationError(mapOf("view" to "Required: day, week, or month"))
            )
        val dateParam =
          call.request.queryParameters["date"]
            ?: throw AppException(AppError.ValidationError(mapOf("date" to "Required")))
        call.respond(service.get(userId, view, dateParam, tz))
      }

      post {
        val userId = call.userId()
        val request = call.receive<CreateScheduleEventRequest>()
        val event = service.create(userId, request)
        call.respond(HttpStatusCode.Created, event)
      }

      route("/{id}") {
        patch {
          val userId = call.userId()
          val eventId = call.pathUuid("id")
          val request = call.receive<UpdateScheduleEventRequest>()
          call.respond(service.update(userId, eventId, request))
        }

        delete {
          val userId = call.userId()
          val eventId = call.pathUuid("id")
          service.delete(userId, eventId)
          call.respond(HttpStatusCode.NoContent)
        }
      }
    }
  }
}
