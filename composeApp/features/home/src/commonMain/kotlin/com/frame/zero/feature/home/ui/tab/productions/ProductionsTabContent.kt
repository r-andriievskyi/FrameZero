package com.frame.zero.feature.home.ui.tab.productions

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.discovery.playground.shared.design_system.AppTheme
import com.discovery.playground.shared.design_system.widgets.PullToRefreshBox
import com.discovery.playground.shared.design_system.widgets.VerticalSpacer
import com.frame.zero.domain.production.Genre
import com.frame.zero.domain.production.ProductionPhase
import com.frame.zero.feature.home.tab.projects.ProductionUi
import com.frame.zero.feature.home.tab.projects.ProjectsTabComponent
import framezero.composeapp.features.home.generated.resources.Res
import framezero.composeapp.features.home.generated.resources.ic_plus
import framezero.composeapp.features.home.generated.resources.projects_title
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private val AddButtonSize = 40.dp

@Composable
fun ProductionsTabContent(component: ProjectsTabComponent) {
  val state by component.state.collectAsState()
  val lazyPagingItems = component.productions.collectAsLazyPagingItems()
  ProductionsContent(
    lazyPagingItems = lazyPagingItems,
    selectedFilter = state.selectedFilter,
    onFilterSelected = component::onFilterSelected,
    onCreateProductionClick = component.onCreateProductionClick,
    onProductionClick = component.onProductionClick
  )
}

@Composable
private fun ProductionsContent(
  lazyPagingItems: LazyPagingItems<ProductionUi>,
  selectedFilter: ProductionPhase?,
  onFilterSelected: (ProductionPhase?) -> Unit,
  onCreateProductionClick: () -> Unit,
  onProductionClick: (productionId: String) -> Unit = {}
) {
  val refreshState = lazyPagingItems.loadState.refresh
  val appendState = lazyPagingItems.loadState.append
  val isInitialLoad = refreshState is LoadState.Loading && lazyPagingItems.itemCount == 0
  val isRefreshing = refreshState is LoadState.Loading && lazyPagingItems.itemCount > 0
  val isEmpty = !isInitialLoad && lazyPagingItems.itemCount == 0

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(AppTheme.colorSystem.background)
      .padding(
        horizontal = AppTheme.spacingSystem.space16,
        vertical = AppTheme.spacingSystem.space24
      )
  ) {
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
      if (!isEmpty) {
        Box(
          modifier = Modifier
            .size(AddButtonSize)
            .clip(RoundedCornerShape(AppTheme.radiusSystem.radius8))
            .background(AppTheme.colorSystem.accent)
            .clickable(onClick = onCreateProductionClick),
          contentAlignment = Alignment.Center
        ) {
          Image(
            painter = painterResource(Res.drawable.ic_plus),
            contentDescription = null
          )
        }
      }
    }

    VerticalSpacer(AppTheme.spacingSystem.space16)

    FilterChipsRow(selectedFilter = selectedFilter, onFilterSelected = onFilterSelected)

    VerticalSpacer(AppTheme.spacingSystem.space16)

    when {
      isEmpty -> EmptyState(onCreateProductionClick = onCreateProductionClick)
      else -> PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = lazyPagingItems::refresh,
        modifier = Modifier.fillMaxSize()
      ) {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.spacedBy(AppTheme.spacingSystem.space16)
        ) {
          items(
            count = lazyPagingItems.itemCount,
            key = lazyPagingItems.itemKey { it.id }
          ) { index ->
            val production = lazyPagingItems[index] ?: return@items
            ProductionCard(
              production = production,
              onClick = { onProductionClick(production.id) }
            )
          }
          if (appendState is LoadState.Loading) {
            item(key = "append-loading") {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(AppTheme.spacingSystem.space16),
                contentAlignment = Alignment.Center
              ) {
                CircularProgressIndicator(color = AppTheme.colorSystem.accent)
              }
            }
          }
        }
      }
    }
  }
}

// ── Previews ──────────────────────────────────────────────────────────

private fun previewPagingFlow(items: List<ProductionUi>): Flow<PagingData<ProductionUi>> =
  flowOf(PagingData.from(items))

@Preview
@Composable
private fun ProductionsEmptyPreview() {
  AppTheme(darkTheme = true) {
    ProductionsContent(
      lazyPagingItems = previewPagingFlow(emptyList()).collectAsLazyPagingItems(),
      selectedFilter = null,
      onFilterSelected = {},
      onCreateProductionClick = {}
    )
  }
}

@Preview
@Composable
private fun ProductionsContentPreview() {
  AppTheme(darkTheme = true) {
    val items = listOf(
      ProductionUi(
        id = "1",
        title = "Echoes of Silence",
        genre = Genre.DRAMA,
        phase = ProductionPhase.PRODUCTION,
        progressPercent = 68,
        daysLeft = 24,
        membersCount = 12
      ),
      ProductionUi(
        id = "2",
        title = "Neon Wolves",
        genre = Genre.THRILLER,
        phase = ProductionPhase.PRE_PRODUCTION,
        progressPercent = 34,
        daysLeft = 61,
        membersCount = 8
      ),
      ProductionUi(
        id = "3",
        title = "The Last Frame",
        genre = Genre.SCI_FI,
        phase = ProductionPhase.POST_PRODUCTION,
        progressPercent = 91,
        daysLeft = 7,
        membersCount = 6
      )
    )
    ProductionsContent(
      lazyPagingItems = previewPagingFlow(items).collectAsLazyPagingItems(),
      selectedFilter = null,
      onFilterSelected = {},
      onCreateProductionClick = {},
      onProductionClick = {}
    )
  }
}
