package com.frame.zero.feature.chat

import com.frame.zero.feature.chat.domain.DiscardPendingMessageUseCase
import com.frame.zero.feature.chat.domain.EnqueueMessageUseCase
import com.frame.zero.feature.chat.domain.GetCurrentUserIdUseCase
import com.frame.zero.feature.chat.domain.MarkReadUseCase
import com.frame.zero.feature.chat.domain.ObservePendingMessagesUseCase
import com.frame.zero.feature.chat.domain.OpenConversationUseCase
import com.frame.zero.feature.chat.domain.RetryPendingMessageUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

val featureChatModule: Module = module {
  factory { OpenConversationUseCase(get()) }
  factory { EnqueueMessageUseCase(get()) }
  factory { ObservePendingMessagesUseCase(get()) }
  factory { RetryPendingMessageUseCase(get()) }
  factory { DiscardPendingMessageUseCase(get()) }
  factory { MarkReadUseCase(get()) }
  factory { GetCurrentUserIdUseCase(get()) }
  factory { (taskId: String) ->
    ChatViewModel(
      taskId = taskId,
      chatRepository = get(),
      openConversationUseCase = get(),
      enqueueMessageUseCase = get(),
      observePendingMessagesUseCase = get(),
      retryPendingMessageUseCase = get(),
      discardPendingMessageUseCase = get(),
      markReadUseCase = get(),
      getCurrentUserIdUseCase = get()
    )
  }
}
