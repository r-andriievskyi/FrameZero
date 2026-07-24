package com.frame.zero.repository.chat

import com.frame.zero.core.session.SessionCleaner
import com.frame.zero.database.FrameZeroDatabase
import com.frame.zero.repository.chat.network.ChatApi
import com.frame.zero.repository.chat.network.ChatApiImpl
import com.frame.zero.repository.chat.outbox.ChatOutbox
import com.frame.zero.repository.chat.outbox.ChatOutboxStarter
import com.frame.zero.repository.chat.outbox.ChatOutboxStore
import com.frame.zero.repository.chat.outbox.chatOutboxPlatformModule
import org.koin.dsl.bind
import org.koin.dsl.module

val chatRepositoryModule = module {
  includes(chatOutboxPlatformModule())
  single<ChatApi> { ChatApiImpl(get(), get()) }
  single { ChatOutboxStore(get<FrameZeroDatabase>().chatOutboxDao()) }
  // Single, so the per-conversation mutex is shared by every drain trigger — UI send, socket
  // reconnect, connectivity, and the WorkManager worker alike.
  single { ChatOutbox(get(), get(), get<FrameZeroDatabase>().chatDao(), get()) }
  single<ChatRepository> { ChatRepositoryImpl(get(), get(), get(), get(), get(), get(), get()) }
  single(createdAtStart = true) { ChatOutboxStarter(get(), get()) }
  single {
    ChatSessionCleaner(get<FrameZeroDatabase>().chatDao(), get(), get())
  } bind SessionCleaner::class
}
