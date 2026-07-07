package com.frame.zero.chat

import com.frame.zero.common.testing.TestAppEnv
import com.frame.zero.domain.production.Genre
import com.frame.zero.dto.production.CreateProductionRequest
import com.frame.zero.dto.task.CreateTaskRequest
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketExtension
import io.ktor.websocket.WebSocketSession
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import java.util.UUID
import kotlin.coroutines.CoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ChatRevocationTest {
  private val productionRequest =
    CreateProductionRequest(
      title = "Film",
      genre = Genre.DRAMA,
      startDate = LocalDate(2026, 1, 1),
      wrapDate = LocalDate(2026, 12, 31)
    )

  @Test
  fun `removing a production member drops their live chat subscriptions`() =
    runBlocking {
      val env = TestAppEnv()
      val owner = UUID.randomUUID()
      val removedUser = UUID.randomUUID()
      val prod = env.productionService.createProduction(owner, productionRequest)
      val prodId = UUID.fromString(prod.id)
      val memberRecord = env.productionMembers.add(prodId, removedUser, "Bea", "Grip", null)
      val task = env.taskService.create(owner, CreateTaskRequest(productionId = prod.id, title = "T"))
      val conversationId = UUID.fromString(
        env.chatService.getOrCreateTaskConversation(owner, UUID.fromString(task.id)).id
      )

      val connection = ChatHub.Connection(removedUser, StubWebSocketSession())
      env.chatHub.register(connection)
      env.chatHub.subscribe(connection, conversationId)

      env.productionService.removeMember(owner, prodId, memberRecord.id)

      assertTrue(
        connection.subscriptions.isEmpty(),
        "a removed member's live subscriptions must be dropped"
      )
    }

  @Test
  fun `a user still linked to another member row keeps their subscription`() =
    runBlocking {
      val env = TestAppEnv()
      val owner = UUID.randomUUID()
      val user = UUID.randomUUID()
      val prod = env.productionService.createProduction(owner, productionRequest)
      val prodId = UUID.fromString(prod.id)
      val firstRow = env.productionMembers.add(prodId, user, "Bea", "Grip", null)
      env.productionMembers.add(prodId, user, "Bea", "Gaffer", null)
      val task = env.taskService.create(owner, CreateTaskRequest(productionId = prod.id, title = "T"))
      val conversationId = UUID.fromString(
        env.chatService.getOrCreateTaskConversation(owner, UUID.fromString(task.id)).id
      )

      val connection = ChatHub.Connection(user, StubWebSocketSession())
      env.chatHub.register(connection)
      env.chatHub.subscribe(connection, conversationId)

      env.productionService.removeMember(owner, prodId, firstRow.id)

      assertEquals(
        setOf(conversationId),
        connection.subscriptions,
        "a user who is still a member via another row must keep the subscription"
      )
    }

  // The hub never touches the session during revocation; this only satisfies
  // Connection's constructor.
  private class StubWebSocketSession : WebSocketSession {
    override val coroutineContext: CoroutineContext = Job()
    override var masking: Boolean = false
    override var maxFrameSize: Long = Long.MAX_VALUE
    override val incoming: ReceiveChannel<Frame> = Channel()
    override val outgoing: SendChannel<Frame> = Channel()
    override val extensions: List<WebSocketExtension<*>> = emptyList()

    override suspend fun send(frame: Frame) = Unit

    override suspend fun flush() = Unit

    @Deprecated(
      "Use cancel() instead.",
      ReplaceWith("cancel()", "kotlinx.coroutines.cancel")
    )
    override fun terminate() = Unit
  }
}
