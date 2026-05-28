package com.frame.zero.feature.task.details

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.coroutines.CoroutineContext

class TaskDetailsViewModel(
  private val taskId: String,
  dispatcher: CoroutineContext = Dispatchers.Main
) : InstanceKeeper.Instance {
  private val scope = CoroutineScope(dispatcher + SupervisorJob())

  private val _state = MutableStateFlow(TaskDetailsState(taskId = taskId))
  val state: StateFlow<TaskDetailsState> = _state.asStateFlow()

  private val _events =
    MutableSharedFlow<TaskDetailsEvent>(
      extraBufferCapacity = 1,
      onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
  val events: SharedFlow<TaskDetailsEvent> = _events.asSharedFlow()

  fun onIntent(intent: TaskDetailsIntent) {
    when (intent) {
      TaskDetailsIntent.Refresh -> Unit
      TaskDetailsIntent.ToggleComplete -> {
        _state.update { current ->
          val newStatus = if (current.status == TaskStatus.COMPLETED) {
            TaskStatus.IN_PROGRESS
          } else {
            TaskStatus.COMPLETED
          }
          current.copy(status = newStatus)
        }
      }
      is TaskDetailsIntent.ToggleChecklistItem -> {
        _state.update { current ->
          val updatedChecklist = current.checklist.map { item ->
            if (item.id == intent.itemId) item.copy(isCompleted = !item.isCompleted) else item
          }
          current.copy(checklist = updatedChecklist)
        }
      }
    }
  }

  override fun onDestroy() {
    scope.cancel()
  }
}
