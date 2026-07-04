package com.frame.zero.feature.chat

import androidx.paging.PagingData
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.getOrCreate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

class ChatComponent(
  componentContext: ComponentContext,
  val taskId: String,
  val onBack: () -> Unit,
  viewModelFactory: (taskId: String) -> ChatViewModel
) : ComponentContext by componentContext {
  private val viewModel: ChatViewModel = instanceKeeper.getOrCreate { viewModelFactory(taskId) }

  val state: StateFlow<ChatState>
    get() = viewModel.state

  val messages: Flow<PagingData<ChatMessageUi>>
    get() = viewModel.messages

  val events: SharedFlow<ChatEvent>
    get() = viewModel.events

  fun onIntent(intent: ChatIntent) = viewModel.onIntent(intent)
}
