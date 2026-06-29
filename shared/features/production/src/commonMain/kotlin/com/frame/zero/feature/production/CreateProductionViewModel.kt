package com.frame.zero.feature.production

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.frame.zero.domain.DomainError
import com.frame.zero.domain.Outcome
import com.frame.zero.dto.production.CreateCrewMemberDto
import com.frame.zero.feature.production.domain.CreateProductionUseCase
import com.frame.zero.ui.UiText
import com.frame.zero.ui.asUiText
import framezero.shared.features.production.generated.resources.Res
import framezero.shared.features.production.generated.resources.error_auth_failed
import framezero.shared.features.production.generated.resources.error_conflict
import framezero.shared.features.production.generated.resources.error_email_exists
import framezero.shared.features.production.generated.resources.error_forbidden
import framezero.shared.features.production.generated.resources.error_invalid_dates
import framezero.shared.features.production.generated.resources.error_missing_dates
import framezero.shared.features.production.generated.resources.error_network
import framezero.shared.features.production.generated.resources.error_server
import framezero.shared.features.production.generated.resources.error_title_required
import framezero.shared.features.production.generated.resources.error_unknown_fallback
import kotlinx.coroutines.CoroutineScope
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
import kotlin.coroutines.CoroutineContext

internal const val DEFAULT_CREW_ROLE = "Director"

class CreateProductionViewModel(
  private val createProductionUseCase: CreateProductionUseCase,
  dispatcher: CoroutineContext = kotlinx.coroutines.Dispatchers.Main.immediate
) : InstanceKeeper.Instance {
  private val scope = CoroutineScope(dispatcher + SupervisorJob())

  private val _state = MutableStateFlow(CreateProductionState(totalSteps = TOTAL_STEPS))
  val state: StateFlow<CreateProductionState> = _state.asStateFlow()

  private val _events = MutableSharedFlow<CreateProductionEvent>(
    extraBufferCapacity = 1,
    onBufferOverflow = BufferOverflow.DROP_OLDEST
  )
  val events: SharedFlow<CreateProductionEvent> = _events.asSharedFlow()

  fun onIntent(intent: CreateProductionIntent) {
    when (intent) {
      is CreateProductionIntent.TitleChanged ->
        _state.update {
          val updated = it.copy(title = intent.title, error = null)
          updated.copy(canAdvanceStep1 = updated.computeCanAdvanceStep1())
        }
      is CreateProductionIntent.GenreChanged ->
        _state.update { it.copy(genre = intent.genre) }
      is CreateProductionIntent.LoglineChanged ->
        _state.update { it.copy(logline = intent.logline) }
      is CreateProductionIntent.StartDateChanged ->
        _state.update {
          val updated = it.copy(startDate = intent.date, error = null)
          updated.copy(canAdvanceStep1 = updated.computeCanAdvanceStep1())
        }
      is CreateProductionIntent.WrapDateChanged ->
        _state.update {
          val updated = it.copy(wrapDate = intent.date, error = null)
          updated.copy(canAdvanceStep1 = updated.computeCanAdvanceStep1())
        }
      is CreateProductionIntent.BudgetChanged ->
        _state.update {
          it.copy(budgetCents = intent.budgetCents, budgetDisplay = intent.budgetCents?.let(::formatBudget))
        }
      is CreateProductionIntent.CrewNameChanged ->
        _state.update { it.copy(crewNameInput = intent.name) }
      is CreateProductionIntent.CrewRoleChanged ->
        _state.update { it.copy(crewRoleInput = intent.role) }
      CreateProductionIntent.AddCrewMember -> addCrewMember()
      is CreateProductionIntent.RemoveCrewMember ->
        _state.update {
          it.copy(crewMembers = it.crewMembers.filterIndexed { i, _ -> i != intent.index })
        }
      CreateProductionIntent.NextStep -> nextStep()
      CreateProductionIntent.PreviousStep ->
        _state.update {
          if (it.currentStep > 1) {
            it.copy(currentStep = it.currentStep - 1, error = null)
          } else {
            it
          }
        }
      CreateProductionIntent.Submit -> submit()
      CreateProductionIntent.ToastDismissed -> _state.update { it.copy(errorToast = null) }
      CreateProductionIntent.BackPressed -> onBackPressed()
    }
  }

  private fun onBackPressed() {
    if (_state.value.currentStep > 1) {
      _state.update { it.copy(currentStep = it.currentStep - 1, error = null) }
    } else {
      _events.tryEmit(CreateProductionEvent.Dismissed)
    }
  }

  private fun addCrewMember() {
    _state.update {
      val name = it.crewNameInput.trim()
      if (name.isBlank()) {
        it
      } else {
        it.copy(
          crewMembers = it.crewMembers + CrewMemberEntry(name = name, role = it.crewRoleInput),
          crewNameInput = "",
          crewRoleInput = DEFAULT_CREW_ROLE
        )
      }
    }
  }

  private fun nextStep() {
    val current = _state.value
    when (current.currentStep) {
      1 -> {
        if (!current.canAdvanceStep1) {
          val error = if (current.title.isBlank()) {
            Res.string.error_title_required.asUiText()
          } else {
            Res.string.error_invalid_dates.asUiText()
          }
          _state.update { it.copy(error = error) }
          return
        }
        _state.update { it.copy(currentStep = 2, error = null) }
      }
      2 -> _state.update { it.copy(currentStep = 3, error = null) }
      3 -> submit()
    }
  }

  private fun submit() {
    _state.update {
      if (it.isLoading) return
      it.copy(isLoading = true, error = null)
    }
    val current = _state.value
    val start = current.startDate
    val wrap = current.wrapDate
    if (start == null || wrap == null) {
      _state.update {
        it.copy(isLoading = false, error = Res.string.error_missing_dates.asUiText())
      }
      return
    }
    scope.launch {
      val params = CreateProductionUseCase.Params(
        title = current.title.trim(),
        genre = current.genre,
        logline = current.logline.ifBlank { null },
        startDate = start,
        wrapDate = wrap,
        budgetCents = current.budgetCents,
        crew = current.crewMembers.map { CreateCrewMemberDto(name = it.name, role = it.role) }
      )
      when (val outcome = createProductionUseCase(params)) {
        is Outcome.Success -> {
          _state.update { it.copy(isLoading = false) }
          _events.tryEmit(CreateProductionEvent.Created)
        }
        is Outcome.Failure -> _state.update {
          val message = outcome.error.toUiText()
          // Network/server failures are transient → toast; validation/auth errors are
          // user-fixable → inline. Mirrors the auth feature's split.
          if (outcome.error.isOfflineOrServerError) {
            it.copy(isLoading = false, errorToast = message)
          } else {
            it.copy(isLoading = false, error = message)
          }
        }
      }
    }
  }

  private val DomainError.isOfflineOrServerError: Boolean
    get() = this is DomainError.Offline || this is DomainError.Server || this is DomainError.Unknown

  private fun DomainError.toUiText(): UiText =
    when (this) {
      is DomainError.Offline -> Res.string.error_network.asUiText()
      is DomainError.Server -> Res.string.error_server.asUiText()
      DomainError.Forbidden -> Res.string.error_forbidden.asUiText()
      DomainError.Conflict -> Res.string.error_conflict.asUiText()
      DomainError.InvalidCredentials -> Res.string.error_auth_failed.asUiText()
      DomainError.EmailAlreadyExists -> Res.string.error_email_exists.asUiText()
      DomainError.NotFound,
      DomainError.InsufficientStorage,
      is DomainError.Unknown -> Res.string.error_unknown_fallback.asUiText()
    }

  private fun formatBudget(cents: Long): String {
    val dollars = cents / 100
    val prefix = if (dollars < 0) "-$" else "$"
    val absStr = kotlin.math.abs(dollars).toString()
    val formatted = absStr.reversed().chunked(3).joinToString(",").reversed()
    return "$prefix$formatted"
  }

  private fun CreateProductionState.computeCanAdvanceStep1(): Boolean =
    title.isNotBlank() &&
      startDate != null &&
      wrapDate != null &&
      wrapDate.toEpochDays() > startDate.toEpochDays()

  override fun onDestroy() {
    scope.cancel()
  }

  private companion object {
    const val TOTAL_STEPS = 3
  }
}
