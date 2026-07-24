package com.frame.zero.repository.chat.outbox

import org.koin.core.module.Module

/**
 * Durable backstop for the outbox: gets queued messages sent even when the app is no longer
 * running. Purely additive — the repository always kicks the in-process [ChatOutbox] itself, so a
 * foreground send never waits on the OS scheduler.
 *
 * Android backs this with WorkManager (one unique chain per conversation, so FIFO survives).
 * iOS has no equivalent worth having: `BGTaskScheduler` decides when — often not until the app is
 * opened anyway — so there it is the in-process drain plus the app-start flush.
 */
internal interface ChatOutboxScheduler {
  /** Asks the OS to drain [conversationId] later. Never blocks on delivery. */
  suspend fun schedule(conversationId: String)

  /** Drops all scheduled work; sign-out must not leave a send pending for the next user. */
  suspend fun cancelAll()
}

/** Platform half of the outbox wiring, mirroring `uploadPlatformModule()`. */
internal expect fun chatOutboxPlatformModule(): Module
