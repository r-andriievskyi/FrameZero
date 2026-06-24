package com.frame.zero.feature.task.details

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.frame.zero.core.files.AttachmentFileManager
import com.frame.zero.domain.DomainError
import com.frame.zero.domain.Outcome
import com.frame.zero.dto.task.TaskDetailDto
import com.frame.zero.feature.task.details.usecase.CompleteTaskUseCase
import com.frame.zero.feature.task.details.usecase.GetTaskDetailsUseCase
import com.frame.zero.repository.tasks.TasksRepository
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
  private val tasksRepository: TasksRepository,
  private val attachmentFileManager: AttachmentFileManager,
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
      TaskDetailsIntent.DownloadAttachment -> downloadAttachment()
      TaskDetailsIntent.AttachmentErrorDismissed -> _state.update { it.copy(attachmentError = null) }
    }
  }

  private fun downloadAttachment() {
    val attachment = _state.value.attachment ?: return
    if (_state.value.isDownloadingAttachment) return
    _state.update { it.copy(isDownloadingAttachment = true, attachmentError = null) }
    scope.launch {
      val outcome = tasksRepository.downloadAttachment(taskId, attachment.fileName, attachment.sizeBytes)
      when (outcome) {
        is Outcome.Success -> {
          attachmentFileManager.openWith(outcome.data, attachment.contentType)
          _state.update { it.copy(isDownloadingAttachment = false) }
        }
        is Outcome.Failure ->
          _state.update {
            it.copy(isDownloadingAttachment = false, attachmentError = outcome.error.toDownloadError())
          }
      }
    }
  }

  private fun DomainError.toDownloadError(): AttachmentDownloadError =
    when (this) {
      is DomainError.Offline -> AttachmentDownloadError.OFFLINE
      DomainError.InsufficientStorage -> AttachmentDownloadError.INSUFFICIENT_STORAGE
      else -> AttachmentDownloadError.GENERIC
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
      attachment = attachment?.let {
        TaskAttachment(
          fileName = it.fileName,
          sizeLabel = formatFileSize(it.sizeBytes),
          contentType = it.contentType,
          sizeBytes = it.sizeBytes
        )
      },
      isLoading = false,
      isError = false,
      showMarkCompleteButton = mappedStatus != TaskStatus.COMPLETED
    )
  }

  private fun formatFileSize(bytes: Long): String {
    val kb = 1024.0
    val mb = kb * 1024
    return when {
      bytes >= mb -> formatOneDecimal(bytes / mb) + " MB"
      bytes >= kb -> formatOneDecimal(bytes / kb) + " KB"
      else -> "$bytes B"
    }
  }

  private fun formatOneDecimal(value: Double): String {
    val rounded = (value * 10).toLong()
    return "${rounded / 10}.${rounded % 10}"
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
