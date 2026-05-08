package com.frame.zero.feature.home.ui.tab

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.discovery.playground.shared.design_system.AppTheme
import com.discovery.playground.shared.design_system.widgets.VerticalSpacer
import com.frame.zero.domain.production.Genre
import com.frame.zero.domain.production.Production
import com.frame.zero.domain.production.ProductionPhase
import com.frame.zero.dto.production.AccentColorHint
import com.frame.zero.feature.home.tab.projects.ProjectsTabComponent
import com.frame.zero.feature.home.tab.projects.ProjectsTabState
import framezero.composeapp.features.home.generated.resources.Res
import framezero.composeapp.features.home.generated.resources.days_left
import framezero.composeapp.features.home.generated.resources.members_count
import framezero.composeapp.features.home.generated.resources.projects_create_button
import framezero.composeapp.features.home.generated.resources.projects_empty_description
import framezero.composeapp.features.home.generated.resources.projects_empty_invite
import framezero.composeapp.features.home.generated.resources.projects_empty_title
import framezero.composeapp.features.home.generated.resources.projects_filter_all
import framezero.composeapp.features.home.generated.resources.projects_filter_post_production
import framezero.composeapp.features.home.generated.resources.projects_filter_pre_production
import framezero.composeapp.features.home.generated.resources.projects_filter_production
import framezero.composeapp.features.home.generated.resources.projects_pipeline_progress
import framezero.composeapp.features.home.generated.resources.projects_title
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProjectsTabContent(component: ProjectsTabComponent) {
  LaunchedEffect(Unit) { component.onAppeared() }
  val state by component.state.collectAsState()
  ProjectsContent(state = state, onCreateProductionClick = component.onCreateProductionClick)
}

@Composable
private fun ProjectsContent(
  state: ProjectsTabState,
  onCreateProductionClick: () -> Unit
) {
  var selectedFilter by remember { mutableStateOf<ProductionPhase?>(null) }

  val filteredProductions = if (selectedFilter == null) {
    state.productions
  } else {
    state.productions.filter { it.phase == selectedFilter }
  }

  Column(
    modifier =
      Modifier
        .fillMaxSize()
        .background(AppTheme.colorSystem.background)
        .verticalScroll(rememberScrollState())
        .padding(
          horizontal = AppTheme.spacingSystem.space16,
          vertical = AppTheme.spacingSystem.space24
        )
  ) {
    // Header row
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = stringResource(Res.string.projects_title),
        style = AppTheme.typographySystem.displayMedium,
        color = AppTheme.colorSystem.textPrimary
      )
      if (filteredProductions.isNotEmpty()) {
        Box(
          modifier =
            Modifier
              .size(40.dp)
              .clip(RoundedCornerShape(AppTheme.radiusSystem.radiusMax))
              .background(AppTheme.colorSystem.accent)
              .clickable(onClick = onCreateProductionClick),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "+",
            style = AppTheme.typographySystem.titleLarge,
            color = AppTheme.colorSystem.textOnAccent
          )
        }
      }
    }

    VerticalSpacer(AppTheme.spacingSystem.space16)

    // Filter chips
    FilterChipsRow(selectedFilter = selectedFilter, onFilterSelected = { selectedFilter = it })

    VerticalSpacer(AppTheme.spacingSystem.space16)

    // Production cards or empty state
    if (filteredProductions.isEmpty()) {
      EmptyState(onCreateProductionClick = onCreateProductionClick)
    } else {
      filteredProductions.forEach { production ->
        ProductionCard(production = production)
        VerticalSpacer(AppTheme.spacingSystem.space16)
      }
    }
  }
}

@Composable
private fun EmptyState(onCreateProductionClick: () -> Unit) {
  Column(
    modifier = Modifier.fillMaxWidth().padding(vertical = AppTheme.spacingSystem.space24),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Illustration: stacked cards
    EmptyStateIllustration()

    VerticalSpacer(AppTheme.spacingSystem.space24)

    Text(
      text = stringResource(Res.string.projects_empty_title),
      style = AppTheme.typographySystem.titleMedium,
      color = AppTheme.colorSystem.textPrimary,
      textAlign = TextAlign.Center
    )

    VerticalSpacer(AppTheme.spacingSystem.space8)

    Text(
      text = stringResource(Res.string.projects_empty_description),
      style = AppTheme.typographySystem.bodySmall,
      color = AppTheme.colorSystem.textMuted,
      textAlign = TextAlign.Center
    )

    VerticalSpacer(AppTheme.spacingSystem.space24)

    // Create production button
    Box(
      modifier =
        Modifier
          .fillMaxWidth(0.7f)
          .clip(RoundedCornerShape(AppTheme.radiusSystem.radius16))
          .background(AppTheme.colorSystem.accent)
          .clickable(onClick = onCreateProductionClick)
          .padding(
            horizontal = AppTheme.spacingSystem.space24,
            vertical = AppTheme.spacingSystem.space16
          ),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = stringResource(Res.string.projects_create_button),
        style = AppTheme.typographySystem.labelLarge,
        color = AppTheme.colorSystem.textOnAccent
      )
    }

    VerticalSpacer(AppTheme.spacingSystem.space16)

    Text(
      text = stringResource(Res.string.projects_empty_invite),
      style = AppTheme.typographySystem.bodySmall,
      color = AppTheme.colorSystem.textMuted,
      textAlign = TextAlign.Center
    )
  }
}

@Composable
private fun EmptyStateIllustration() {
  val cardShape = RoundedCornerShape(AppTheme.radiusSystem.radius16)

  Box(
    modifier = Modifier.size(width = 220.dp, height = 160.dp),
    contentAlignment = Alignment.BottomCenter
  ) {
    // Back card (rotated left)
    Box(
      modifier =
        Modifier
          .size(width = 180.dp, height = 100.dp)
          .offset(y = (-40).dp)
          .rotate(-5f)
          .clip(cardShape)
          .background(AppTheme.colorSystem.cardBackground)
          .border(1.dp, AppTheme.colorSystem.cardBorder, cardShape)
    )

    // Middle card (rotated right)
    Box(
      modifier =
        Modifier
          .size(width = 180.dp, height = 100.dp)
          .offset(y = (-25).dp)
          .rotate(3f)
          .clip(cardShape)
          .background(AppTheme.colorSystem.cardBackground)
          .border(1.dp, AppTheme.colorSystem.cardBorder, cardShape)
    ) {
      // Colored progress bars at bottom of middle card
      Row(
        modifier =
          Modifier
            .align(Alignment.BottomStart)
            .padding(AppTheme.spacingSystem.space8)
            .fillMaxWidth()
      ) {
        Box(
          modifier =
            Modifier
              .weight(1f)
              .height(4.dp)
              .clip(RoundedCornerShape(AppTheme.radiusSystem.radius4))
              .background(AppTheme.colorSystem.warningText)
        )
        Spacer(modifier = Modifier.width(AppTheme.spacingSystem.space4))
        Box(
          modifier =
            Modifier
              .weight(1f)
              .height(4.dp)
              .clip(RoundedCornerShape(AppTheme.radiusSystem.radius4))
              .background(AppTheme.colorSystem.successText)
        )
        Spacer(modifier = Modifier.width(AppTheme.spacingSystem.space4))
        Box(
          modifier =
            Modifier
              .weight(0.5f)
              .height(4.dp)
              .clip(RoundedCornerShape(AppTheme.radiusSystem.radius4))
              .background(AppTheme.colorSystem.accent)
        )
      }
    }

    // Front card
    Box(
      modifier =
        Modifier
          .size(width = 200.dp, height = 70.dp)
          .clip(cardShape)
          .background(AppTheme.colorSystem.cardBackground)
          .border(1.dp, AppTheme.colorSystem.cardBorder, cardShape)
          .padding(AppTheme.spacingSystem.space8),
      contentAlignment = Alignment.CenterStart
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        // Plus icon
        Box(
          modifier =
            Modifier
              .size(32.dp)
              .clip(RoundedCornerShape(AppTheme.radiusSystem.radius8))
              .background(AppTheme.colorSystem.accent),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "+",
            style = AppTheme.typographySystem.titleSmall,
            color = AppTheme.colorSystem.textOnAccent
          )
        }
        Spacer(modifier = Modifier.width(AppTheme.spacingSystem.space8))
        Column {
          Box(
            modifier =
              Modifier
                .size(width = 80.dp, height = 8.dp)
                .clip(RoundedCornerShape(AppTheme.radiusSystem.radius4))
                .background(AppTheme.colorSystem.border)
          )
          VerticalSpacer(AppTheme.spacingSystem.space4)
          Box(
            modifier =
              Modifier
                .size(width = 50.dp, height = 6.dp)
                .clip(RoundedCornerShape(AppTheme.radiusSystem.radius4))
                .background(AppTheme.colorSystem.border)
          )
        }
      }
    }
  }
}

@Composable
private fun FilterChipsRow(
  selectedFilter: ProductionPhase?,
  onFilterSelected: (ProductionPhase?) -> Unit
) {
  val filters = listOf(
    null to stringResource(Res.string.projects_filter_all),
    ProductionPhase.PRE_PRODUCTION to stringResource(Res.string.projects_filter_pre_production),
    ProductionPhase.PRODUCTION to stringResource(Res.string.projects_filter_production),
    ProductionPhase.POST_PRODUCTION to stringResource(Res.string.projects_filter_post_production)
  )

  Row(
    modifier = Modifier.horizontalScroll(rememberScrollState()),
    horizontalArrangement = Arrangement.spacedBy(AppTheme.spacingSystem.space8)
  ) {
    filters.forEach { (phase, label) ->
      FilterChip(
        label = label,
        selected = selectedFilter == phase,
        onClick = { onFilterSelected(phase) }
      )
    }
  }
}

@Composable
private fun FilterChip(
  label: String,
  selected: Boolean,
  onClick: () -> Unit
) {
  val background = if (selected) AppTheme.colorSystem.accent else Color.Transparent
  val textColor =
    if (selected) AppTheme.colorSystem.textOnAccent else AppTheme.colorSystem.textSecondary
  val shape = RoundedCornerShape(AppTheme.radiusSystem.radiusMax)

  Box(
    modifier =
      Modifier
        .clip(shape)
        .clickable(onClick = onClick)
        .background(background)
        .padding(
          horizontal = AppTheme.spacingSystem.space16,
          vertical = AppTheme.spacingSystem.space8
        ),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      style = AppTheme.typographySystem.labelMedium,
      color = textColor
    )
  }
}

@Composable
private fun ProductionCard(production: Production) {
  val accentColor = accentColorFor(production.accentColorHint)

  Column(
    modifier =
      Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(AppTheme.radiusSystem.radius16))
        .background(AppTheme.colorSystem.cardBackground)
        .padding(AppTheme.spacingSystem.space16)
  ) {
    // Title row with phase badge
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.Top
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = production.title,
          style = AppTheme.typographySystem.titleMedium,
          color = AppTheme.colorSystem.textPrimary
        )
        VerticalSpacer(AppTheme.spacingSystem.space4)
        Text(
          text = production.genre.displayLabel(),
          style = AppTheme.typographySystem.bodySmall,
          color = AppTheme.colorSystem.textMuted
        )
      }
      PhaseBadge(phase = production.phase)
    }

    VerticalSpacer(AppTheme.spacingSystem.space16)

    // Pipeline progress
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = stringResource(Res.string.projects_pipeline_progress),
        style = AppTheme.typographySystem.bodySmall,
        color = AppTheme.colorSystem.textMuted
      )
      Text(
        text = "${production.progressPercent}%",
        style = AppTheme.typographySystem.labelMedium,
        color = accentColor
      )
    }

    VerticalSpacer(AppTheme.spacingSystem.space8)

    // Progress bar
    ProgressBar(progress = production.progressPercent / 100f, color = accentColor)

    VerticalSpacer(AppTheme.spacingSystem.space16)

    // Footer: members and days left
    Row(
      horizontalArrangement = Arrangement.spacedBy(AppTheme.spacingSystem.space16),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacingSystem.space4),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "👤",
          style = AppTheme.typographySystem.bodySmall
        )
        Text(
          text = stringResource(Res.string.members_count, production.membersCount),
          style = AppTheme.typographySystem.bodySmall,
          color = AppTheme.colorSystem.textMuted
        )
      }
      Row(
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacingSystem.space4),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "📅",
          style = AppTheme.typographySystem.bodySmall
        )
        val daysColor = if (production.daysLeft <= 7) {
          AppTheme.colorSystem.errorText
        } else {
          AppTheme.colorSystem.textMuted
        }
        Text(
          text = stringResource(Res.string.days_left, production.daysLeft),
          style = AppTheme.typographySystem.bodySmall,
          color = daysColor
        )
      }
    }
  }
}

@Composable
private fun PhaseBadge(phase: ProductionPhase) {
  val shape = RoundedCornerShape(AppTheme.radiusSystem.radius8)
  Box(
    modifier =
      Modifier
        .clip(shape)
        .background(AppTheme.colorSystem.accentSurface)
        .padding(
          horizontal = AppTheme.spacingSystem.space8,
          vertical = AppTheme.spacingSystem.space4
        ),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = phase.displayLabel(),
      style = AppTheme.typographySystem.labelSmall,
      color = AppTheme.colorSystem.accentText
    )
  }
}

@Composable
private fun ProgressBar(
  progress: Float,
  color: Color
) {
  val trackColor = AppTheme.colorSystem.border
  val shape = RoundedCornerShape(AppTheme.radiusSystem.radius4)

  Box(
    modifier =
      Modifier
        .fillMaxWidth()
        .height(6.dp)
        .clip(shape)
        .background(trackColor)
  ) {
    Box(
      modifier =
        Modifier
          .fillMaxWidth(fraction = progress.coerceIn(0f, 1f))
          .height(6.dp)
          .clip(shape)
          .background(color)
    )
  }
}

@Composable
private fun accentColorFor(hint: AccentColorHint): Color =
  when (hint) {
    AccentColorHint.GREEN -> AppTheme.colorSystem.successText
    AccentColorHint.PURPLE -> AppTheme.colorSystem.accent
    AccentColorHint.ORANGE -> AppTheme.colorSystem.warningText
  }

private fun ProductionPhase.displayLabel(): String =
  name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }

private fun Genre.displayLabel(): String = name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }

// ── Preview ───────────────────────────────────────────────────────────

@Preview
@Composable
private fun ProjectsEmptyPreview() {
  AppTheme(darkTheme = true) {
    ProjectsContent(
      state = ProjectsTabState(isLoading = false, productions = emptyList()),
      onCreateProductionClick = {}
    )
  }
}

@OptIn(ExperimentalTime::class)
@Preview
@Composable
private fun ProjectsContentPreview() {
  AppTheme(darkTheme = true) {
    ProjectsContent(
      onCreateProductionClick = {},
      state =
        ProjectsTabState(
          isLoading = false,
          productions =
            listOf(
              Production(
                id = "1",
                title = "Echoes of Silence",
                genre = Genre.DRAMA,
                phase = ProductionPhase.PRODUCTION,
                progressPercent = 68,
                daysLeft = 24,
                membersCount = 12,
                accentColorHint = AccentColorHint.GREEN,
                updatedAt = Clock.System.now()
              ),
              Production(
                id = "2",
                title = "Neon Wolves",
                genre = Genre.THRILLER,
                phase = ProductionPhase.PRE_PRODUCTION,
                progressPercent = 34,
                daysLeft = 61,
                membersCount = 8,
                accentColorHint = AccentColorHint.PURPLE,
                updatedAt = Clock.System.now()
              ),
              Production(
                id = "3",
                title = "The Last Frame",
                genre = Genre.SCI_FI,
                phase = ProductionPhase.POST_PRODUCTION,
                progressPercent = 91,
                daysLeft = 7,
                membersCount = 6,
                accentColorHint = AccentColorHint.ORANGE,
                updatedAt = Clock.System.now()
              )
            )
        )
    )
  }
}
