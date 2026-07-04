package com.frame.zero.repository.chat

import com.frame.zero.core.session.SessionCleaner
import com.frame.zero.database.FrameZeroDatabase
import com.frame.zero.repository.chat.network.ChatApi
import com.frame.zero.repository.chat.network.ChatApiImpl
import org.koin.dsl.bind
import org.koin.dsl.module

val chatRepositoryModule = module {
  single<ChatApi> { ChatApiImpl(get(), get()) }
  single<ChatRepository> { ChatRepositoryImpl(get(), get(), get()) }
  single {
    ChatSessionCleaner(get<FrameZeroDatabase>().chatDao(), get())
  } bind SessionCleaner::class
}
