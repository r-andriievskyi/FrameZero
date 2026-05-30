package com.frame.zero.feature.home.ui.tab.productions

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.frame.zero.domain.production.Genre
import com.frame.zero.domain.production.ProductionPhase
import com.frame.zero.feature.home.tab.productions.ProductionUi
import com.frame.zero.feature.home.tab.productions.ProductionsTabComponent
import com.frame.zero.feature.home.ui.FloatingBottomNavClearance
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.widgets.DefaultInlineRefreshIndicator
import com.frame.zero.shared.design_system.widgets.PagingLazyColumn
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import com.frame.zero.shared.design_system.widgets.rememberPagingListUiState
import framezero.composeapp.features.home.generated.resources.Res
import framezero.composeapp.features.home.generated.resources.ic_plus
import framezero.composeapp.features.home.generated.resources.projects_count
import framezero.composeapp.features.home.generated.resources.projects_refreshing
import framezero.composeapp.features.home.generated.resources.projects_release_to_refresh
import framezero.composeapp.features.home.generated.resources.projects_title
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private val AddButtonSize = 40.dp
private const val ContentFadeInMillis = 150
private const val ContentFadeOutMillis = 140

private enum class ProductionsContentState { Skeleton, Empty, List }

@Composable
fun ProductionsTab(component: ProductionsTabComponent) {
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
  val pagingState = rememberPagingListUiState(
    lazyPagingItems = lazyPagingItems,
    resetKey = selectedFilter
  )

  val navigationBarsBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(AppTheme.colorSystem.background)
      .padding(horizontal = AppTheme.spacingSystem.space16)
      .padding(top = AppTheme.spacingSystem.space16)
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
      AnimatedContent(
        targetState = pagingState.isEmpty,
        transitionSpec = {
          fadeIn(animationSpec = tween(durationMillis = ContentFadeInMillis)) togetherWith
            fadeOut(animationSpec = tween(durationMillis = ContentFadeOutMillis))
        }
      ) { isEmpty ->
        if (isEmpty) {
          Spacer(
            modifier = Modifier.size(AddButtonSize)
          )
        } else {
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
    }

    VerticalSpacer(AppTheme.spacingSystem.space16)

    FilterChipsRow(selectedFilter = selectedFilter, onFilterSelected = onFilterSelected)

    VerticalSpacer(AppTheme.spacingSystem.space16)

    val contentState = when {
      pagingState.isInitialLoad -> ProductionsContentState.Skeleton
      pagingState.isEmpty -> ProductionsContentState.Empty
      else -> ProductionsContentState.List
    }

    AnimatedContent(
      targetState = contentState,
      transitionSpec = {
        fadeIn(animationSpec = tween(durationMillis = ContentFadeInMillis)) togetherWith
          fadeOut(animationSpec = tween(durationMillis = ContentFadeOutMillis))
      },
      modifier = Modifier.fillMaxSize(),
      contentKey = { it }
    ) { target ->
      when (target) {
        ProductionsContentState.Skeleton -> ProductionsSkeleton()
        ProductionsContentState.Empty -> EmptyState(onCreateProductionClick = onCreateProductionClick)
        ProductionsContentState.List -> {
          val count = lazyPagingItems.itemCount
          PagingLazyColumn(
            lazyPagingItems = lazyPagingItems,
            state = pagingState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
              bottom = navigationBarsBottom + FloatingBottomNavClearance
            ),
            refreshIndicator = { pullState ->
              DefaultInlineRefreshIndicator(
                pullState = pullState,
                refreshingText = stringResource(Res.string.projects_refreshing),
                releaseText = stringResource(Res.string.projects_release_to_refresh),
                subtitle = stringResource(Res.string.projects_count, count)
              )
            },
            itemKey = { it.id }
          ) { production ->
            ProductionCard(
              production = production,
              onClick = { onProductionClick(production.id) }
            )
          }
        }
      }
    }
  }
}

private fun previewPagingFlow(items: List<ProductionUi>): Flow<PagingData<ProductionUi>> =
  flowOf(PagingData.from(items))

@LightDarkPreview
@Composable
private fun ProductionsContentPreview() {
  AppTheme {
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
