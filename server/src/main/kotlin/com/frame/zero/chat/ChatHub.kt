package com.frame.zero.chat

import com.frame.zero.dto.chat.ChatSocketFrame
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.send
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import java.util.UUID

/** JSON framing for the socket. Discriminator defaults to `type`; see [ChatSocketFrame]. */
internal val chatJson = Json { ignoreUnknownKeys = true }

/**
 * In-process WebSocket registry and per-conversation fan-out. Honest single-node
 * MVP: a `userId → sockets` registry with each socket's subscribed conversations.
 * The named multi-node upgrade path is Redis pub/sub in place of [broadcast]'s
 * in-memory loop.
 *
 * The hub caches no authorization — subscriptions are added only after a DB
 * task-circle check, and [retainSubscribers] drops a user's live subscription the
 * moment they leave a task's circle.
 */
class ChatHub {
  /** One live socket and the conversation ids it is currently subscribed to. */
  class Connection(
    val userId: UUID,
    val session: WebSocketSession
  ) {
    val subscriptions: MutableSet<UUID> = mutableSetOf()
  }

  private val mutex = Mutex()
  private val connections = mutableSetOf<Connection>()

  // Owned by the hub for its app-wide lifetime: sends are fired on this scope so
  // one stalled/dead socket can suspend forever without blocking the caller (the
  // sender's POST) or delaying delivery to any other subscriber.
  private val sendScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

  suspend fun register(connection: Connection) = mutex.withLock { connections.add(connection) }

  suspend fun unregister(connection: Connection) = mutex.withLock { connections.remove(connection) }

  suspend fun subscribe(
    connection: Connection,
    conversationId: UUID
  ) = mutex.withLock { connection.subscriptions.add(conversationId) }

  /** Fan a frame out to every socket subscribed to [conversationId]. Called after commit. */
  suspend fun broadcast(
    conversationId: UUID,
    frame: ChatSocketFrame
  ) {
    val text = chatJson.encodeToString(ChatSocketFrame.serializer(), frame)
    val targets = mutex.withLock {
      connections.filter { conversationId in it.subscriptions }.map { it.session }
    }
    // Each send is launched independently on sendScope, outside the lock and
    // without this suspend function awaiting completion: a slow/blocked socket
    // can only ever stall itself, never the caller or other recipients.
    targets.forEach { session ->
      sendScope.launch { runCatching { session.send(Frame.Text(text)) } }
    }
  }

  /**
   * Drops [conversationId] from the subscriptions of any connected user not in
   * [allowedUserIds] — the revocation triggered when a task's circle shrinks.
   */
  suspend fun retainSubscribers(
    conversationId: UUID,
    allowedUserIds: Set<UUID>
  ) {
    mutex.withLock {
      connections.forEach { connection ->
        if (connection.userId !in allowedUserIds) connection.subscriptions.remove(conversationId)
      }
    }
  }
}
