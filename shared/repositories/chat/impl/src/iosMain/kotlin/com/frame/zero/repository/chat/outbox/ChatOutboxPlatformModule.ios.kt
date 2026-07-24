package com.frame.zero.repository.chat.outbox

import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * iOS has no durable background send. `BGTaskScheduler` runs when the system feels like it — often
 * not before the user reopens the app — so scheduling one would add moving parts without moving
 * delivery. Queued messages go out on the in-process drain while the app lives, and on the
 * app-start flush otherwise; the outbox row is what makes that safe.
 */
internal class InProcessChatOutboxScheduler : ChatOutboxScheduler {
  override suspend fun schedule(conversationId: String) = Unit

  override suspend fun cancelAll() = Unit
}

internal actual fun chatOutboxPlatformModule(): Module =
  module {
    single<ChatOutboxScheduler> { InProcessChatOutboxScheduler() }
  }
