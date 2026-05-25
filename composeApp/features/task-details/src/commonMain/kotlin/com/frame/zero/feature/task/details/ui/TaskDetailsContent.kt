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
import androidx.compose.ui.tooling.preview.Preview
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.feature.task.details.TaskDetailsComponent
import com.frame.zero.feature.task.details.TaskDetailsState

@Composable
fun TaskDetailsContent(component: TaskDetailsComponent) {
  val state by component.state.collectAsState()
  TaskDetailsScreen(
    state = state,
    onBack = component.onBack
  )
}

@Composable
internal fun TaskDetailsScreen(
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
      text = "Task Details",
      style = AppTheme.typographySystem.titleLarge,
      color = AppTheme.colorSystem.textPrimary
    )
  }
}

@Preview
@Composable
private fun TaskDetailsScreenPreview() {
  AppTheme(darkTheme = true) {
    TaskDetailsScreen(
      state = TaskDetailsState(taskId = "preview-task-id"),
      onBack = {}
    )
  }
}
