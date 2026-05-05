package com.frame.zero.routes

import com.frame.zero.AppError
import com.frame.zero.AppException
import com.frame.zero.dto.common.PagedResponse
import com.frame.zero.dto.task.CreateTaskRequest
import com.frame.zero.dto.task.TaskStatus
import com.frame.zero.dto.task.UpdateTaskRequest
import com.frame.zero.services.TaskService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
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
import java.util.UUID

fun Route.taskRoutes() {
  val service by inject<TaskService>()

  authenticate("auth-jwt") {
    route("/api/v1/tasks") {
      get {
        val userId = call.userId()
        val tz = call.timezoneHeader()
        val assigneeMe = call.request.queryParameters["assignee"] == "me"
        val status =
          call.request.queryParameters["status"]?.let {
            runCatching { TaskStatus.valueOf(it) }
              .getOrElse {
                throw AppException(AppError.ValidationError(mapOf("status" to "Invalid value")))
              }
          }
        val productionId =
          call.request.queryParameters["productionId"]?.let {
            runCatching { UUID.fromString(it) }
              .getOrElse {
                throw AppException(
                  AppError.ValidationError(mapOf("productionId" to "Invalid UUID"))
                )
              }
          }
        val limit =
          call.request.queryParameters["limit"]
            ?.toIntOrNull()
            ?.coerceIn(1, 100) ?: 20
        val cursor = call.request.queryParameters["cursor"]
        val (items, nextCursor) =
          service.list(userId, assigneeMe, status, productionId, limit, cursor, tz)
        call.respond(PagedResponse(items = items, nextCursor = nextCursor))
      }

      post {
        val userId = call.userId()
        val tz = call.timezoneHeader()
        val request = call.receive<CreateTaskRequest>()
        val task = service.create(userId, request, tz)
        call.respond(HttpStatusCode.Created, task)
      }

      route("/{id}") {
        get {
          val userId = call.userId()
          val taskId = call.pathUuid("id")
          val tz = call.timezoneHeader()
          call.respond(service.get(userId, taskId, tz))
        }

        patch {
          val userId = call.userId()
          val taskId = call.pathUuid("id")
          val tz = call.timezoneHeader()
          val request = call.receive<UpdateTaskRequest>()
          call.respond(service.update(userId, taskId, request, tz))
        }

        delete {
          val userId = call.userId()
          val taskId = call.pathUuid("id")
          service.delete(userId, taskId)
          call.respond(HttpStatusCode.NoContent)
        }
      }
    }
  }
}

private fun ApplicationCall.timezoneHeader(): ZoneId =
  request.headers["X-Timezone"]?.let {
    runCatching { ZoneId.of(it) }.getOrDefault(ZoneId.of("UTC"))
  } ?: ZoneId.of("UTC")
