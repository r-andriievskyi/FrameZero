package com.frame.zero.feature.home.ui.tab.productions

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.frame.zero.domain.production.ProductionPhase
import com.frame.zero.feature.home.tab.productions.ProductionFilter
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.modifier.clickableWithRipple
import com.frame.zero.shared.design_system.widgets.rememberRoundedCornerShape
import framezero.composeapp.features.home.generated.resources.Res
import framezero.composeapp.features.home.generated.resources.projects_filter_all
import framezero.composeapp.features.home.generated.resources.projects_filter_archived
import framezero.composeapp.features.home.generated.resources.projects_filter_development
import framezero.composeapp.features.home.generated.resources.projects_filter_distribution
import framezero.composeapp.features.home.generated.resources.projects_filter_financing
import framezero.composeapp.features.home.generated.resources.projects_filter_idea
import framezero.composeapp.features.home.generated.resources.projects_filter_marketing
import framezero.composeapp.features.home.generated.resources.projects_filter_post_production
import framezero.composeapp.features.home.generated.resources.projects_filter_pre_production
import framezero.composeapp.features.home.generated.resources.projects_filter_production
import framezero.composeapp.features.home.generated.resources.projects_filter_release
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun FilterChipsRow(
  availableFilters: List<ProductionFilter>,
  selectedFilter: ProductionFilter,
  onFilterSelected: (ProductionFilter) -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier.horizontalScroll(rememberScrollState()),
    horizontalArrangement = Arrangement.spacedBy(AppTheme.spacingSystem.space8)
  ) {
    availableFilters.forEach { filter ->
      FilterChip(
        label = filter.filterLabel(),
        selected = selectedFilter == filter,
        onClick = { onFilterSelected(filter) }
      )
    }
  }
}

@Composable
private fun ProductionFilter.filterLabel(): String = when (this) {
  ProductionFilter.All -> stringResource(Res.string.projects_filter_all)
  is ProductionFilter.ByPhase -> when (phase) {
    ProductionPhase.IDEA -> stringResource(Res.string.projects_filter_idea)
    ProductionPhase.DEVELOPMENT -> stringResource(Res.string.projects_filter_development)
    ProductionPhase.FINANCING -> stringResource(Res.string.projects_filter_financing)
    ProductionPhase.PRE_PRODUCTION -> stringResource(Res.string.projects_filter_pre_production)
    ProductionPhase.PRODUCTION -> stringResource(Res.string.projects_filter_production)
    ProductionPhase.POST_PRODUCTION -> stringResource(Res.string.projects_filter_post_production)
    ProductionPhase.MARKETING -> stringResource(Res.string.projects_filter_marketing)
    ProductionPhase.DISTRIBUTION -> stringResource(Res.string.projects_filter_distribution)
    ProductionPhase.RELEASE -> stringResource(Res.string.projects_filter_release)
    ProductionPhase.ARCHIVED -> stringResource(Res.string.projects_filter_archived)
  }
}

@Composable
private fun FilterChip(
  label: String,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val colorSystem = AppTheme.colorSystem
  val background = if (selected) colorSystem.accent else Color.Transparent
  val textColor = if (selected) colorSystem.textOnAccent else colorSystem.textSecondary
  val shape = rememberRoundedCornerShape(AppTheme.radiusSystem.radiusMax)

  val spacingSystem = AppTheme.spacingSystem
  Box(
    modifier = modifier
      .clip(shape)
      .clickableWithRipple(color = colorSystem.accentDim, onClick = onClick)
      .background(background)
      .padding(
        horizontal = spacingSystem.space16,
        vertical = spacingSystem.space8
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

@LightDarkPreview
@Composable
private fun FilterChipsRowProductionSelectedPreview() {
  AppTheme {
    Column(
      modifier = Modifier
        .background(AppTheme.colorSystem.background)
        .padding(AppTheme.spacingSystem.space16)
    ) {
      FilterChipsRow(
        availableFilters = listOf(ProductionFilter.All) + ProductionPhase.entries.map { ProductionFilter.ByPhase(it) },
        selectedFilter = ProductionFilter.ByPhase(ProductionPhase.IDEA),
        onFilterSelected = {}
      )
    }
  }
}
