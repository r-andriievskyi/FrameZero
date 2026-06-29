package com.frame.zero.feature.production.details

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.frame.zero.core.collections.mapImmutable
import com.frame.zero.core.format.formatMedium
import com.frame.zero.domain.DomainError
import com.frame.zero.domain.Outcome
import com.frame.zero.domain.production.ProductionDetail
import com.frame.zero.domain.production.ProductionMember
import com.frame.zero.domain.production.ProductionPipelinePhase
import com.frame.zero.domain.production.ViewerCrew
import com.frame.zero.feature.production.details.domain.DeleteProductionUseCase
import com.frame.zero.feature.production.details.domain.GetProductionDetailsUseCase
import com.frame.zero.feature.production.details.domain.GetProductionTasksUseCase
import com.frame.zero.feature.production.details.domain.ProductionTask
import com.frame.zero.ui.UiText
import com.frame.zero.ui.asUiText
import framezero.shared.features.production_details.generated.resources.Res
import framezero.shared.features.production_details.generated.resources.error_auth_failed
import framezero.shared.features.production_details.generated.resources.error_conflict
import framezero.shared.features.production_details.generated.resources.error_forbidden
import framezero.shared.features.production_details.generated.resources.error_network
import framezero.shared.features.production_details.generated.resources.error_not_found
import framezero.shared.features.production_details.generated.resources.error_server
import framezero.shared.features.production_details.generated.resources.error_unknown_fallback
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
import kotlin.coroutines.CoroutineContext

class ProductionDetailsViewModel(
  private val productionId: String,
  private val getProductionDetailsUseCase: GetProductionDetailsUseCase,
  private val getProductionTasksUseCase: GetProductionTasksUseCase,
  private val deleteProductionUseCase: DeleteProductionUseCase,
  dispatcher: CoroutineContext = Dispatchers.Main.immediate
) : InstanceKeeper.Instance {
  private val scope = CoroutineScope(dispatcher + SupervisorJob())

  private val _state = MutableStateFlow(ProductionDetailsState())
  val state: StateFlow<ProductionDetailsState> = _state.asStateFlow()

  private val _events =
    MutableSharedFlow<ProductionDetailsEvent>(
      extraBufferCapacity = 1,
      onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
  val events: SharedFlow<ProductionDetailsEvent> = _events.asSharedFlow()

  init {
    load()
    loadTasks()
  }

  fun onIntent(intent: ProductionDetailsIntent) {
    when (intent) {
      ProductionDetailsIntent.Refresh -> load()
      ProductionDetailsIntent.RefreshTasks -> loadTasks()
      ProductionDetailsIntent.AddTaskRequested -> requestAddTask()
      ProductionDetailsIntent.DeleteRequested ->
        _state.update { it.copy(isDeleteDialogVisible = true, deleteError = null) }
      ProductionDetailsIntent.DeleteDismissed ->
        _state.update { it.copy(isDeleteDialogVisible = false) }
      ProductionDetailsIntent.DeleteConfirmed -> deleteProduction()
      ProductionDetailsIntent.DeleteErrorDismissed ->
        _state.update { it.copy(deleteError = null) }
    }
  }

  private fun requestAddTask() {
    val detail = _state.value.detail ?: return
    _events.tryEmit(ProductionDetailsEvent.AddTaskRequested(productionId, detail.title))
  }

  private fun load() {
    scope.launch {
      _state.update { it.copy(isLoading = true, error = null) }
      val params = GetProductionDetailsUseCase.Params(productionId = productionId)
      when (val outcome = getProductionDetailsUseCase(params)) {
        is Outcome.Success ->
          _state.update { it.copy(isLoading = false, detail = outcome.data.toUi()) }
        is Outcome.Failure ->
          _state.update { it.copy(isLoading = false, error = outcome.error.toUiText()) }
      }
    }
  }

  private fun loadTasks() {
    if (_state.value.areTasksLoading) return
    scope.launch {
      _state.update { it.copy(areTasksLoading = true) }
      val params = GetProductionTasksUseCase.Params(productionId = productionId)
      when (val outcome = getProductionTasksUseCase(params)) {
        is Outcome.Success -> _state.update {
          it.copy(areTasksLoading = false, tasks = outcome.data.mapImmutable { task -> task.toUi() })
        }
        is Outcome.Failure -> _state.update { it.copy(areTasksLoading = false) }
      }
    }
  }

  private fun ProductionTask.toUi(): ProductionTaskUi =
    ProductionTaskUi(
      id = id,
      title = title,
      dueDateLabel = dueDate?.formatMedium(),
      isDone = isDone
    )

  private fun ProductionDetail.toUi(): ProductionDetailUi =
    ProductionDetailUi(
      title = title,
      logline = logline,
      phase = phase,
      progressPercent = progressPercent,
      daysLeft = daysLeft,
      membersCount = membersCount,
      budgetLabel = formatBudget(budgetCents),
      startDateLabel = startDate.formatMedium(),
      wrapDateLabel = wrapDate.formatMedium(),
      pipeline = pipeline.mapImmutable { it.toUi() },
      viewerCrew = viewerCrew?.toUi()
    )

  private fun ProductionPipelinePhase.toUi(): ProductionPipelinePhaseUi =
    ProductionPipelinePhaseUi(
      phase = phase,
      label = label,
      isCompleted = isCompleted,
      isCurrent = isCurrent
    )

  private fun ViewerCrew.toUi(): ViewerCrewUi =
    ViewerCrewUi(
      viewerRole = viewer.role,
      manager = manager?.toUi(),
      peers = peers.mapImmutable { it.toUi() },
      reports = reports.mapImmutable { it.toUi() }
    )

  private fun ProductionMember.toUi(): ProductionMemberUi =
    ProductionMemberUi(
      id = id,
      name = name,
      role = role,
      initials = initials,
      avatarColorHex = avatarColorHex
    )

  private fun formatBudget(cents: Long?): String {
    if (cents == null) return "—"
    val dollars = cents / 100
    return "$${
      dollars.toString().reversed().chunked(3)
        .joinToString(",").reversed()
    }"
  }

  private fun deleteProduction() {
    if (_state.value.isDeleting) return
    scope.launch {
      _state.update {
        it.copy(isDeleting = true, isDeleteDialogVisible = false, deleteError = null)
      }
      val params = DeleteProductionUseCase.Params(productionId = productionId)
      when (val outcome = deleteProductionUseCase(params)) {
        is Outcome.Success -> {
          _state.update { it.copy(isDeleting = false) }
          _events.tryEmit(ProductionDetailsEvent.Deleted(productionId))
        }
        is Outcome.Failure ->
          _state.update {
            it.copy(isDeleting = false, deleteError = outcome.error.toUiText())
          }
      }
    }
  }

  override fun onDestroy() {
    scope.cancel()
  }

  private fun DomainError.toUiText(): UiText =
    when (this) {
      is DomainError.Offline -> Res.string.error_network.asUiText()
      is DomainError.Server -> Res.string.error_server.asUiText()
      DomainError.NotFound -> Res.string.error_not_found.asUiText()
      DomainError.Forbidden -> Res.string.error_forbidden.asUiText()
      DomainError.Conflict -> Res.string.error_conflict.asUiText()
      DomainError.InvalidCredentials -> Res.string.error_auth_failed.asUiText()
      DomainError.EmailAlreadyExists,
      DomainError.InsufficientStorage,
      is DomainError.Unknown -> Res.string.error_unknown_fallback.asUiText()
    }
}
