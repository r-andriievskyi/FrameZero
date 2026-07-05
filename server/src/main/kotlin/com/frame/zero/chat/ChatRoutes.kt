package com.frame.zero.chat

import com.frame.zero.common.pathUuid
import com.frame.zero.common.userId
import com.frame.zero.dto.chat.ChatSocketFrame
import com.frame.zero.dto.chat.MarkReadRequest
import com.frame.zero.dto.chat.MarkReadResponse
import com.frame.zero.dto.chat.SendMessageRequest
import com.frame.zero.dto.common.CursorPagedResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import org.koin.ktor.ext.inject
import java.util.UUID

val CHAT_SEND_RATE_LIMIT_NAME = RateLimitName("chat-send")

private const val DEFAULT_MESSAGE_PAGE = 30
private const val MAX_MESSAGE_PAGE = 100
private const val MAX_SUBSCRIPTIONS_PER_SOCKET = 50

fun Route.chatRoutes() {
  authenticate("auth-jwt") {
    get("/api/v1/tasks/{id}/conversation") {
      val service by call.inject<ChatService>()
      call.respond(service.getOrCreateTaskConversation(call.userId(), call.pathUuid("id")))
    }

    route("/api/v1/conversations/{id}/messages") {
      get {
        val service by call.inject<ChatService>()
        val conversationId = call.pathUuid("id")
        val before = call.request.queryParameters["before"]?.toLongOrNull()
        val limit = call.request.queryParameters["limit"]?.toIntOrNull()
          ?.coerceIn(1, MAX_MESSAGE_PAGE) ?: DEFAULT_MESSAGE_PAGE
        val (items, nextCursor) = service.listMessages(call.userId(), conversationId, before, limit)
        call.respond(CursorPagedResponse(items = items, nextCursor = nextCursor))
      }

      rateLimit(CHAT_SEND_RATE_LIMIT_NAME) {
        post {
          val service by call.inject<ChatService>()
          val conversationId = call.pathUuid("id")
          val request = call.receive<SendMessageRequest>()
          val message = service.send(call.userId(), conversationId, request.clientMessageId, request.body)
          call.respond(HttpStatusCode.Created, message)
        }
      }
    }

    put("/api/v1/conversations/{id}/read") {
      val service by call.inject<ChatService>()
      val conversationId = call.pathUuid("id")
      val request = call.receive<MarkReadRequest>()
      val applied = service.markRead(call.userId(), conversationId, request.lastReadOrdinal)
      call.respond(MarkReadResponse(applied))
    }
  }
}

/**
 * Receive-only chat socket. JWT is verified on the upgrade request by the
 * `auth-jwt` provider (never in the URL query). The client sends `SUBSCRIBE`
 * frames — each task-circle-checked against the DB — and the server pushes
 * `MESSAGE` frames via the hub.
 */
fun Route.chatWebSocket() {
  authenticate("auth-jwt") {
    webSocket("/ws") {
      val hub by call.inject<ChatHub>()
      val service by call.inject<ChatService>()
      val userId = call.userId()
      val connection = ChatHub.Connection(userId, this)
      hub.register(connection)
      try {
        for (frame in incoming) {
          handleClientFrame(frame, connection, userId, service, hub)
        }
      } finally {
        hub.unregister(connection)
      }
    }
  }
}

/**
 * Handles one inbound client frame. Unknown/garbled frames and over-cap or
 * unauthorized subscriptions are dropped silently rather than closing the socket,
 * so a misbehaving client can't take its own connection down mid-session.
 */
private suspend fun handleClientFrame(
  frame: Frame,
  connection: ChatHub.Connection,
  userId: UUID,
  service: ChatService,
  hub: ChatHub
) {
  if (frame !is Frame.Text) return
  val parsed = runCatching {
    chatJson.decodeFromString(ChatSocketFrame.serializer(), frame.readText())
  }.getOrNull() ?: return
  when (parsed) {
    is ChatSocketFrame.Subscribe -> {
      if (connection.subscriptions.size >= MAX_SUBSCRIPTIONS_PER_SOCKET) return
      val conversationId = runCatching { UUID.fromString(parsed.conversationId) }.getOrNull() ?: return
      // Re-checked from the DB on every subscribe; never cached in the session.
      if (service.canAccessConversation(userId, conversationId)) {
        hub.subscribe(connection, conversationId)
      }
    }
    // MESSAGE and READ are server→client only; a client that sends one is ignored.
    is ChatSocketFrame.Message -> {}
    is ChatSocketFrame.Read -> {}
  }
}
