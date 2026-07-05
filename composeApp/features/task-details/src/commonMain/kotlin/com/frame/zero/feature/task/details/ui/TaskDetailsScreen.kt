package com.frame.zero.feature.task.details.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.frame.zero.feature.task.details.AssignableMemberUi
import com.frame.zero.feature.task.details.AttachmentDownloadError
import com.frame.zero.feature.task.details.TaskAttachment
import com.frame.zero.feature.task.details.TaskDetailsComponent
import com.frame.zero.feature.task.details.TaskDetailsIntent
import com.frame.zero.feature.task.details.TaskDetailsState
import com.frame.zero.feature.task.details.TaskMember
import com.frame.zero.feature.task.details.TaskPriority
import com.frame.zero.feature.task.details.TaskStatus
import com.frame.zero.feature.task.details.ui.components.AssigneeDueRow
import com.frame.zero.feature.task.details.ui.components.AttachmentCard
import com.frame.zero.feature.task.details.ui.components.ParticipantsSection
import com.frame.zero.feature.task.details.ui.components.PriorityBadge
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.modifier.clickableWithRipple
import com.frame.zero.shared.design_system.widgets.CtaButton
import com.frame.zero.shared.design_system.widgets.FullScreenError
import com.frame.zero.shared.design_system.widgets.FullScreenProgress
import com.frame.zero.shared.design_system.widgets.TopToolbar
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import com.frame.zero.shared.design_system.widgets.toast.ToastHost
import com.frame.zero.ui.asString
import framezero.composeapp.features.task_details.generated.resources.Res
import framezero.composeapp.features.task_details.generated.resources.task_details_attachment_error_generic
import framezero.composeapp.features.task_details.generated.resources.task_details_attachment_error_offline
import framezero.composeapp.features.task_details.generated.resources.task_details_attachment_error_storage
import framezero.composeapp.features.task_details.generated.resources.task_details_error
import framezero.composeapp.features.task_details.generated.resources.task_details_mark_complete
import framezero.composeapp.features.task_details.generated.resources.task_details_open_chat
import framezero.composeapp.features.task_details.generated.resources.task_details_retry
import framezero.composeapp.features.task_details.generated.resources.task_details_title
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

private val ChatButtonSize = 40.dp
private val ChatIconSize = 20.dp
private val UnreadBadgeMinSize = 18.dp
private val UnreadBadgeOverhang = 4.dp
private const val MaxUnreadBadgeCount = 99

@Composable
fun TaskDetailsScreen(
  component: TaskDetailsComponent,
  modifier: Modifier = Modifier
) {
  val state by component.state.collectAsStateWithLifecycle()
  Box(modifier = modifier.fillMaxSize()) {
    TaskDetailsContent(
      state = state,
      onBack = component.onBack,
      onOpenChat = component.onOpenChat,
      onIntent = component::onIntent
    )
    ToastHost(
      message = state.participantsError?.asString(),
      onDismiss = { component.onIntent(TaskDetailsIntent.ParticipantsErrorDismissed) }
    )
  }
}

@Composable
internal fun TaskDetailsContent(
  state: TaskDetailsState,
  onBack: () -> Unit,
  onIntent: (TaskDetailsIntent) -> Unit,
  modifier: Modifier = Modifier,
  onOpenChat: () -> Unit = {}
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
        onBack = onBack,
        trailingContent = {
          if (!state.isLoading && !state.isError) {
            ChatAction(onClick = onOpenChat, unreadCount = state.unreadChatCount)
          }
        }
      )

      when {
        state.isLoading ->
          FullScreenProgress(modifier = Modifier.weight(1f).testTag(TaskDetailsTestTags.LOADING))
        state.isError -> FullScreenError(
          modifier = Modifier.weight(1f).testTag(TaskDetailsTestTags.ERROR),
          message = stringResource(Res.string.task_details_error),
          onRetry = { onIntent(TaskDetailsIntent.Refresh) },
          retryLabel = stringResource(Res.string.task_details_retry)
        )
        else -> {
          Column(
            modifier = Modifier
              .weight(1f)
              .testTag(TaskDetailsTestTags.CONTENT)
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
                style = AppTheme.typographySystem.titleLarge,
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

            ParticipantsSection(
              participants = state.participants,
              isPickerVisible = state.isParticipantPickerVisible,
              query = state.participantQuery,
              members = state.filteredAssignableMembers,
              isUpdating = state.isUpdatingParticipants,
              onOpen = { onIntent(TaskDetailsIntent.ParticipantPickerOpened) },
              onDismiss = { onIntent(TaskDetailsIntent.ParticipantPickerDismissed) },
              onQueryChange = { onIntent(TaskDetailsIntent.ParticipantSearchChanged(it)) },
              onToggle = { onIntent(TaskDetailsIntent.ParticipantToggled(it)) }
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
                .testTag(TaskDetailsTestTags.MARK_COMPLETE)
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
private fun ChatAction(
  onClick: () -> Unit,
  unreadCount: Int,
  modifier: Modifier = Modifier
) {
  val colorSystem = AppTheme.colorSystem
  val shape = RoundedCornerShape(AppTheme.radiusSystem.radius8)
  // Wrapper doesn't clip, so the unread badge can overhang the button's top-end corner.
  Box(modifier = modifier) {
    Box(
      modifier = Modifier
        .testTag(TaskDetailsTestTags.OPEN_CHAT)
        .size(ChatButtonSize)
        .clip(shape)
        .background(colorSystem.cardBackground)
        .border(width = AppTheme.borderSystem.hairline, color = colorSystem.border, shape = shape)
        .clickableWithRipple(
          color = colorSystem.accentDim,
          bounded = true,
          role = Role.Button,
          onClickLabel = stringResource(Res.string.task_details_open_chat),
          onClick = onClick
        ),
      contentAlignment = Alignment.Center
    ) {
      val iconColor = colorSystem.textPrimary
      Canvas(modifier = Modifier.size(ChatIconSize)) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = size.minDimension * 0.1f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val bubble = Path().apply {
          addRoundRect(RoundRect(0f, 0f, w, h * 0.72f, CornerRadius(h * 0.22f)))
          // Tail dropping from the bottom-left of the bubble.
          moveTo(w * 0.30f, h * 0.68f)
          lineTo(w * 0.20f, h * 0.96f)
          lineTo(w * 0.50f, h * 0.68f)
        }
        drawPath(bubble, iconColor, style = stroke)
      }
    }
    if (unreadCount > 0) {
      UnreadBadge(
        count = unreadCount,
        modifier = Modifier
          .align(Alignment.TopEnd)
          .offset(x = UnreadBadgeOverhang, y = -UnreadBadgeOverhang)
      )
    }
  }
}

@Composable
private fun UnreadBadge(
  count: Int,
  modifier: Modifier = Modifier
) {
  val colorSystem = AppTheme.colorSystem
  val label = if (count > MaxUnreadBadgeCount) "$MaxUnreadBadgeCount+" else count.toString()
  Box(
    modifier = modifier
      .testTag(TaskDetailsTestTags.CHAT_UNREAD_BADGE)
      .defaultMinSize(minWidth = UnreadBadgeMinSize, minHeight = UnreadBadgeMinSize)
      .clip(RoundedCornerShape(AppTheme.radiusSystem.radiusMax))
      .background(colorSystem.accent)
      .padding(horizontal = AppTheme.spacingSystem.space4),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      style = AppTheme.typographySystem.labelSmall,
      color = colorSystem.textOnAccent
    )
  }
}

private fun AttachmentDownloadError.messageRes() =
  when (this) {
    AttachmentDownloadError.OFFLINE -> Res.string.task_details_attachment_error_offline
    AttachmentDownloadError.INSUFFICIENT_STORAGE -> Res.string.task_details_attachment_error_storage
    AttachmentDownloadError.GENERIC -> Res.string.task_details_attachment_error_generic
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
        participants = persistentListOf(
          AssignableMemberUi("u2", "Jake Morse", "JM", "#009688"),
          AssignableMemberUi("u3", "Priya Shah", "PS", "#3F51B5")
        ),
        attachment = TaskAttachment(
          fileName = "Scene12_rev4.pdf",
          typeLabel = "PDF",
          sizeLabel = "1.2 MB",
          contentType = "application/pdf",
          sizeBytes = 1_258_291
        ),
        description = "Writer turned in revised pages for the confrontation in Scene 12. " +
          "Review the new dialogue against the shooting schedule and flag any continuity " +
          "issues with the Act II callbacks before the table read."
      ),
      onBack = {},
      onIntent = {}
    )
  }
}
