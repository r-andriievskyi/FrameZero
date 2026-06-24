package com.frame.zero.feature.task.details.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.frame.zero.feature.task.details.AttachmentDownloadError
import com.frame.zero.feature.task.details.TaskAttachment
import com.frame.zero.feature.task.details.TaskDetailsComponent
import com.frame.zero.feature.task.details.TaskDetailsIntent
import com.frame.zero.feature.task.details.TaskDetailsState
import com.frame.zero.feature.task.details.TaskMember
import com.frame.zero.feature.task.details.TaskPriority
import com.frame.zero.feature.task.details.TaskStatus
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.modifier.clickableWithRipple
import com.frame.zero.shared.design_system.widgets.CtaButton
import com.frame.zero.shared.design_system.widgets.FullScreenError
import com.frame.zero.shared.design_system.widgets.FullScreenProgress
import com.frame.zero.shared.design_system.widgets.HorizontalSpacer
import com.frame.zero.shared.design_system.widgets.TopToolbar
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import framezero.composeapp.features.task_details.generated.resources.Res
import framezero.composeapp.features.task_details.generated.resources.task_details_assignee
import framezero.composeapp.features.task_details.generated.resources.task_details_attachment
import framezero.composeapp.features.task_details.generated.resources.task_details_attachment_error_generic
import framezero.composeapp.features.task_details.generated.resources.task_details_attachment_error_offline
import framezero.composeapp.features.task_details.generated.resources.task_details_attachment_error_storage
import framezero.composeapp.features.task_details.generated.resources.task_details_attachment_downloading
import framezero.composeapp.features.task_details.generated.resources.task_details_attachment_open
import framezero.composeapp.features.task_details.generated.resources.task_details_due_date
import framezero.composeapp.features.task_details.generated.resources.task_details_error
import framezero.composeapp.features.task_details.generated.resources.task_details_mark_complete
import framezero.composeapp.features.task_details.generated.resources.task_details_retry
import framezero.composeapp.features.task_details.generated.resources.task_details_title
import framezero.composeapp.features.task_details.generated.resources.task_details_today
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

private val AvatarSize = 36.dp

@Composable
fun TaskDetailsScreen(
  component: TaskDetailsComponent,
  modifier: Modifier = Modifier
) {
  val state by component.state.collectAsStateWithLifecycle()
  TaskDetailsContent(
    state = state,
    onBack = component.onBack,
    onIntent = component::onIntent,
    modifier = modifier
  )
}

@Composable
internal fun TaskDetailsContent(
  state: TaskDetailsState,
  onBack: () -> Unit,
  onIntent: (TaskDetailsIntent) -> Unit,
  modifier: Modifier = Modifier
) {
  val colorSystem = AppTheme.colorSystem
  Box(
    modifier = modifier
      .fillMaxSize()
      .background(colorSystem.background)
      .systemBarsPadding()
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      TopToolbar(
        title = stringResource(Res.string.task_details_title),
        onBack = onBack
      )

      when {
        state.isLoading -> FullScreenProgress(modifier = Modifier.weight(1f))
        state.isError -> FullScreenError(
          modifier = Modifier.weight(1f),
          message = stringResource(Res.string.task_details_error),
          onRetry = { onIntent(TaskDetailsIntent.Refresh) },
          retryLabel = stringResource(Res.string.task_details_retry)
        )
        else -> {
          Column(
            modifier = Modifier
              .weight(1f)
              .verticalScroll(rememberScrollState())
              .padding(horizontal = AppTheme.spacingSystem.space16)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = state.productionName,
                style = AppTheme.typographySystem.bodyLarge,
                color = colorSystem.textSecondary
              )
              PriorityBadge(priority = state.priority)
            }
            VerticalSpacer(AppTheme.spacingSystem.space8)

            Text(
              text = state.title,
              style = AppTheme.typographySystem.displayMedium,
              color = colorSystem.textPrimary
            )
            VerticalSpacer(AppTheme.spacingSystem.space16)

            AssigneeDueRow(
              assignee = state.assignee,
              dueDate = state.dueDate,
              isDueToday = state.isDueToday
            )
            VerticalSpacer(AppTheme.spacingSystem.space24)

            if (state.description.isNotBlank()) {
              Text(
                text = state.description,
                style = AppTheme.typographySystem.bodyLarge,
                color = colorSystem.textSecondary
              )
            }

            state.attachment?.let { attachment ->
              VerticalSpacer(AppTheme.spacingSystem.space24)
              AttachmentCard(
                attachment = attachment,
                isDownloading = state.isDownloadingAttachment,
                errorMessage = state.attachmentError?.let { stringResource(it.messageRes()) },
                onClick = { onIntent(TaskDetailsIntent.DownloadAttachment) }
              )
            }
          }

          if (state.showMarkCompleteButton) {
            CtaButton(
              text = stringResource(Res.string.task_details_mark_complete),
              modifier = Modifier
                .fillMaxWidth()
                .padding(AppTheme.spacingSystem.space16),
              onClick = { onIntent(TaskDetailsIntent.MarkComplete) }
            )
          }
        }
      }
    }
  }
}

@Composable
private fun PriorityBadge(
  priority: TaskPriority,
  modifier: Modifier = Modifier
) {
  val (bgColor, textColor, label) = when (priority) {
    TaskPriority.HIGH -> Triple(
      AppTheme.colorSystem.priorityHighSurface,
      AppTheme.colorSystem.priorityHighText,
      "High"
    )
    TaskPriority.MEDIUM -> Triple(
      AppTheme.colorSystem.priorityMedSurface,
      AppTheme.colorSystem.priorityMedText,
      "Medium"
    )
    TaskPriority.LOW -> Triple(
      AppTheme.colorSystem.priorityLowSurface,
      AppTheme.colorSystem.priorityLowText,
      "Low"
    )
  }
  Text(
    text = label,
    style = AppTheme.typographySystem.labelMedium.copy(fontWeight = FontWeight.Bold),
    color = textColor,
    modifier = modifier
      .clip(RoundedCornerShape(AppTheme.radiusSystem.radius8))
      .background(bgColor)
      .padding(horizontal = AppTheme.spacingSystem.space8, vertical = AppTheme.spacingSystem.space4)
  )
}

@Composable
private fun AssigneeDueRow(
  assignee: TaskMember?,
  dueDate: LocalDate?,
  isDueToday: Boolean,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.Top
  ) {
    // Assignee column
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = stringResource(Res.string.task_details_assignee),
        style = AppTheme.typographySystem.caption.copy(fontWeight = FontWeight.Bold),
        color = AppTheme.colorSystem.textMuted
      )
      VerticalSpacer(AppTheme.spacingSystem.space8)
      assignee?.let { member ->
        Row(verticalAlignment = Alignment.CenterVertically) {
          val avatarColor = remember(member.avatarColorHex) {
            member.avatarColorHex?.let { parseHexColor(it) }
          } ?: AppTheme.colorSystem.accentDim
          Box(
            modifier = Modifier
              .size(AvatarSize)
              .clip(CircleShape)
              .background(avatarColor),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = member.initials,
              style = AppTheme.typographySystem.labelSmall,
              color = AppTheme.colorSystem.textOnAccent
            )
          }
          HorizontalSpacer(AppTheme.spacingSystem.space8)
          Text(
            text = member.name,
            style = AppTheme.typographySystem.titleSmall,
            color = AppTheme.colorSystem.textPrimary
          )
        }
      }
    }

    // Due column
    Column {
      Text(
        text = stringResource(Res.string.task_details_due_date),
        style = AppTheme.typographySystem.caption.copy(fontWeight = FontWeight.Bold),
        color = AppTheme.colorSystem.textMuted
      )
      VerticalSpacer(AppTheme.spacingSystem.space8)
      dueDate?.let { date ->
        if (isDueToday) {
          Text(
            text = stringResource(Res.string.task_details_today),
            style = AppTheme.typographySystem.titleSmall,
            color = AppTheme.colorSystem.errorText
          )
        } else {
          Text(
            text = date.toMediumDateLabel(),
            style = AppTheme.typographySystem.titleSmall,
            color = AppTheme.colorSystem.textPrimary
          )
        }
      }
    }
  }
}

@Composable
private fun AttachmentCard(
  attachment: TaskAttachment,
  isDownloading: Boolean,
  errorMessage: String?,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val colors = AppTheme.colorSystem
  val typography = AppTheme.typographySystem
  val spacing = AppTheme.spacingSystem

  Column(
    modifier = modifier
      .fillMaxWidth()
      .clickableWithRipple(color = colors.accentDim, onClick = onClick)
      .padding(vertical = spacing.space8)
  ) {
    Text(
      text = stringResource(Res.string.task_details_attachment),
      style = typography.caption.copy(fontWeight = FontWeight.Bold),
      color = colors.textMuted
    )
    VerticalSpacer(spacing.space8)
    Text(text = attachment.fileName, style = typography.titleSmall, color = colors.textPrimary)
    VerticalSpacer(spacing.space4)
    Text(text = attachment.sizeLabel, style = typography.bodySmall, color = colors.textMuted)
    VerticalSpacer(spacing.space4)
    val subtitle = when {
      errorMessage != null -> errorMessage
      isDownloading -> stringResource(Res.string.task_details_attachment_downloading)
      else -> stringResource(Res.string.task_details_attachment_open)
    }
    Text(
      text = subtitle,
      style = typography.bodySmall,
      color = if (errorMessage != null) colors.errorText else colors.accent
    )
  }
}

private fun AttachmentDownloadError.messageRes() =
  when (this) {
    AttachmentDownloadError.OFFLINE -> Res.string.task_details_attachment_error_offline
    AttachmentDownloadError.INSUFFICIENT_STORAGE -> Res.string.task_details_attachment_error_storage
    AttachmentDownloadError.GENERIC -> Res.string.task_details_attachment_error_generic
  }

@Suppress("MagicNumber")
private fun parseHexColor(hex: String): Color? {
  val cleaned = hex.removePrefix("#")
  return runCatching {
    Color(("FF$cleaned").toLong(16))
  }.getOrNull()
}

@LightDarkPreview
@Composable
private fun TaskDetailsContentPreview() {
  AppTheme {
    TaskDetailsContent(
      state = TaskDetailsState(
        taskId = "taskId",
        title = "Review Scene 12 script revisions",
        productionName = "Echoes of Silence",
        priority = TaskPriority.HIGH,
        status = TaskStatus.COMPLETED,
        assignee = TaskMember(
          initials = "MR",
          name = "Maya Rivera",
          avatarColorHex = "#0097A7"
        ),
        dueDate = LocalDate(2026, 4, 26),
        isDueToday = true,
        showMarkCompleteButton = true,
        description = "Writer turned in revised pages for the confrontation in Scene 12. " +
          "Review the new dialogue against the shooting schedule and flag any continuity " +
          "issues with the Act II callbacks before the table read."
      ),
      onBack = {},
      onIntent = {}
    )
  }
}
