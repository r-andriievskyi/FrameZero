package com.frame.zero.feature.task.list.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.frame.zero.domain.task.TaskStatus
import com.frame.zero.feature.task.list.TaskListItemUi
import com.frame.zero.feature.task.list.TasksListComponent
import com.frame.zero.feature.task.list.TasksListIntent
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.modifier.clickableWithRipple
import com.frame.zero.shared.design_system.widgets.PagingLazyColumn
import com.frame.zero.shared.design_system.widgets.TopToolbar
import com.frame.zero.shared.design_system.widgets.rememberPagingListUiState
import framezero.composeapp.features.task_list.generated.resources.Res
import framezero.composeapp.features.task_list.generated.resources.task_list_title
import kotlinx.coroutines.flow.flowOf
import org.jetbrains.compose.resources.stringResource

@Composable
fun TasksListScreen(component: TasksListComponent) {
  val lazyPagingItems = component.tasks.collectAsLazyPagingItems()
  TasksListContent(
    lazyPagingItems = lazyPagingItems,
    onIntent = component::onIntent
  )
}

@Composable
internal fun TasksListContent(
  lazyPagingItems: LazyPagingItems<TaskListItemUi>,
  onIntent: (TasksListIntent) -> Unit,
) {
  Column(
    modifier = Modifier.fillMaxSize().statusBarsPadding()
  ) {
    TopToolbar(
      title = stringResource(Res.string.task_list_title),
      onBack = {
        onIntent(TasksListIntent.Back)
      }
    )
    val pagingState = rememberPagingListUiState(lazyPagingItems = lazyPagingItems)
    PagingLazyColumn(
      lazyPagingItems = lazyPagingItems,
      state = pagingState,
      modifier = Modifier.fillMaxSize(),
      /*contentPadding = PaddingValues(
        bottom = navigationBarsBottom + FloatingBottomNavClearance
      ),*/
      /*refreshIndicator = { pullState ->
        DefaultInlineRefreshIndicator(
          pullState = pullState,
          refreshingText = stringResource(Res.string.projects_refreshing),
          releaseText = stringResource(Res.string.projects_release_to_refresh),
          subtitle = stringResource(Res.string.projects_count, count)
        )
      },*/
      /*  appendErrorMessage = appendError?.toDomainError()?.toUiText()?.asString()
          ?: stringResource(DesignSystemRes.string.error_network_message),*/
      itemKey = { it.id }
    ) { task ->
      TaskCard(
        data = task,
        onClick = { onIntent(TasksListIntent.TaskClick(task.id)) }
      )
    }
  }
}

@Composable
private fun TaskCard(data: TaskListItemUi, onClick: () -> Unit) {
  val spacingSystem = AppTheme.spacingSystem
  Column(
    modifier = Modifier.padding(horizontal = spacingSystem.space16, vertical = spacingSystem.space8).fillMaxWidth()
      .background(AppTheme.colorSystem.background)
      .clickableWithRipple(color = AppTheme.colorSystem.accentDim, onClick = onClick)
  ) {
    Text(text = data.title)
  }
}

@LightDarkPreview
@Composable
private fun TasksListContentPreview() {
  AppTheme {
    val items = listOf(
      TaskListItemUi(
        id = "1",
        title = "Write shooting script",
        dueDateLabel = "Aug 5, 2026",
        status = TaskStatus.OPEN
      ),
      TaskListItemUi(
        id = "2",
        title = "Book location permits",
        dueDateLabel = "Aug 12, 2026",
        status = TaskStatus.OPEN
      ),
      TaskListItemUi(
        id = "3",
        title = "Finalise cast list",
        dueDateLabel = null,
        status = TaskStatus.DONE
      )
    )
    TasksListContent(
      lazyPagingItems = flowOf(PagingData.from(items)).collectAsLazyPagingItems(),
      onIntent = {}
    )
  }
}

