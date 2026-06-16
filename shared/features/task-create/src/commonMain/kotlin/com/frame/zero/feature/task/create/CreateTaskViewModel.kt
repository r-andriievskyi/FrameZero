package com.frame.zero.feature.task.create

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.frame.zero.domain.DomainError
import com.frame.zero.domain.Outcome
import com.frame.zero.feature.task.create.domain.CreateTaskUseCase
import com.frame.zero.feature.task.create.domain.GetAssignableMembersUseCase
import com.frame.zero.ui.UiText
import com.frame.zero.ui.asUiText
import framezero.shared.features.task_create.generated.resources.Res
import framezero.shared.features.task_create.generated.resources.error_auth_failed
import framezero.shared.features.task_create.generated.resources.error_conflict
import framezero.shared.features.task_create.generated.resources.error_forbidden
import framezero.shared.features.task_create.generated.resources.error_network
import framezero.shared.features.task_create.generated.resources.error_not_found
import framezero.shared.features.task_create.generated.resources.error_server
import framezero.shared.features.task_create.generated.resources.error_title_required
import framezero.shared.features.task_create.generated.resources.error_unknown_fallback
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
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.coroutines.CoroutineContext
import kotlin.time.Clock

class CreateTaskViewModel(
  private val productionId: String,
  productionTitle: String,
  private val createTaskUseCase: CreateTaskUseCase,
  private val getAssignableMembersUseCase: GetAssignableMembersUseCase,
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
      is CreateTaskIntent.PriorityChanged ->
        _state.update { it.copy(priority = intent.priority) }
      is CreateTaskIntent.DueDateChanged ->
        _state.update { it.copy(dueDate = intent.date) }
      is CreateTaskIntent.QuickDueDateSelected ->
        _state.update { it.copy(dueDate = resolveQuickDate(intent.option)) }
      CreateTaskIntent.Submit -> submit()
      CreateTaskIntent.ToastDismissed -> _state.update { it.copy(errorToast = null) }
    }
  }

  private fun loadMembers() {
    scope.launch {
      val params = GetAssignableMembersUseCase.Params(productionId = productionId)
      // A failure here just leaves the picker empty; task creation doesn't require an assignee.
      when (val outcome = getAssignableMembersUseCase(params)) {
        is Outcome.Success -> _state.update { it.copy(assignableMembers = outcome.data) }
        is Outcome.Failure -> Unit
      }
    }
  }

  private fun submit() {
    val current = _state.value
    if (current.isLoading) return
    if (current.title.isBlank()) {
      _state.update { it.copy(titleError = Res.string.error_title_required.asUiText()) }
      return
    }
    _state.update { it.copy(isLoading = true, titleError = null, errorToast = null) }
    scope.launch {
      val params = CreateTaskUseCase.Params(
        productionId = productionId,
        title = current.title,
        description = current.description.ifBlank { null },
        dueDate = current.dueDate,
        assigneeUserId = current.assigneeUserId,
        priority = current.priority
      )
      when (val outcome = createTaskUseCase(params)) {
        is Outcome.Success -> {
          _state.update { it.copy(isLoading = false) }
          _events.tryEmit(CreateTaskEvent.Created(outcome.data.id))
        }
        is Outcome.Failure ->
          _state.update { it.copy(isLoading = false, errorToast = outcome.error.toUiText()) }
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

  private fun DomainError.toUiText(): UiText =
    when (this) {
      is DomainError.Network -> Res.string.error_network.asUiText()
      is DomainError.Server -> Res.string.error_server.asUiText()
      DomainError.NotFound -> Res.string.error_not_found.asUiText()
      DomainError.Forbidden -> Res.string.error_forbidden.asUiText()
      DomainError.Conflict -> Res.string.error_conflict.asUiText()
      DomainError.InvalidCredentials -> Res.string.error_auth_failed.asUiText()
      DomainError.EmailAlreadyExists,
      is DomainError.Unknown -> Res.string.error_unknown_fallback.asUiText()
    }

  private companion object {
    const val DAYS_IN_WEEK = 7
  }
}
