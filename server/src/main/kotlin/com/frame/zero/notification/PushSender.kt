package com.frame.zero.notification

interface PushSender {
  suspend fun sendToTokens(
    tokens: List<String>,
    body: String,
    data: Map<String, String>
  )
}
