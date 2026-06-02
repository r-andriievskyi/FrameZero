package com.frame.zero.feature.production.details.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import com.frame.zero.domain.production.Genre
import com.frame.zero.domain.production.ProductionDetail
import com.frame.zero.domain.production.ProductionMember
import com.frame.zero.domain.production.ProductionPhase
import com.frame.zero.domain.production.ProductionPipelinePhase
import com.frame.zero.domain.production.ViewerCrew
import com.frame.zero.feature.production.details.ProductionDetailsComponent
import com.frame.zero.feature.production.details.ProductionDetailsIntent
import com.frame.zero.feature.production.details.ProductionDetailsState
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.widgets.OverflowMenu
import com.frame.zero.shared.design_system.widgets.OverflowMenuItem
import com.frame.zero.shared.design_system.widgets.TopToolbar
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import framezero.composeapp.features.production_details.generated.resources.Res
import framezero.composeapp.features.production_details.generated.resources.delete_production_menu
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant

private const val OverlayAlpha = 0.6f

@Composable
fun ProductionDetailsScreen(component: ProductionDetailsComponent) {
  val state by component.state.collectAsStateWithLifecycle()
  ProductionDetailsContent(
    state = state,
    onBack = component.onBack,
    onIntent = component::onIntent
  )
}

@Composable
private fun ProductionDetailsContent(
  state: ProductionDetailsState,
  onBack: () -> Unit,
  onIntent: (ProductionDetailsIntent) -> Unit,
  modifier: Modifier = Modifier
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
            items = listOf(
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
        loadError != null && state.detail == null -> CenteredMessage(loadError)
        state.detail != null -> DetailBody(detail = state.detail!!)
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
        CircularProgressIndicator(color = AppTheme.colorSystem.accent)
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
      message = message,
      onDismiss = { onIntent(ProductionDetailsIntent.DeleteErrorDismissed) }
    )
  }
}

@Composable
private fun DetailBody(
  detail: ProductionDetail,
  modifier: Modifier = Modifier
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
    DateCards(startDate = detail.startDate, wrapDate = detail.wrapDate)
    VerticalSpacer(AppTheme.spacingSystem.space16)
    detail.viewerCrew?.let { viewerCrew ->
      TeamCard(viewerCrew = viewerCrew)
      VerticalSpacer(AppTheme.spacingSystem.space24)
    }
  }
}

@Composable
private fun CenteredProgress(modifier: Modifier = Modifier) {
  Box(
    modifier = modifier.fillMaxSize(),
    contentAlignment = Alignment.Center
  ) {
    CircularProgressIndicator(color = AppTheme.colorSystem.accent)
  }
}

@Composable
private fun CenteredMessage(
  message: String,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxSize()
      .padding(AppTheme.spacingSystem.space16),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = message,
      style = AppTheme.typographySystem.bodyMedium,
      color = AppTheme.colorSystem.textSecondary
    )
  }
}

private val PreviewInstant = Instant.fromEpochMilliseconds(0L)

@LightDarkPreview
@Composable
private fun ProductionDetailsLoadedPreview() {
  AppTheme {
    ProductionDetailsContent(
      state = ProductionDetailsState(
        detail = ProductionDetail(
          id = "1",
          title = "Echoes of Silence",
          genre = Genre.DRAMA,
          logline = "A deaf composer rediscovers sound through" +
            " the chaos of war.",
          phase = ProductionPhase.PRODUCTION,
          progressPercent = 68,
          daysLeft = 24,
          startDate = LocalDate(2026, 2, 10),
          wrapDate = LocalDate(2026, 8, 30),
          budgetCents = 240_000_000L,
          membersCount = 12,
          keyCrew = emptyList(),
          pipeline = ProductionPhase.entries.map { p ->
            ProductionPipelinePhase(
              phase = p,
              label = p.displayLabel(),
              isCompleted =
                p.ordinal < ProductionPhase.PRODUCTION.ordinal,
              isCurrent = p == ProductionPhase.PRODUCTION
            )
          },
          createdAt = PreviewInstant,
          updatedAt = PreviewInstant,
          viewerCrew = ViewerCrew(
            viewer = ProductionMember(
              id = "m2",
              userId = "u-me",
              name = "Tom Ellison",
              role = "Producer",
              initials = "TE",
              avatarColorHex = "#2196F3",
              addedAt = PreviewInstant,
              reportsToMemberId = "m1"
            ),
            manager = ProductionMember(
              id = "m1",
              userId = null,
              name = "Maya Rivera",
              role = "Director",
              initials = "MR",
              avatarColorHex = "#E91E63",
              addedAt = PreviewInstant,
              reportsToMemberId = null
            ),
            peers = listOf(
              ProductionMember(
                id = "m3",
                userId = null,
                name = "Sara Lin",
                role = "DP",
                initials = "SL",
                avatarColorHex = "#9C27B0",
                addedAt = PreviewInstant,
                reportsToMemberId = "m1"
              )
            ),
            reports = listOf(
              ProductionMember(
                id = "m4",
                userId = null,
                name = "Jake Morse",
                role = "1st AD",
                initials = "JM",
                avatarColorHex = "#009688",
                addedAt = PreviewInstant,
                reportsToMemberId = "m2"
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
