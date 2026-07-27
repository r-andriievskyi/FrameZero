package com.frame.zero.repository.chat.outbox

import org.koin.core.module.Module
import org.koin.dsl.module

internal actual fun chatOutboxPlatformModule(): Module =
  module {
    single<ChatOutboxScheduler> { WorkManagerChatOutboxScheduler(get()) }
  }
