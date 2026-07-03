package com.frame.zero.task

import com.frame.zero.AppError
import com.frame.zero.AppException
import com.frame.zero.common.parseUuidField
import com.frame.zero.common.pathUuid
import com.frame.zero.common.userId
import com.frame.zero.dto.common.CursorPagedResponse
import com.frame.zero.dto.task.CreateTaskRequest
import com.frame.zero.dto.task.TaskPriority
import com.frame.zero.dto.task.TaskStatus
import com.frame.zero.dto.task.UpdateTaskParticipantsRequest
import com.frame.zero.dto.task.UpdateTaskRequest
import com.frame.zero.storage.FileStorage
import io.ktor.http.ContentDisposition
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.authenticate
import io.ktor.server.request.contentType
import io.ktor.server.request.receive
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.response.respondOutputStream
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import org.koin.ktor.ext.inject

const val MAX_ATTACHMENT_BYTES: Long = 50L * 1024 * 1024

private const val MAX_IDEMPOTENCY_KEY_LENGTH = 64

fun Route.taskRoutes() {
  authenticate("auth-jwt") {
    route("/api/v1/tasks") {
      get {
        val service by call.inject<TaskService>()
        val userId = call.userId()
        val assigneeMe = call.request.queryParameters["assignee"] == "me"
        val status = call.request.queryParameters["status"]?.let {
          runCatching { TaskStatus.valueOf(it) }
            .getOrElse {
              throw AppException(AppError.ValidationError(mapOf("status" to "Invalid value")))
            }
        }
        val productionId = call.request.queryParameters["productionId"]?.let {
          parseUuidField("productionId", it)
        }
        val limit = call.request.queryParameters["limit"]?.toIntOrNull()?.coerceIn(1, 100) ?: 20
        val cursor = call.request.queryParameters["cursor"]
        val (items, nextCursor) = service.list(userId, assigneeMe, status, productionId, limit, cursor)
        call.respond(CursorPagedResponse(items = items, nextCursor = nextCursor))
      }

      post {
        val service by call.inject<TaskService>()
        val fileStorage by call.inject<FileStorage>()
        val userId = call.userId()
        val idempotencyKey = call.request.headers["Idempotency-Key"]
        if (idempotencyKey != null && idempotencyKey.length > MAX_IDEMPOTENCY_KEY_LENGTH) {
          throw AppException(AppError.ValidationError(mapOf("idempotencyKey" to "Too long")))
        }
        val task = if (call.request.contentType().match(ContentType.MultiPart.FormData)) {
          val (request, attachment) = call.receiveCreateTaskMultipart(fileStorage)
          service.create(userId, request, attachment, idempotencyKey)
        } else {
          val request = call.receive<CreateTaskRequest>()
          service.create(userId, request, null, idempotencyKey)
        }
        call.respond(HttpStatusCode.Created, task)
      }

      route("/{id}") {
        get {
          val service by call.inject<TaskService>()
          val userId = call.userId()
          val taskId = call.pathUuid("id")
          call.respond(service.get(userId, taskId))
        }

        get("/attachment") {
          val service by call.inject<TaskService>()
          val fileStorage by call.inject<FileStorage>()
          val userId = call.userId()
          val taskId = call.pathUuid("id")
          val attachment = service.getAttachment(userId, taskId)
          call.response.header(
            HttpHeaders.ContentDisposition,
            ContentDisposition.Attachment
              .withParameter(ContentDisposition.Parameters.FileName, attachment.fileName)
              .toString()
          )
          val contentType = runCatching { ContentType.parse(attachment.contentType) }
            .getOrDefault(ContentType.Application.OctetStream)
          call.respondOutputStream(contentType = contentType, status = HttpStatusCode.OK) {
            withContext(Dispatchers.IO) {
              fileStorage.openStream(attachment.storageKey).use { it.copyTo(this@respondOutputStream) }
            }
          }
        }

        patch {
          val service by call.inject<TaskService>()
          val userId = call.userId()
          val taskId = call.pathUuid("id")
          val request = call.receive<UpdateTaskRequest>()
          call.respond(service.update(userId, taskId, request))
        }

        put("/participants") {
          val service by call.inject<TaskService>()
          val userId = call.userId()
          val taskId = call.pathUuid("id")
          val request = call.receive<UpdateTaskParticipantsRequest>()
          call.respond(service.updateParticipants(userId, taskId, request))
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

private suspend fun ApplicationCall.receiveCreateTaskMultipart(
  fileStorage: FileStorage
): Pair<CreateTaskRequest, NewAttachment?> {
  val fields = mutableMapOf<String, String>()
  var attachment: NewAttachment? = null

  receiveMultipart().forEachPart { part ->
    when (part) {
      is PartData.FormItem -> part.name?.let { fields[it] = part.value }

      is PartData.FileItem -> {
        // Exactly one attachment per task: ignore any extra file parts instead of
        // streaming them to disk, where they'd be orphaned (no row ever links them).
        if (attachment == null) {
          val blob = fileStorage.store(part.provider().toInputStream(), MAX_ATTACHMENT_BYTES)
          attachment = NewAttachment(
            fileName = part.originalFileName?.takeIf { it.isNotBlank() } ?: "attachment",
            contentType = part.contentType?.toString() ?: ContentType.Application.OctetStream.toString(),
            sizeBytes = blob.sizeBytes,
            storageKey = blob.storageKey
          )
        }
      }

      else -> {}
    }
    part.release()
  }

  val productionId = fields["productionId"]
  val title = fields["title"]
  if (productionId == null || title == null) {
    attachment?.let { fileStorage.delete(it.storageKey) }
    throw AppException(AppError.ValidationError(mapOf("title" to "Required")))
  }

  return CreateTaskRequest(
    productionId = productionId,
    title = title,
    description = fields["description"]?.ifBlank { null },
    dueDate = fields["dueDate"]?.ifBlank { null }?.let { LocalDate.parse(it) },
    assigneeUserId = fields["assigneeUserId"]?.ifBlank { null },
    priority = fields["priority"]?.let { runCatching { TaskPriority.valueOf(it) }.getOrNull() } ?: TaskPriority.MEDIUM
  ) to attachment
}
