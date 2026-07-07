package com.frame.zero.chat

import com.frame.zero.common.ProductionMemberRevocationListener
import com.frame.zero.common.TaskCircleRevocationListener
import org.koin.dsl.module

fun chatModule() =
  module {
    single<ConversationRepository> { ConversationRepositoryImpl() }
    single<MessageRepository> { MessageRepositoryImpl() }
    single { TaskCircleAccessService(get(), get()) }
    single { ChatHub() }
    single { ChatService(get(), get(), get(), get(), get()) }
    single<TaskCircleRevocationListener> { ChatTaskCircleRevoker(get(), get()) }
    single<ProductionMemberRevocationListener> { ChatProductionMemberRevoker(get(), get()) }
  }
