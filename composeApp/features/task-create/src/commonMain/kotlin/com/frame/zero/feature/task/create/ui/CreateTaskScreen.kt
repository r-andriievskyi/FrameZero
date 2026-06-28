package com.frame.zero.feature.task.create.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.frame.zero.dto.task.TaskPriority
import com.frame.zero.feature.task.create.AssignableMemberUi
import com.frame.zero.feature.task.create.CreateTaskComponent
import com.frame.zero.feature.task.create.CreateTaskIntent
import com.frame.zero.feature.task.create.CreateTaskState
import com.frame.zero.feature.task.create.ui.components.AssigneeSelector
import com.frame.zero.feature.task.create.ui.components.AttachmentRow
import com.frame.zero.feature.task.create.ui.components.DueDateSection
import com.frame.zero.feature.task.create.ui.components.MultiLineInputField
import com.frame.zero.feature.task.create.ui.components.PrioritySelector
import com.frame.zero.feature.task.create.ui.components.SectionLabel
import com.frame.zero.shared.design_system.AppTheme
import kotlinx.collections.immutable.persistentListOf
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.modifier.clickableWithRipple
import com.frame.zero.shared.design_system.widgets.CtaButton
import com.frame.zero.shared.design_system.widgets.SingleLineInputField
import com.frame.zero.shared.design_system.widgets.TopToolbar
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import com.frame.zero.shared.design_system.widgets.toast.ToastHost
import com.frame.zero.ui.asString
import framezero.composeapp.features.task_create.generated.resources.Res
import framezero.composeapp.features.task_create.generated.resources.assignee_subtitle_count
import framezero.composeapp.features.task_create.generated.resources.assignee_subtitle_none
import framezero.composeapp.features.task_create.generated.resources.cancel
import framezero.composeapp.features.task_create.generated.resources.create_task
import framezero.composeapp.features.task_create.generated.resources.description_placeholder
import framezero.composeapp.features.task_create.generated.resources.label_assignee
import framezero.composeapp.features.task_create.generated.resources.label_description
import framezero.composeapp.features.task_create.generated.resources.label_attachment
import framezero.composeapp.features.task_create.generated.resources.label_due_date
import framezero.composeapp.features.task_create.generated.resources.label_priority
import framezero.composeapp.features.task_create.generated.resources.label_title
import framezero.composeapp.features.task_create.generated.resources.new_task_title
import framezero.composeapp.features.task_create.generated.resources.title_placeholder
import org.jetbrains.compose.resources.stringResource

@Composable
fun CreateTaskScreen(
  component: CreateTaskComponent,
  modifier: Modifier = Modifier
) {
  val state by component.state.collectAsStateWithLifecycle()
  Box(modifier = modifier.fillMaxSize()) {
    CreateTaskContent(
      state = state,
      onIntent = component::onIntent,
      onBack = component.onBack
    )
    ToastHost(
      message = state.errorToast?.asString(),
      onDismiss = { component.onIntent(CreateTaskIntent.ToastDismissed) }
    )
  }
}

@Composable
internal fun CreateTaskContent(
  state: CreateTaskState,
  onIntent: (CreateTaskIntent) -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val colors = AppTheme.colorSystem
  val spacing = AppTheme.spacingSystem

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(colors.background)
      .systemBarsPadding()
  ) {
    TopToolbar(
      title = stringResource(Res.string.new_task_title),
      onBack = onBack,
      trailingContent = {
        Text(
          text = stringResource(Res.string.cancel),
          style = AppTheme.typographySystem.bodyLarge,
          color = colors.textSecondary,
          modifier = Modifier
            .clickableWithRipple(color = colors.accentDim, onClick = onBack)
            .padding(horizontal = spacing.space8, vertical = spacing.space4)
        )
      }
    )

    Column(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = spacing.space16)
    ) {
      SectionLabel(text = stringResource(Res.string.label_title))
      VerticalSpacer(spacing.space8)
      SingleLineInputField(
        value = state.title,
        onValueChange = { onIntent(CreateTaskIntent.TitleChanged(it)) },
        placeholder = stringResource(Res.string.title_placeholder),
        errorMessage = state.titleError?.asString()
      )
      state.titleError?.let { error ->
        VerticalSpacer(spacing.space4)
        Text(
          text = error.asString(),
          style = AppTheme.typographySystem.bodySmall,
          color = colors.errorText,
          modifier = Modifier.testTag(CreateTaskTestTags.TITLE_ERROR)
        )
      }

      VerticalSpacer(spacing.space24)

      SectionLabel(text = stringResource(Res.string.label_description))
      VerticalSpacer(spacing.space8)
      MultiLineInputField(
        value = state.description,
        onValueChange = { onIntent(CreateTaskIntent.DescriptionChanged(it)) },
        placeholder = stringResource(Res.string.description_placeholder)
      )

      VerticalSpacer(spacing.space24)

      SectionLabel(text = stringResource(Res.string.label_attachment))
      VerticalSpacer(spacing.space8)
      AttachmentRow(
        attachmentName = state.attachment?.name,
        onAttach = { onIntent(CreateTaskIntent.AttachFileClicked) },
        onRemove = { onIntent(CreateTaskIntent.AttachmentRemoved) }
      )
      state.attachmentError?.let { error ->
        VerticalSpacer(spacing.space4)
        Text(
          text = error.asString(),
          style = AppTheme.typographySystem.bodySmall,
          color = colors.errorText
        )
      }

      VerticalSpacer(spacing.space24)

      SectionLabel(text = stringResource(Res.string.label_assignee))
      VerticalSpacer(spacing.space8)
      AssigneeSelector(
        selected = state.selectedAssignee,
        isPickerVisible = state.isAssigneePickerVisible,
        query = state.assigneeQuery,
        members = state.filteredAssignableMembers,
        onOpen = { onIntent(CreateTaskIntent.AssigneePickerOpened) },
        onDismiss = { onIntent(CreateTaskIntent.AssigneePickerDismissed) },
        onQueryChange = { onIntent(CreateTaskIntent.AssigneeSearchChanged(it)) },
        onSelect = { onIntent(CreateTaskIntent.AssigneeSelected(it)) }
      )
      VerticalSpacer(spacing.space4)
      Text(
        text = if (state.assignableMembers.isEmpty()) {
          stringResource(Res.string.assignee_subtitle_none)
        } else {
          stringResource(Res.string.assignee_subtitle_count, state.assignableMembers.size)
        },
        style = AppTheme.typographySystem.bodySmall,
        color = colors.textMuted
      )

      VerticalSpacer(spacing.space24)

      SectionLabel(text = stringResource(Res.string.label_priority))
      VerticalSpacer(spacing.space8)
      PrioritySelector(
        selected = state.priority,
        onSelect = { onIntent(CreateTaskIntent.PriorityChanged(it)) }
      )

      VerticalSpacer(spacing.space24)

      SectionLabel(text = stringResource(Res.string.label_due_date))
      VerticalSpacer(spacing.space8)
      DueDateSection(
        dueDate = state.dueDate,
        onQuickSelect = { onIntent(CreateTaskIntent.QuickDueDateSelected(it)) },
        onDateChange = { onIntent(CreateTaskIntent.DueDateChanged(it)) }
      )

      VerticalSpacer(spacing.space24)
    }

    HorizontalDivider(
      thickness = AppTheme.borderSystem.hairline,
      color = colors.border
    )

    CtaButton(
      text = stringResource(Res.string.create_task),
      loading = state.isLoading,
      onClick = { onIntent(CreateTaskIntent.Submit) },
      modifier = Modifier
        .fillMaxWidth()
        .testTag(CreateTaskTestTags.SUBMIT)
        .padding(spacing.space16)
    )
  }
}

@LightDarkPreview
@Composable
private fun CreateTaskContentPreview() {
  AppTheme {
    CreateTaskContent(
      state = CreateTaskState(
        productionTitle = "Echoes of Silence",
        priority = TaskPriority.MEDIUM,
        assignableMembers = persistentListOf(
          AssignableMemberUi("u1", "Sara Lin", "SL", "#9C27B0"),
          AssignableMemberUi("u2", "Jake Morse", "JM", "#009688")
        )
      ),
      onIntent = {},
      onBack = {}
    )
  }
}
