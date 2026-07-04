package com.frame.zero.chat

import com.frame.zero.common.TaskCircleRevocationListener
import org.koin.dsl.module

fun chatModule() =
  module {
    single<ConversationRepository> { ConversationRepositoryImpl() }
    single<MessageRepository> { MessageRepositoryImpl() }
    single { TaskCircleAccessService(get(), get()) }
    // One hub per process: the in-memory registry every socket and broadcast shares.
    single { ChatHub() }
    single { ChatService(get(), get(), get(), get(), get()) }
    // Bound as the task layer's revocation hook so a shrinking task circle drops
    // stale live subscriptions.
    single<TaskCircleRevocationListener> { ChatTaskCircleRevoker(get(), get()) }
  }
