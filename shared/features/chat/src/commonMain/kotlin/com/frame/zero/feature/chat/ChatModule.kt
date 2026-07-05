package com.frame.zero.feature.chat

import com.frame.zero.feature.chat.domain.GetCurrentUserIdUseCase
import com.frame.zero.feature.chat.domain.MarkReadUseCase
import com.frame.zero.feature.chat.domain.OpenConversationUseCase
import com.frame.zero.feature.chat.domain.SendMessageUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

val featureChatModule: Module = module {
  factory { OpenConversationUseCase(get()) }
  factory { SendMessageUseCase(get()) }
  factory { MarkReadUseCase(get()) }
  factory { GetCurrentUserIdUseCase(get()) }
  factory { (taskId: String) ->
    ChatViewModel(
      taskId = taskId,
      chatRepository = get(),
      openConversationUseCase = get(),
      sendMessageUseCase = get(),
      markReadUseCase = get(),
      getCurrentUserIdUseCase = get()
    )
  }
}
