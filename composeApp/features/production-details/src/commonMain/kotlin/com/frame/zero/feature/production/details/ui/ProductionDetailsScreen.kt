package com.frame.zero.feature.production.details.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import com.frame.zero.core.collections.mapImmutable
import com.frame.zero.domain.production.ProductionPhase
import com.frame.zero.feature.production.details.ProductionDetailUi
import com.frame.zero.feature.production.details.ProductionDetailsComponent
import com.frame.zero.feature.production.details.ProductionDetailsIntent
import com.frame.zero.feature.production.details.ProductionDetailsState
import com.frame.zero.feature.production.details.ProductionMemberUi
import com.frame.zero.feature.production.details.ProductionPipelinePhaseUi
import com.frame.zero.feature.production.details.ProductionTaskUi
import com.frame.zero.feature.production.details.ViewerCrewUi
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.widgets.FullScreenError
import com.frame.zero.shared.design_system.widgets.OverflowMenu
import com.frame.zero.shared.design_system.widgets.OverflowMenuItem
import com.frame.zero.shared.design_system.widgets.TopToolbar
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import com.frame.zero.ui.asString
import kotlinx.collections.immutable.persistentListOf
import framezero.composeapp.features.production_details.generated.resources.Res
import framezero.composeapp.features.production_details.generated.resources.delete_production_menu
import org.jetbrains.compose.resources.stringResource

private const val OverlayAlpha = 0.6f

@Composable
fun ProductionDetailsScreen(component: ProductionDetailsComponent) {
  val state by component.state.collectAsStateWithLifecycle()
  ProductionDetailsContent(
    state = state,
    onBack = component.onBack,
    onIntent = component::onIntent,
    onAddTask = component::requestAddTask
  )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ProductionDetailsContent(
  state: ProductionDetailsState,
  onBack: () -> Unit,
  onIntent: (ProductionDetailsIntent) -> Unit,
  modifier: Modifier = Modifier,
  onAddTask: () -> Unit = {}
) {
  Box(
    modifier = modifier
      .fillMaxSize()
      .background(AppTheme.colorSystem.background)
      .systemBarsPadding()
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      TopToolbar(
        title = state.detail?.title.orEmpty(),
        onBack = onBack,
        trailingContent = {
          OverflowMenu(
            items = persistentListOf(
              OverflowMenuItem(
                text = stringResource(Res.string.delete_production_menu),
                isDestructive = true,
                onClick = { onIntent(ProductionDetailsIntent.DeleteRequested) }
              )
            )
          )
        }
      )

      val loadError = state.error
      when {
        state.isLoading && state.detail == null -> CenteredProgress()
        loadError != null && state.detail == null ->
          FullScreenError(
            message = loadError.asString(),
            onRetry = { onIntent(ProductionDetailsIntent.Refresh) }
          )
        state.detail != null ->
          DetailBody(detail = state.detail!!, tasks = state.tasks, onAddTask = onAddTask)
        else -> Box(modifier = Modifier.fillMaxSize())
      }
    }

    if (state.isDeleting) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            AppTheme.colorSystem.background.copy(alpha = OverlayAlpha)
          ),
        contentAlignment = Alignment.Center
      ) {
        LoadingIndicator(color = AppTheme.colorSystem.accent)
      }
    }
  }

  if (state.isDeleteDialogVisible) {
    DeleteConfirmDialog(
      title = state.detail?.title.orEmpty(),
      onConfirm = { onIntent(ProductionDetailsIntent.DeleteConfirmed) },
      onDismiss = { onIntent(ProductionDetailsIntent.DeleteDismissed) }
    )
  }

  state.deleteError?.let { message ->
    DeleteErrorDialog(
      message = message.asString(),
      onDismiss = { onIntent(ProductionDetailsIntent.DeleteErrorDismissed) }
    )
  }
}

@Composable
private fun DetailBody(
  detail: ProductionDetailUi,
  tasks: List<ProductionTaskUi>,
  modifier: Modifier = Modifier,
  onAddTask: () -> Unit = {}
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
  ) {
    LoglineCard(logline = detail.logline, detail = detail)
    VerticalSpacer(AppTheme.spacingSystem.space16)
    PipelineCard(
      pipeline = detail.pipeline,
      currentPhase = detail.phase
    )
    VerticalSpacer(AppTheme.spacingSystem.space16)
    DateCards(startDate = detail.startDateLabel, wrapDate = detail.wrapDateLabel)
    VerticalSpacer(AppTheme.spacingSystem.space16)
    detail.viewerCrew?.let { viewerCrew ->
      TeamCard(viewerCrew = viewerCrew)
      VerticalSpacer(AppTheme.spacingSystem.space16)
    }
    TasksCard(tasks = tasks, onAddTask = onAddTask)
    VerticalSpacer(AppTheme.spacingSystem.space24)
  }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CenteredProgress(modifier: Modifier = Modifier) {
  Box(
    modifier = modifier.fillMaxSize(),
    contentAlignment = Alignment.Center
  ) {
    LoadingIndicator(color = AppTheme.colorSystem.accent)
  }
}

@LightDarkPreview
@Composable
private fun ProductionDetailsLoadedPreview() {
  AppTheme {
    ProductionDetailsContent(
      state = ProductionDetailsState(
        tasks = persistentListOf(
          ProductionTaskUi(id = "1", title = "Lock shooting schedule", dueDateLabel = "Apr 12", isDone = false),
          ProductionTaskUi(id = "2", title = "Send call sheets", dueDateLabel = null, isDone = true)
        ),
        detail = ProductionDetailUi(
          title = "Echoes of Silence",
          logline = "A deaf composer rediscovers sound through" +
            " the chaos of war.",
          phase = ProductionPhase.PRODUCTION,
          progressPercent = 68,
          daysLeft = 24,
          membersCount = 12,
          budgetLabel = "$2,400,000",
          startDateLabel = "Feb 10, 2026",
          wrapDateLabel = "Aug 30, 2026",
          pipeline = ProductionPhase.entries.mapImmutable { p ->
            ProductionPipelinePhaseUi(
              phase = p,
              label = p.displayLabel(),
              isCompleted =
                p.ordinal < ProductionPhase.PRODUCTION.ordinal,
              isCurrent = p == ProductionPhase.PRODUCTION
            )
          },
          viewerCrew = ViewerCrewUi(
            viewerRole = "Producer",
            manager = ProductionMemberUi(
              id = "m1",
              name = "Maya Rivera",
              role = "Director",
              initials = "MR",
              avatarColorHex = "#E91E63"
            ),
            peers = persistentListOf(
              ProductionMemberUi(
                id = "m3",
                name = "Sara Lin",
                role = "DP",
                initials = "SL",
                avatarColorHex = "#9C27B0"
              )
            ),
            reports = persistentListOf(
              ProductionMemberUi(
                id = "m4",
                name = "Jake Morse",
                role = "1st AD",
                initials = "JM",
                avatarColorHex = "#009688"
              )
            )
          )
        )
      ),
      onBack = {},
      onIntent = {}
    )
  }
}
