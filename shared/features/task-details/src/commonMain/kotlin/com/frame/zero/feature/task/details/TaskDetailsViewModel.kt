package com.frame.zero.feature.task.details

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.frame.zero.domain.Outcome
import com.frame.zero.dto.task.TaskDetailDto
import com.frame.zero.feature.task.details.usecase.CompleteTaskUseCase
import com.frame.zero.feature.task.details.usecase.GetTaskDetailsUseCase
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
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.coroutines.CoroutineContext
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import com.frame.zero.dto.task.TaskPriority as DtoTaskPriority
import com.frame.zero.dto.task.TaskStatus as DtoTaskStatus

class TaskDetailsViewModel(
  private val taskId: String,
  private val getTaskDetailsUseCase: GetTaskDetailsUseCase,
  private val completeTaskUseCase: CompleteTaskUseCase,
  dispatcher: CoroutineContext = Dispatchers.Main.immediate
) : InstanceKeeper.Instance {
  private val scope = CoroutineScope(dispatcher + SupervisorJob())

  private val _state = MutableStateFlow(TaskDetailsState(taskId = taskId, isLoading = true))
  val state: StateFlow<TaskDetailsState> = _state.asStateFlow()

  private val _events = MutableSharedFlow<TaskDetailsEvent>(
    extraBufferCapacity = 1,
    onBufferOverflow = BufferOverflow.DROP_OLDEST
  )
  val events: SharedFlow<TaskDetailsEvent> = _events.asSharedFlow()

  init {
    load()
  }

  fun onIntent(intent: TaskDetailsIntent) {
    when (intent) {
      TaskDetailsIntent.Refresh -> load()
      TaskDetailsIntent.MarkComplete -> markComplete()
    }
  }

  @OptIn(ExperimentalTime::class)
  private fun load() {
    scope.launch {
      _state.update { it.copy(isLoading = true, isError = false) }
      val today = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date
      when (val result = getTaskDetailsUseCase(taskId)) {
        is Outcome.Success -> _state.update { result.data.toTaskDetailsState(today) }
        is Outcome.Failure -> _state.update { it.copy(isLoading = false, isError = true) }
      }
    }
  }

  private fun markComplete() {
    scope.launch {
      when (completeTaskUseCase(taskId)) {
        is Outcome.Success -> _state.update {
          it.copy(status = TaskStatus.COMPLETED, showMarkCompleteButton = false)
        }
        is Outcome.Failure -> Unit
      }
    }
  }

  private fun TaskDetailDto.toTaskDetailsState(today: LocalDate): TaskDetailsState {
    val mappedStatus = status.toFeatureStatus()
    return TaskDetailsState(
      taskId = id,
      title = title,
      productionName = productionTitle,
      priority = priority.toFeaturePriority(),
      status = mappedStatus,
      assignee = assignee?.let { member ->
        TaskMember(
          initials = initialsFrom(member.name),
          name = member.name,
          avatarColorHex = member.avatarColorHex
        )
      },
      dueDate = dueDate,
      isDueToday = dueDate == today,
      description = description.orEmpty(),
      isLoading = false,
      isError = false,
      showMarkCompleteButton = mappedStatus != TaskStatus.COMPLETED
    )
  }

  private fun DtoTaskStatus.toFeatureStatus(): TaskStatus =
    when (this) {
      DtoTaskStatus.OPEN -> TaskStatus.IN_PROGRESS
      DtoTaskStatus.DONE -> TaskStatus.COMPLETED
    }

  private fun DtoTaskPriority.toFeaturePriority(): TaskPriority =
    when (this) {
      DtoTaskPriority.HIGH -> TaskPriority.HIGH
      DtoTaskPriority.MEDIUM -> TaskPriority.MEDIUM
      DtoTaskPriority.LOW -> TaskPriority.LOW
    }

  private fun initialsFrom(name: String): String =
    name.trim().split(" ")
      .filter { it.isNotBlank() }
      .take(2)
      .map { it.first().uppercaseChar() }
      .joinToString("")

  override fun onDestroy() {
    scope.cancel()
  }
}
