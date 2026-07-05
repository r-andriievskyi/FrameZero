package com.frame.zero.dto.chat

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * WebSocket envelope, discriminated by a `type` field (kotlinx's default class
 * discriminator). The MVP defines exactly two frames: the client sends [Subscribe];
 * the server sends [Message]. New frame kinds (READ, TYPING, REACTION, …) are added
 * as sealed subtypes in later phases — deployed clients must ignore `type`s they
 * don't know rather than failing, so the protocol stays forward-compatible.
 *
 * Client copy, duplicated from the server copy under the same package name.
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

  /** Server → client: the user's own read cursor advanced on another device. */
  @Serializable
  @SerialName("READ")
  data class Read(
    val conversationId: String,
    val lastReadOrdinal: Long
  ) : ChatSocketFrame
}
