package com.frame.zero.chat

import com.frame.zero.common.testing.TestAppEnv
import com.frame.zero.domain.production.Genre
import com.frame.zero.dto.chat.ChatSocketFrame
import com.frame.zero.dto.production.CreateProductionRequest
import com.frame.zero.dto.task.CreateTaskRequest
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.server.testing.testApplication
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.datetime.LocalDate
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ChatWebSocketTest {
  private val productionRequest =
    CreateProductionRequest(
      title = "Film",
      genre = Genre.DRAMA,
      startDate = LocalDate(2026, 1, 1),
      wrapDate = LocalDate(2026, 12, 31)
    )

  @Test
  fun `a subscribed socket receives a MESSAGE frame when a message is sent`() =
    testApplication {
      val env = TestAppEnv()
      application { env.configure(this) }
      val owner = UUID.randomUUID()
      val token = env.tokenFor(owner)
      val prod = env.productionService.createProduction(owner, productionRequest)
      val task = env.taskService.create(owner, CreateTaskRequest(productionId = prod.id, title = "T"))
      val conversationId = UUID.fromString(
        env.chatService.getOrCreateTaskConversation(owner, UUID.fromString(task.id)).id
      )

      val wsClient = createClient { install(WebSockets) }

      var received: ChatSocketFrame.Message? = null
      wsClient.webSocket("/ws", request = { header(HttpHeaders.Authorization, "Bearer $token") }) {
        send(
          Frame.Text(
            chatJson.encodeToString(
              ChatSocketFrame.serializer(),
              ChatSocketFrame.Subscribe(conversationId.toString())
            )
          )
        )

        // The server registers the subscription asynchronously in its receive loop,
        // and the MVP socket has no ACK. Retry sends (each with a fresh client id)
        // until one lands on the live subscription — deterministic without a sleep.
        for (attempt in 0 until 20) {
          env.chatService.send(owner, conversationId, "cid-$attempt", "hello")
          val frame = withTimeoutOrNull(300) { incoming.receive() }
          if (frame is Frame.Text) {
            received = chatJson.decodeFromString(ChatSocketFrame.serializer(), frame.readText())
              as? ChatSocketFrame.Message
            if (received != null) break
          }
        }
      }

      val message = assertNotNull(received, "a subscribed socket must receive a MESSAGE frame").message
      assertEquals("hello", message.body)
    }

  @Test
  fun `markRead pushes a READ frame to the user's own socket`() =
    testApplication {
      val env = TestAppEnv()
      application { env.configure(this) }
      val owner = UUID.randomUUID()
      val token = env.tokenFor(owner)
      val prod = env.productionService.createProduction(owner, productionRequest)
      val task = env.taskService.create(owner, CreateTaskRequest(productionId = prod.id, title = "T"))
      val conversationId = UUID.fromString(
        env.chatService.getOrCreateTaskConversation(owner, UUID.fromString(task.id)).id
      )
      // Give the cursor room to advance one ordinal per retry so every attempt broadcasts.
      repeat(20) { env.chatService.send(owner, conversationId, "cid-$it", "m$it") }

      val wsClient = createClient { install(WebSockets) }

      var received: ChatSocketFrame.Read? = null
      wsClient.webSocket("/ws", request = { header(HttpHeaders.Authorization, "Bearer $token") }) {
        // No subscribe: READ targets the user's connections directly, not a subscription.
        for (attempt in 1..20) {
          env.chatService.markRead(owner, conversationId, attempt.toLong())
          val frame = withTimeoutOrNull(300) { incoming.receive() }
          if (frame is Frame.Text) {
            received = chatJson.decodeFromString(ChatSocketFrame.serializer(), frame.readText())
              as? ChatSocketFrame.Read
            if (received != null) break
          }
        }
      }

      val read = assertNotNull(received, "a READ frame must reach the user's own socket")
      assertEquals(conversationId.toString(), read.conversationId)
    }

  @Test
  fun `the socket upgrade requires a valid token`() =
    testApplication {
      val env = TestAppEnv()
      application { env.configure(this) }
      val wsClient = createClient { install(WebSockets) }

      var upgraded = false
      runCatching {
        wsClient.webSocket("/ws") { upgraded = true }
      }
      assertEquals(false, upgraded, "an unauthenticated upgrade must be rejected")
    }
}
