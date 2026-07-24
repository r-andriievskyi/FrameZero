package com.frame.zero.repository.chat.outbox

/** Records the durable-backstop requests the drain makes, so tests can assert when it asks. */
internal class RecordingChatOutboxScheduler : ChatOutboxScheduler {
  val scheduled = mutableListOf<String>()
  var cancellations: Int = 0
    private set

  override suspend fun schedule(conversationId: String) {
    scheduled += conversationId
  }

  override suspend fun cancelAll() {
    cancellations++
  }
}
