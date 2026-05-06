package com.frame.zero.feature.production

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.frame.zero.domain.Outcome
import com.frame.zero.feature.production.domain.CreateProductionUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class CreateProductionViewModel(
  private val createProductionUseCase: CreateProductionUseCase,
  dispatcher: CoroutineContext = Dispatchers.Main
) : InstanceKeeper.Instance {
  private val scope = CoroutineScope(dispatcher + SupervisorJob())

  private val _state = MutableStateFlow(CreateProductionState())
  val state: StateFlow<CreateProductionState> = _state.asStateFlow()

  fun onIntent(intent: CreateProductionIntent) {
    when (intent) {
      is CreateProductionIntent.TitleChanged ->
        _state.update { it.copy(title = intent.title, error = null) }
      is CreateProductionIntent.GenreChanged ->
        _state.update { it.copy(genre = intent.genre) }
      is CreateProductionIntent.PhaseChanged ->
        _state.update { it.copy(phase = intent.phase) }
      is CreateProductionIntent.LoglineChanged ->
        _state.update { it.copy(logline = intent.logline) }
      is CreateProductionIntent.StartDateChanged ->
        _state.update { it.copy(startDate = intent.date, error = null) }
      is CreateProductionIntent.WrapDateChanged ->
        _state.update { it.copy(wrapDate = intent.date, error = null) }
      CreateProductionIntent.Submit -> submit()
    }
  }

  private fun submit() {
    val current = _state.value
    if (current.isLoading) return
    if (current.title.isBlank()) {
      _state.update { it.copy(error = "Title is required") }
      return
    }
    val start = current.startDate
    val wrap = current.wrapDate
    if (start == null || wrap == null) {
      _state.update { it.copy(error = "Start and wrap dates are required") }
      return
    }
    if (!wrap.toEpochDays().let { w -> start.toEpochDays() < w }) {
      _state.update { it.copy(error = "Wrap date must be after start date") }
      return
    }
    scope.launch {
      _state.update { it.copy(isLoading = true, error = null) }
      val params = CreateProductionUseCase.Params(
        title = current.title.trim(),
        genre = current.genre,
        phase = current.phase,
        logline = current.logline.ifBlank { null },
        startDate = start,
        wrapDate = wrap
      )
      when (val outcome = createProductionUseCase(params)) {
        is Outcome.Success -> _state.update { it.copy(isLoading = false, isSuccess = true) }
        is Outcome.Failure -> _state.update { it.copy(isLoading = false, error = outcome.error.toString()) }
      }
    }
  }

  override fun onDestroy() {
    scope.cancel()
  }
}
