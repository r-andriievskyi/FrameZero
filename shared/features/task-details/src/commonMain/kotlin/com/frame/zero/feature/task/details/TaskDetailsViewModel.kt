package com.frame.zero.feature.task.details

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.frame.zero.core.error.DomainErrorMessages
import com.frame.zero.core.error.toUiText
import com.frame.zero.core.files.AttachmentFileManager
import com.frame.zero.domain.DomainError
import com.frame.zero.domain.Outcome
import com.frame.zero.domain.task.AssignableMember
import com.frame.zero.dto.task.TaskDetailDto
import com.frame.zero.dto.task.TaskParticipantDto
import com.frame.zero.feature.task.details.usecase.CompleteTaskUseCase
import com.frame.zero.feature.task.details.usecase.GetAssignableMembersUseCase
import com.frame.zero.feature.task.details.usecase.GetTaskDetailsUseCase
import com.frame.zero.feature.task.details.usecase.ObserveTaskChatUnreadUseCase
import com.frame.zero.feature.task.details.usecase.UpdateTaskParticipantsUseCase
import com.frame.zero.repository.tasks.TasksRepository
import framezero.shared.features.task_details.generated.resources.Res
import framezero.shared.features.task_details.generated.resources.error_auth_failed
import framezero.shared.features.task_details.generated.resources.error_conflict
import framezero.shared.features.task_details.generated.resources.error_forbidden
import framezero.shared.features.task_details.generated.resources.error_network
import framezero.shared.features.task_details.generated.resources.error_not_found
import framezero.shared.features.task_details.generated.resources.error_server
import framezero.shared.features.task_details.generated.resources.error_unknown_fallback
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
  private val getAssignableMembersUseCase: GetAssignableMembersUseCase,
  private val updateTaskParticipantsUseCase: UpdateTaskParticipantsUseCase,
  private val observeTaskChatUnreadUseCase: ObserveTaskChatUnreadUseCase,
  private val tasksRepository: TasksRepository,
  private val attachmentFileManager: AttachmentFileManager,
  dispatcher: CoroutineContext = Dispatchers.Main.immediate
) : InstanceKeeper.Instance {
  private val scope = CoroutineScope(dispatcher + SupervisorJob())

  private val _state = MutableStateFlow(TaskDetailsState(taskId = taskId, isLoading = true))
  val state: StateFlow<TaskDetailsState> = _state.asStateFlow()

  // Last observed chat unread. load() rebuilds the whole state from the task DTO (which has no
  // unread), so it re-applies this through the single [applyUnread] owner rather than scattering
  // copy(unreadChatCount = …) across every state-rebuild path.
  private var lastUnreadChatCount = 0

  init {
    load()
    observeChatUnread()
  }

  private fun observeChatUnread() {
    scope.launch {
      observeTaskChatUnreadUseCase(taskId).collect { count -> applyUnread(count) }
    }
  }

  private fun applyUnread(count: Int) {
    lastUnreadChatCount = count
    _state.update { it.copy(unreadChatCount = count) }
  }

  fun onIntent(intent: TaskDetailsIntent) {
    when (intent) {
      TaskDetailsIntent.Refresh -> load()
      TaskDetailsIntent.MarkComplete -> markComplete()
      TaskDetailsIntent.DownloadAttachment -> downloadAttachment()
      TaskDetailsIntent.AttachmentErrorDismissed -> _state.update { it.copy(attachmentError = null) }
      TaskDetailsIntent.ParticipantPickerOpened ->
        _state.update { it.copy(isParticipantPickerVisible = true, participantQuery = "") }
      TaskDetailsIntent.ParticipantPickerDismissed ->
        _state.update { it.copy(isParticipantPickerVisible = false, participantQuery = "") }
      is TaskDetailsIntent.ParticipantSearchChanged ->
        _state.update { it.copy(participantQuery = intent.query) }
      is TaskDetailsIntent.ParticipantToggled -> toggleParticipant(intent.userId)
      TaskDetailsIntent.ParticipantsErrorDismissed -> _state.update { it.copy(participantsError = null) }
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

  private fun toggleParticipant(userId: String) {
    val current = _state.value
    if (current.isUpdatingParticipants) return
    val currentIds = current.participants.map { it.userId }
    val updatedIds = if (userId in currentIds) currentIds - userId else currentIds + userId
    _state.update { it.copy(isUpdatingParticipants = true) }
    scope.launch {
      val params = UpdateTaskParticipantsUseCase.Params(taskId = taskId, participantUserIds = updatedIds)
      when (val outcome = updateTaskParticipantsUseCase(params)) {
        is Outcome.Success ->
          _state.update {
            it.copy(
              participants = outcome.data.participants.map { p -> p.toUi() }.toImmutableList(),
              isUpdatingParticipants = false
            )
          }
        is Outcome.Failure ->
          _state.update {
            it.copy(isUpdatingParticipants = false, participantsError = outcome.error.toUiText(errorMessages))
          }
      }
    }
  }

  private fun loadAssignableMembers(productionId: String) {
    scope.launch {
      val params = GetAssignableMembersUseCase.Params(productionId = productionId)
      // A failure here just leaves the picker empty; it doesn't affect the rest of the screen.
      when (val outcome = getAssignableMembersUseCase(params)) {
        is Outcome.Success ->
          _state.update {
            it.copy(
              assignableMembers = outcome.data.map { member ->
                member.toUi()
              }.toImmutableList()
            )
          }
        is Outcome.Failure -> Unit
      }
    }
  }

  private fun AssignableMember.toUi(): AssignableMemberUi =
    AssignableMemberUi(
      userId = userId,
      name = name,
      initials = initials,
      avatarColorHex = avatarColorHex
    )

  private fun TaskParticipantDto.toUi(): AssignableMemberUi =
    AssignableMemberUi(
      userId = userId,
      name = name,
      initials = initialsFrom(name),
      avatarColorHex = avatarColorHex
    )

  @OptIn(ExperimentalTime::class)
  private fun load() {
    scope.launch {
      _state.update { it.copy(isLoading = true, isError = false) }
      val today = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date
      when (val result = getTaskDetailsUseCase(taskId)) {
        is Outcome.Success -> {
          _state.update { result.data.toTaskDetailsState(today).copy(unreadChatCount = lastUnreadChatCount) }
          loadAssignableMembers(result.data.productionId)
        }
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
      productionId = productionId,
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
          typeLabel = fileTypeLabel(it.fileName, it.contentType),
          sizeLabel = formatFileSize(it.sizeBytes),
          contentType = it.contentType,
          sizeBytes = it.sizeBytes
        )
      },
      participants = participants.map { it.toUi() }.toImmutableList(),
      isLoading = false,
      isError = false,
      showMarkCompleteButton = mappedStatus != TaskStatus.COMPLETED
    )
  }

  /** Short, human label for the file's kind — extension if present, else a content-type fallback. */
  private fun fileTypeLabel(
    fileName: String,
    contentType: String
  ): String {
    val extension = fileName.substringAfterLast('.', missingDelimiterValue = "")
    return if (extension.isNotBlank() && extension.length <= MAX_EXTENSION_LENGTH) {
      extension.uppercase()
    } else {
      contentType.substringAfterLast('/', missingDelimiterValue = "")
        .substringBefore(';')
        .ifBlank { contentType }
        .uppercase()
    }
  }

  private fun formatFileSize(bytes: Long): String {
    val kb = 1024.0
    val mb = kb * 1024
    return when {
      bytes >= mb -> "${formatOneDecimal(bytes / mb)} MB"
      bytes >= kb -> "${formatOneDecimal(bytes / kb)} KB"
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
      .mapNotNull { it.firstOrNull() }
      .take(2)
      .joinToString("") { it.uppercaseChar().toString() }

  override fun onDestroy() {
    scope.cancel()
  }

  private companion object {
    const val MAX_EXTENSION_LENGTH = 5

    val errorMessages = DomainErrorMessages(
      network = Res.string.error_network,
      server = Res.string.error_server,
      notFound = Res.string.error_not_found,
      forbidden = Res.string.error_forbidden,
      conflict = Res.string.error_conflict,
      invalidCredentials = Res.string.error_auth_failed,
      emailExists = Res.string.error_unknown_fallback,
      fallback = Res.string.error_unknown_fallback
    )
  }
}
