package com.frame.zero.feature.home.ui.tab.productions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.discovery.playground.shared.design_system.AppTheme
import com.discovery.playground.shared.design_system.widgets.VerticalSpacer
import com.frame.zero.domain.production.Genre
import com.frame.zero.domain.production.Production
import com.frame.zero.domain.production.ProductionPhase
import com.frame.zero.feature.home.tab.projects.ProjectsTabComponent
import com.frame.zero.feature.home.tab.projects.ProjectsTabState
import framezero.composeapp.features.home.generated.resources.Res
import framezero.composeapp.features.home.generated.resources.projects_title
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Composable
fun ProductionsTabContent(component: ProjectsTabComponent) {
  LaunchedEffect(Unit) { component.onAppeared() }
  val state by component.state.collectAsState()
  ProductionsContent(state = state, onCreateProductionClick = component.onCreateProductionClick)
}

@Composable
private fun ProductionsContent(
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
    modifier = Modifier
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
          modifier = Modifier
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

// ── Previews ──────────────────────────────────────────────────────────

@Preview
@Composable
private fun ProductionsEmptyPreview() {
  AppTheme(darkTheme = true) {
    ProductionsContent(
      state = ProjectsTabState(isLoading = false, productions = emptyList()),
      onCreateProductionClick = {}
    )
  }
}

@OptIn(ExperimentalTime::class)
@Preview
@Composable
private fun ProductionsContentPreview() {
  AppTheme(darkTheme = true) {
    ProductionsContent(
      onCreateProductionClick = {},
      state = ProjectsTabState(
        isLoading = false,
        productions = listOf(
          Production(
            id = "1",
            title = "Echoes of Silence",
            genre = Genre.DRAMA,
            phase = ProductionPhase.PRODUCTION,
            progressPercent = 68,
            daysLeft = 24,
            membersCount = 12,
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
            updatedAt = Clock.System.now()
          )
        )
      )
    )
  }
}
