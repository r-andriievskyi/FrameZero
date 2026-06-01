package com.frame.zero.task

import com.frame.zero.AppError
import com.frame.zero.AppException
import com.frame.zero.common.pathUuid
import com.frame.zero.common.timezone
import com.frame.zero.common.userId
import com.frame.zero.dto.common.CursorPagedResponse
import com.frame.zero.dto.task.CreateTaskRequest
import com.frame.zero.dto.task.TaskStatus
import com.frame.zero.dto.task.UpdateTaskRequest
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
import java.util.UUID

fun Route.taskRoutes() {
  authenticate("auth-jwt") {
    route("/api/v1/tasks") {
      get {
        val service by call.inject<TaskService>()
        val userId = call.userId()
        val tz = call.timezone()
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
        call.respond(CursorPagedResponse(items = items, nextCursor = nextCursor))
      }

      post {
        val service by call.inject<TaskService>()
        val userId = call.userId()
        val tz = call.timezone()
        val request = call.receive<CreateTaskRequest>()
        val task = service.create(userId, request, tz)
        call.respond(HttpStatusCode.Created, task)
      }

      route("/{id}") {
        get {
          val service by call.inject<TaskService>()
          val userId = call.userId()
          val taskId = call.pathUuid("id")
          val tz = call.timezone()
          call.respond(service.get(userId, taskId, tz))
        }

        patch {
          val service by call.inject<TaskService>()
          val userId = call.userId()
          val taskId = call.pathUuid("id")
          val tz = call.timezone()
          val request = call.receive<UpdateTaskRequest>()
          call.respond(service.update(userId, taskId, request, tz))
        }

        delete {
          val service by call.inject<TaskService>()
          val userId = call.userId()
          val taskId = call.pathUuid("id")
          service.delete(userId, taskId)
          call.respond(HttpStatusCode.NoContent)
        }
      }
    }
  }
}
