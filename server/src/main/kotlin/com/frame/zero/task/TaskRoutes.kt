package com.frame.zero.task

import com.frame.zero.AppError
import com.frame.zero.AppException
import com.frame.zero.common.parseUuidField
import com.frame.zero.common.pathUuid
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

fun Route.taskRoutes() {
  authenticate("auth-jwt") {
    route("/api/v1/tasks") {
      get {
        val service by call.inject<TaskService>()
        val userId = call.userId()
        val assigneeMe = call.request.queryParameters["assignee"] == "me"
        val status =
          call.request.queryParameters["status"]?.let {
            runCatching { TaskStatus.valueOf(it) }
              .getOrElse {
                throw AppException(AppError.ValidationError(mapOf("status" to "Invalid value")))
              }
          }
        val productionId =
          call.request.queryParameters["productionId"]?.let { parseUuidField("productionId", it) }
        val limit =
          call.request.queryParameters["limit"]
            ?.toIntOrNull()
            ?.coerceIn(1, 100) ?: 20
        val cursor = call.request.queryParameters["cursor"]
        val (items, nextCursor) =
          service.list(userId, assigneeMe, status, productionId, limit, cursor)
        call.respond(CursorPagedResponse(items = items, nextCursor = nextCursor))
      }

      post {
        val service by call.inject<TaskService>()
        val userId = call.userId()
        val request = call.receive<CreateTaskRequest>()
        val task = service.create(userId, request)
        call.respond(HttpStatusCode.Created, task)
      }

      route("/{id}") {
        get {
          val service by call.inject<TaskService>()
          val userId = call.userId()
          val taskId = call.pathUuid("id")
          call.respond(service.get(userId, taskId))
        }

        patch {
          val service by call.inject<TaskService>()
          val userId = call.userId()
          val taskId = call.pathUuid("id")
          val request = call.receive<UpdateTaskRequest>()
          call.respond(service.update(userId, taskId, request))
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
