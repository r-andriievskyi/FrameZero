package com.frame.zero.dto.chat

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * WebSocket envelope, discriminated by a `type` field (kotlinx's default class
 * discriminator). The MVP defines exactly two frames: the client sends
 * [Subscribe]; the server sends [Message]. New frame kinds (READ, TYPING,
 * REACTION, …) are added as sealed subtypes in later phases — deployed clients
 * ignore `type`s they don't know rather than failing, so the protocol is
 * forward-compatible.
 */
@Serializable
sealed interface ChatSocketFrame {
  /** Client → server: start receiving live messages for a conversation. */
  @Serializable
  @SerialName("SUBSCRIBE")
  data class Subscribe(
    val conversationId: String
  ) : ChatSocketFrame

  /** Server → client: a newly persisted message. */
  @Serializable
  @SerialName("MESSAGE")
  data class Message(
    val message: ChatMessageDto
  ) : ChatSocketFrame

  /**
   * Server → client: the sender's read cursor advanced. Fanned out only to the
   * reader's own other connections so their devices stay in sync; never seen by the
   * rest of the conversation.
   */
  @Serializable
  @SerialName("READ")
  data class Read(
    val conversationId: String,
    val lastReadOrdinal: Long
  ) : ChatSocketFrame
}
