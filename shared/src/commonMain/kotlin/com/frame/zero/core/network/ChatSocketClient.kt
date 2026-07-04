package com.frame.zero.core.network

import com.frame.zero.core.logging.Logger
import com.frame.zero.dto.chat.ChatSocketFrame
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import kotlin.time.Duration.Companion.seconds

/**
 * Owns the single chat WebSocket. Connect/reconnect with exponential backoff, re-subscribes
 * every tracked conversation on each (re)connect, and exposes a hot [events] stream.
 *
 * The bearer token is attached by the shared [HttpClient]'s `Auth` plugin on the upgrade
 * request (the `/ws` path is not in the unauthenticated set), never in the URL query where it
 * would land in access logs.
 *
 * Receive-only in the MVP: the only client→server frame is `SUBSCRIBE`; the server pushes
 * `MESSAGE` frames. Unknown frame `type`s are ignored so new event kinds don't break the app.
 */
class ChatSocketClient(
  private val httpClient: HttpClient,
  private val networkConfig: NetworkConfig,
  private val logger: Logger,
  scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) {
  private val scope = scope
  private val json = Json { ignoreUnknownKeys = true }

  private val _events = MutableSharedFlow<ChatSocketEvent>(
    extraBufferCapacity = 64,
    onBufferOverflow = BufferOverflow.DROP_OLDEST
  )
  val events: SharedFlow<ChatSocketEvent> = _events.asSharedFlow()

  private val mutex = Mutex()
  private val subscriptions = mutableSetOf<String>()
  private var session: DefaultClientWebSocketSession? = null
  private var connectionJob: Job? = null

  fun start() {
    if (connectionJob?.isActive == true) return
    connectionJob = scope.launch { connectionLoop() }
  }

  /** Stops the socket and clears tracked subscriptions. */
  suspend fun stop() {
    connectionJob?.cancel()
    connectionJob = null
    mutex.withLock {
      subscriptions.clear()
      session?.close()
      session = null
    }
  }

  /**
   * Track [conversationId] and, if the socket is live, send its SUBSCRIBE now. The set is
   * replayed on every reconnect so a dropped socket resubscribes automatically.
   */
  suspend fun subscribe(conversationId: String) {
    val live = mutex.withLock {
      subscriptions.add(conversationId)
      session
    }
    live?.sendFrame(ChatSocketFrame.Subscribe(conversationId))
    start()
  }

  private suspend fun connectionLoop() {
    var backoff = INITIAL_BACKOFF_SECONDS
    while (scope.isActive) {
      try {
        val wsSession = httpClient.webSocketSession(urlString = webSocketUrl())
        mutex.withLock { session = wsSession }
        backoff = INITIAL_BACKOFF_SECONDS
        resubscribeAll(wsSession)
        _events.emit(ChatSocketEvent.Connected)
        receiveLoop(wsSession)
      } catch (cancellation: CancellationException) {
        throw cancellation
      } catch (throwable: Throwable) {
        logger.w(tag = TAG, message = "Chat socket dropped; reconnecting in ${backoff}s")
      } finally {
        mutex.withLock { session = null }
      }
      if (!scope.isActive) break
      delay(backoff.seconds)
      backoff = (backoff * 2).coerceAtMost(MAX_BACKOFF_SECONDS)
    }
  }

  private suspend fun receiveLoop(wsSession: DefaultClientWebSocketSession) {
    for (frame in wsSession.incoming) {
      if (frame !is Frame.Text) continue
      val parsed = runCatching {
        json.decodeFromString(ChatSocketFrame.serializer(), frame.readText())
      }.getOrNull() ?: continue
      when (parsed) {
        is ChatSocketFrame.Message -> _events.emit(ChatSocketEvent.MessageReceived(parsed.message))
        // SUBSCRIBE is client→server only; a server echo is ignored.
        is ChatSocketFrame.Subscribe -> Unit
      }
    }
  }

  private suspend fun resubscribeAll(wsSession: DefaultClientWebSocketSession) {
    val current = mutex.withLock { subscriptions.toList() }
    current.forEach { wsSession.sendFrame(ChatSocketFrame.Subscribe(it)) }
  }

  private suspend fun DefaultClientWebSocketSession.sendFrame(frame: ChatSocketFrame) {
    runCatching { send(json.encodeToString(ChatSocketFrame.serializer(), frame)) }
  }

  private fun webSocketUrl(): String =
    networkConfig.baseUrl
      .replaceFirst("https://", "wss://")
      .replaceFirst("http://", "ws://")
      .trimEnd('/') + WS_PATH

  private companion object {
    const val TAG = "ChatSocket"
    const val WS_PATH = "/ws"
    const val INITIAL_BACKOFF_SECONDS = 1L
    const val MAX_BACKOFF_SECONDS = 30L
  }
}
