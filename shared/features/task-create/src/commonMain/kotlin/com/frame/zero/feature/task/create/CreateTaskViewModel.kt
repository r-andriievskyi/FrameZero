package com.frame.zero.feature.task.create

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.frame.zero.core.collections.mapImmutable
import com.frame.zero.core.error.DomainErrorMessages
import com.frame.zero.core.error.toUiText
import com.frame.zero.domain.task.AssignableMember
import com.frame.zero.core.files.AttachmentFileManager
import com.frame.zero.core.files.FilePicker
import com.frame.zero.core.files.MAX_ATTACHMENT_BYTES
import com.frame.zero.core.files.PickedFile
import com.frame.zero.core.upload.PendingTaskUpload
import com.frame.zero.core.upload.TaskUploadScheduler
import com.frame.zero.domain.Outcome
import com.frame.zero.feature.task.create.domain.CreateTaskUseCase
import com.frame.zero.feature.task.create.domain.GetAssignableMembersUseCase
import com.frame.zero.ui.asUiText
import framezero.shared.features.task_create.generated.resources.Res
import framezero.shared.features.task_create.generated.resources.error_auth_failed
import framezero.shared.features.task_create.generated.resources.error_conflict
import framezero.shared.features.task_create.generated.resources.error_file_too_large
import framezero.shared.features.task_create.generated.resources.error_forbidden
import framezero.shared.features.task_create.generated.resources.error_network
import framezero.shared.features.task_create.generated.resources.error_not_found
import framezero.shared.features.task_create.generated.resources.error_server
import framezero.shared.features.task_create.generated.resources.error_title_required
import framezero.shared.features.task_create.generated.resources.error_unknown_fallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.collections.immutable.toImmutableList
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
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.coroutines.CoroutineContext
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class CreateTaskViewModel(
  private val productionId: String,
  productionTitle: String,
  private val createTaskUseCase: CreateTaskUseCase,
  private val getAssignableMembersUseCase: GetAssignableMembersUseCase,
  private val filePicker: FilePicker,
  private val uploadScheduler: TaskUploadScheduler,
  private val attachmentFileManager: AttachmentFileManager,
  private val clock: Clock = Clock.System,
  private val timeZone: TimeZone = TimeZone.currentSystemDefault(),
  dispatcher: CoroutineContext = Dispatchers.Main.immediate
) : InstanceKeeper.Instance {
  private val scope = CoroutineScope(dispatcher + SupervisorJob())

  private val _state = MutableStateFlow(
    CreateTaskState(productionTitle = productionTitle)
  )
  val state: StateFlow<CreateTaskState> = _state.asStateFlow()

  private val _events = MutableSharedFlow<CreateTaskEvent>(
    extraBufferCapacity = 1,
    onBufferOverflow = BufferOverflow.DROP_OLDEST
  )
  val events: SharedFlow<CreateTaskEvent> = _events.asSharedFlow()

  init {
    loadMembers()
  }

  fun onIntent(intent: CreateTaskIntent) {
    when (intent) {
      is CreateTaskIntent.TitleChanged ->
        _state.update { it.copy(title = intent.title, titleError = null) }
      is CreateTaskIntent.DescriptionChanged ->
        _state.update { it.copy(description = intent.description) }
      CreateTaskIntent.AssigneePickerOpened ->
        _state.update { it.copy(isAssigneePickerVisible = true, assigneeQuery = "") }
      CreateTaskIntent.AssigneePickerDismissed ->
        _state.update { it.copy(isAssigneePickerVisible = false, assigneeQuery = "") }
      is CreateTaskIntent.AssigneeSearchChanged ->
        _state.update { it.copy(assigneeQuery = intent.query) }
      is CreateTaskIntent.AssigneeSelected ->
        _state.update {
          it.copy(assigneeUserId = intent.userId, isAssigneePickerVisible = false, assigneeQuery = "")
        }
      CreateTaskIntent.ParticipantPickerOpened ->
        _state.update { it.copy(isParticipantPickerVisible = true, participantQuery = "") }
      CreateTaskIntent.ParticipantPickerDismissed ->
        _state.update { it.copy(isParticipantPickerVisible = false, participantQuery = "") }
      is CreateTaskIntent.ParticipantSearchChanged ->
        _state.update { it.copy(participantQuery = intent.query) }
      is CreateTaskIntent.ParticipantToggled -> toggleParticipant(intent.userId)
      is CreateTaskIntent.PriorityChanged ->
        _state.update { it.copy(priority = intent.priority) }
      is CreateTaskIntent.DueDateChanged ->
        _state.update { it.copy(dueDate = intent.date) }
      is CreateTaskIntent.QuickDueDateSelected ->
        _state.update { it.copy(dueDate = resolveQuickDate(intent.option)) }
      CreateTaskIntent.AttachFileClicked -> pickAttachment()
      CreateTaskIntent.AttachmentRemoved -> removeAttachment()
      CreateTaskIntent.Submit -> submit()
      CreateTaskIntent.ToastDismissed -> _state.update { it.copy(errorToast = null) }
    }
  }

  private fun toggleParticipant(userId: String) {
    _state.update { current ->
      val updated = if (userId in current.participantUserIds) {
        current.participantUserIds.filterNot { it == userId }
      } else {
        current.participantUserIds + userId
      }
      current.copy(participantUserIds = updated.toImmutableList())
    }
  }

  private fun pickAttachment() {
    scope.launch {
      val picked = filePicker.pickFile() ?: return@launch
      if (picked.sizeBytes > MAX_ATTACHMENT_BYTES) {
        // Too big to upload — discard the copy the picker made and surface the limit.
        attachmentFileManager.delete(picked.localPath)
        _state.update { it.copy(attachmentError = Res.string.error_file_too_large.asUiText()) }
        return@launch
      }
      _state.update { it.copy(attachment = picked, attachmentError = null) }
    }
  }

  private fun removeAttachment() {
    _state.value.attachment?.let { attachmentFileManager.delete(it.localPath) }
    _state.update { it.copy(attachment = null, attachmentError = null) }
  }

  private fun loadMembers() {
    scope.launch {
      val params = GetAssignableMembersUseCase.Params(productionId = productionId)
      // A failure here just leaves the picker empty; task creation doesn't require an assignee.
      when (val outcome = getAssignableMembersUseCase(params)) {
        is Outcome.Success ->
          _state.update { it.copy(assignableMembers = outcome.data.mapImmutable { member -> member.toUi() }) }
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

  private fun submit() {
    val current = _state.value
    if (current.isLoading) return
    if (current.title.isBlank()) {
      _state.update { it.copy(titleError = Res.string.error_title_required.asUiText()) }
      return
    }
    _state.update { it.copy(isLoading = true, titleError = null, errorToast = null) }
    val attachment = current.attachment
    if (attachment != null) enqueueUpload(current, attachment) else createNow(current)
  }

  // With a file, creation happens in the background (survives navigation/app-kill); we just
  // hand it to the scheduler and pop back. The task appears once the upload completes.
  private fun enqueueUpload(
    current: CreateTaskState,
    attachment: PickedFile
  ) {
    scope.launch {
      val upload = PendingTaskUpload(
        uploadId = Uuid.random().toString(),
        productionId = productionId,
        title = current.title.trim(),
        description = current.description.ifBlank { null },
        dueDate = current.dueDate,
        assigneeUserId = current.assigneeUserId,
        priority = current.priority,
        participantUserIds = current.participantUserIds.toList(),
        fileName = attachment.name,
        contentType = attachment.contentType,
        localPath = attachment.localPath,
        idempotencyKey = Uuid.random().toString()
      )
      uploadScheduler.enqueue(upload)
      _state.update { it.copy(isLoading = false) }
      _events.tryEmit(CreateTaskEvent.UploadEnqueued)
    }
  }

  private fun createNow(current: CreateTaskState) {
    scope.launch {
      val params = CreateTaskUseCase.Params(
        productionId = productionId,
        title = current.title,
        description = current.description.ifBlank { null },
        dueDate = current.dueDate,
        assigneeUserId = current.assigneeUserId,
        priority = current.priority,
        participantUserIds = current.participantUserIds.toList()
      )
      when (val outcome = createTaskUseCase(params)) {
        is Outcome.Success -> {
          _state.update { it.copy(isLoading = false) }
          _events.tryEmit(CreateTaskEvent.Created(outcome.data.id))
        }
        is Outcome.Failure ->
          _state.update { it.copy(isLoading = false, errorToast = outcome.error.toUiText(errorMessages)) }
      }
    }
  }

  private fun resolveQuickDate(option: DueDateQuickOption): LocalDate {
    val today = clock.now().toLocalDateTime(timeZone).date
    // ISO week ends on Sunday (isoDayNumber 7); 0 when today already is Sunday.
    val daysUntilEndOfWeek = DAYS_IN_WEEK - today.dayOfWeek.isoDayNumber
    return when (option) {
      DueDateQuickOption.TODAY -> today
      DueDateQuickOption.TOMORROW -> today.plus(DatePeriod(days = 1))
      DueDateQuickOption.THIS_WEEK -> today.plus(DatePeriod(days = daysUntilEndOfWeek))
      DueDateQuickOption.NEXT_WEEK -> today.plus(DatePeriod(days = daysUntilEndOfWeek + DAYS_IN_WEEK))
    }
  }

  override fun onDestroy() {
    scope.cancel()
  }

  private companion object {
    const val DAYS_IN_WEEK = 7

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
