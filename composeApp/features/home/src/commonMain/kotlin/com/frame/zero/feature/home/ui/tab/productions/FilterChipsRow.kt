package com.frame.zero.feature.home.ui.tab.productions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.domain.production.ProductionPhase
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
  selectedFilter: ProductionPhase?,
  onFilterSelected: (ProductionPhase?) -> Unit
) {
  val filters = listOf(
    null to stringResource(Res.string.projects_filter_all),
    ProductionPhase.IDEA to stringResource(Res.string.projects_filter_idea),
    ProductionPhase.DEVELOPMENT to stringResource(Res.string.projects_filter_development),
    ProductionPhase.FINANCING to stringResource(Res.string.projects_filter_financing),
    ProductionPhase.PRE_PRODUCTION to stringResource(Res.string.projects_filter_pre_production),
    ProductionPhase.PRODUCTION to stringResource(Res.string.projects_filter_production),
    ProductionPhase.POST_PRODUCTION to stringResource(Res.string.projects_filter_post_production),
    ProductionPhase.MARKETING to stringResource(Res.string.projects_filter_marketing),
    ProductionPhase.DISTRIBUTION to stringResource(Res.string.projects_filter_distribution),
    ProductionPhase.RELEASE to stringResource(Res.string.projects_filter_release),
    ProductionPhase.ARCHIVED to stringResource(Res.string.projects_filter_archived)
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
  val textColor = if (selected) AppTheme.colorSystem.textOnAccent else AppTheme.colorSystem.textSecondary
  val shape = RoundedCornerShape(AppTheme.radiusSystem.radiusMax)

  Box(
    modifier = Modifier
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

@Preview
@Composable
private fun FilterChipsRowNoneSelectedPreview() {
  AppTheme(darkTheme = true) {
    Column(
      modifier = Modifier
        .background(AppTheme.colorSystem.background)
        .padding(AppTheme.spacingSystem.space16)
    ) {
      FilterChipsRow(
        selectedFilter = null,
        onFilterSelected = {}
      )
    }
  }
}

@Preview
@Composable
private fun FilterChipsRowProductionSelectedPreview() {
  AppTheme(darkTheme = true) {
    Column(
      modifier = Modifier
        .background(AppTheme.colorSystem.background)
        .padding(AppTheme.spacingSystem.space16)
    ) {
      FilterChipsRow(
        selectedFilter = ProductionPhase.PRODUCTION,
        onFilterSelected = {}
      )
    }
  }
}
