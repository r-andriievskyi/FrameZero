package com.frame.zero.feature.task.details.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.frame.zero.feature.task.details.TaskDetailsComponent
import com.frame.zero.feature.task.details.TaskDetailsState
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import framezero.composeapp.features.task_details.generated.resources.Res
import framezero.composeapp.features.task_details.generated.resources.task_details_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun TaskDetailsScreen(component: TaskDetailsComponent) {
  val state by component.state.collectAsState()
  TaskDetailsContent(
    state = state,
    onBack = component.onBack
  )
}

@Composable
internal fun TaskDetailsContent(
  state: TaskDetailsState,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxSize()
      .background(AppTheme.colorSystem.background)
      .systemBarsPadding(),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = stringResource(Res.string.task_details_title),
      style = AppTheme.typographySystem.titleLarge,
      color = AppTheme.colorSystem.textPrimary
    )
  }
}

@LightDarkPreview
@Composable
private fun TaskDetailsScreenPreview() {
  AppTheme {
    TaskDetailsContent(
      state = TaskDetailsState(taskId = "preview-task-id"),
      onBack = {}
    )
  }
}
