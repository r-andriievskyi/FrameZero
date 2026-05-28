package com.frame.zero.feature.task.details.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.frame.zero.feature.task.details.ActivityEntry
import com.frame.zero.feature.task.details.ChecklistItem
import com.frame.zero.feature.task.details.TaskAttachment
import com.frame.zero.feature.task.details.TaskDetailsComponent
import com.frame.zero.feature.task.details.TaskDetailsIntent
import com.frame.zero.feature.task.details.TaskDetailsState
import com.frame.zero.feature.task.details.TaskMember
import com.frame.zero.feature.task.details.TaskPriority
import com.frame.zero.feature.task.details.TaskStatus
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.widgets.OverflowMenu
import com.frame.zero.shared.design_system.widgets.TopToolbar
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import framezero.composeapp.features.task_details.generated.resources.Res
import framezero.composeapp.features.task_details.generated.resources.task_details_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun TaskDetailsScreen(component: TaskDetailsComponent) {
  val state by component.state.collectAsState()
  TaskDetailsContent(
    state = state,
    onBack = component.onBack,
    onIntent = component::onIntent
  )
}

@Composable
internal fun TaskDetailsContent(
  state: TaskDetailsState,
  onBack: () -> Unit,
  onIntent: (TaskDetailsIntent) -> Unit,
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
        title = stringResource(Res.string.task_details_title),
        onBack = onBack,
        trailingContent = {
          OverflowMenu(items = emptyList())
        }
      )

      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = AppTheme.spacingSystem.space16)
      ) {
        HeaderSection(
          priority = state.priority,
          productionName = state.productionName
        )
        VerticalSpacer(AppTheme.spacingSystem.space8)

        Text(
          text = state.title,
          style = AppTheme.typographySystem.displayMedium,
          color = AppTheme.colorSystem.textPrimary
        )
        VerticalSpacer(AppTheme.spacingSystem.space16)

        MarkCompleteCard(
          status = state.status,
          onToggleComplete = { onIntent(TaskDetailsIntent.ToggleComplete) }
        )
        VerticalSpacer(AppTheme.spacingSystem.space16)

        DetailsCard(
          assignee = state.assignee,
          reporter = state.reporter,
          dueDate = state.dueDate,
          isDueToday = state.isDueToday,
          phase = state.phase
        )
        VerticalSpacer(AppTheme.spacingSystem.space16)

        if (state.description.isNotBlank()) {
          DescriptionCard(
            description = state.description,
            tags = state.tags
          )
          VerticalSpacer(AppTheme.spacingSystem.space16)
        }

        if (state.checklist.isNotEmpty()) {
          ChecklistCard(
            checklist = state.checklist,
            onToggleItem = { onIntent(TaskDetailsIntent.ToggleChecklistItem(it)) }
          )
          VerticalSpacer(AppTheme.spacingSystem.space16)
        }

        if (state.attachments.isNotEmpty()) {
          AttachmentsCard(attachments = state.attachments)
          VerticalSpacer(AppTheme.spacingSystem.space16)
        }

        if (state.activity.isNotEmpty()) {
          ActivityCard(activity = state.activity)
          VerticalSpacer(AppTheme.spacingSystem.space32)
        }
      }
    }
  }
}

@LightDarkPreview
@Composable
private fun TaskDetailsContentPreview() {
  AppTheme {
    TaskDetailsContent(
      state = TaskDetailsState(
        taskId = "preview-task-id",
        title = "Review Scene 12 script revisions",
        productionName = "Echoes of Silence",
        priority = TaskPriority.HIGH,
        status = TaskStatus.IN_PROGRESS,
        assignee = TaskMember(
          initials = "MR",
          name = "Maya Rivera",
          role = "Director",
          avatarColorHex = "#0097A7"
        ),
        reporter = TaskMember(
          initials = "TE",
          name = "Tom Ellison",
          role = "1st AD",
          avatarColorHex = "#7B1FA2"
        ),
        dueDate = "Apr 26, 2026",
        isDueToday = true,
        phase = "Production",
        description = "Writer turned in revised pages for the confrontation in Scene 12. " +
          "Review the new dialogue against the shooting schedule and flag any continuity " +
          "issues with the Act II callbacks before the table read.",
        tags = listOf("Script", "Act II", "Review"),
        checklist = listOf(
          ChecklistItem(id = "1", text = "Read revised pages (3\u20137)", isCompleted = true),
          ChecklistItem(id = "2", text = "Check continuity vs. Scene 9", isCompleted = true),
          ChecklistItem(id = "3", text = "Note blocking changes for DP", isCompleted = false),
          ChecklistItem(id = "4", text = "Sign off with writer", isCompleted = false)
        ),
        attachments = listOf(
          TaskAttachment(
            id = "a1",
            fileName = "Scene12_rev4.pdf",
            fileType = "PDF",
            fileSize = "1.2 MB"
          ),
          TaskAttachment(
            id = "a2",
            fileName = "Continuity_notes.fdx",
            fileType = "Final Draft",
            fileSize = "84 KB"
          )
        ),
        activity = listOf(
          ActivityEntry(
            id = "act1",
            initials = "TE",
            avatarColorHex = "#7B1FA2",
            text = "TE assigned this to Maya",
            timestamp = "4d ago"
          ),
          ActivityEntry(
            id = "act2",
            initials = "MR",
            avatarColorHex = "#0097A7",
            text = "MR started reviewing pages",
            timestamp = "2h ago"
          )
        )
      ),
      onBack = {},
      onIntent = {}
    )
  }
}

