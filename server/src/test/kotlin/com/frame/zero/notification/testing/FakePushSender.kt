package com.frame.zero.notification.testing

import com.frame.zero.notification.PushSender

internal class FakePushSender : PushSender {
  data class Sent(
    val tokens: List<String>,
    val title: String,
    val body: String,
    val data: Map<String, String>
  )

  val sent: MutableList<Sent> = mutableListOf()

  override suspend fun sendToTokens(
    tokens: List<String>,
    title: String,
    body: String,
    data: Map<String, String>
  ) {
    sent += Sent(tokens, title, body, data)
  }
}
