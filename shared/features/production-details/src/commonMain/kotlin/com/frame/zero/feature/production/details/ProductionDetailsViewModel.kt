package com.frame.zero.feature.production.details

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.frame.zero.core.collections.mapImmutable
import com.frame.zero.core.format.formatCurrencyUsdCents
import com.frame.zero.core.format.formatMedium
import com.frame.zero.core.upload.PendingUploadStatus
import com.frame.zero.core.upload.PendingUploadStore
import com.frame.zero.core.upload.TaskUploadScheduler
import com.frame.zero.domain.Outcome
import com.frame.zero.domain.production.ProductionDetail
import com.frame.zero.domain.production.ProductionMember
import com.frame.zero.domain.production.ProductionPipelinePhase
import com.frame.zero.domain.production.ViewerCrew
import com.frame.zero.feature.production.details.domain.DeleteProductionUseCase
import com.frame.zero.feature.production.details.domain.GetProductionDetailsUseCase
import com.frame.zero.feature.production.details.domain.GetProductionTasksUseCase
import com.frame.zero.feature.production.details.domain.ProductionTask
import com.frame.zero.ui.DomainErrorCategory
import com.frame.zero.ui.UiText
import com.frame.zero.ui.asUiText
import com.frame.zero.ui.toUiText
import framezero.shared.features.production_details.generated.resources.Res
import framezero.shared.features.production_details.generated.resources.error_conflict
import framezero.shared.features.production_details.generated.resources.error_forbidden
import framezero.shared.features.production_details.generated.resources.error_not_found
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class ProductionDetailsViewModel(
  private val productionId: String,
  private val getProductionDetailsUseCase: GetProductionDetailsUseCase,
  private val getProductionTasksUseCase: GetProductionTasksUseCase,
  private val deleteProductionUseCase: DeleteProductionUseCase,
  private val pendingUploadStore: PendingUploadStore,
  private val taskUploadScheduler: TaskUploadScheduler,
  dispatcher: CoroutineContext = Dispatchers.Main.immediate
) : InstanceKeeper.Instance {
  private val scope = CoroutineScope(dispatcher + SupervisorJob())

  private val _state = MutableStateFlow(ProductionDetailsState())
  val state: StateFlow<ProductionDetailsState> = _state.asStateFlow()

  private val _events = MutableSharedFlow<ProductionDetailsEvent>(
    extraBufferCapacity = 1,
    onBufferOverflow = BufferOverflow.DROP_OLDEST
  )
  val events: SharedFlow<ProductionDetailsEvent> = _events.asSharedFlow()

  init {
    load()
    loadTasks()
    observePendingUpload()
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

      ProductionDetailsIntent.RetryUploadRequested -> retryUpload()
      ProductionDetailsIntent.DismissUploadRequested -> dismissUpload()
    }
  }

  /** At most one upload is ever shown — the pending queue is a single background job per
   *  task-create, not a list a user manages, so the most recent one for this production is
   *  what "did my task save?" actually asks. */
  private fun observePendingUpload() {
    scope.launch {
      pendingUploadStore.uploads
        .map { uploads -> uploads.filter { it.productionId == productionId }.maxByOrNull { it.createdAtMillis } }
        .collect { upload ->
          _state.update {
            it.copy(
              pendingUpload = upload?.let { pending ->
                PendingUploadUi(
                  uploadId = pending.uploadId,
                  taskTitle = pending.title,
                  isFailed = pending.status == PendingUploadStatus.Failed
                )
              }
            )
          }
        }
    }
  }

  private fun retryUpload() {
    val uploadId = _state.value.pendingUpload?.uploadId ?: return
    scope.launch { taskUploadScheduler.retry(uploadId) }
  }

  private fun dismissUpload() {
    val uploadId = _state.value.pendingUpload?.uploadId ?: return
    scope.launch { taskUploadScheduler.cancel(uploadId) }
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
          _state.update { it.copy(isLoading = false, error = outcome.error.toUiText(errorMessages)) }
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

  private fun formatBudget(cents: Long?): String = if (cents == null) "—" else formatCurrencyUsdCents(cents)

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
            it.copy(isDeleting = false, deleteError = outcome.error.toUiText(errorMessages))
          }
      }
    }
  }

  override fun onDestroy() {
    scope.cancel()
  }

  private companion object {
    val errorMessages: Map<DomainErrorCategory, UiText> = mapOf(
      DomainErrorCategory.NOT_FOUND to Res.string.error_not_found.asUiText(),
      DomainErrorCategory.FORBIDDEN to Res.string.error_forbidden.asUiText(),
      DomainErrorCategory.CONFLICT to Res.string.error_conflict.asUiText()
    )
  }
}
