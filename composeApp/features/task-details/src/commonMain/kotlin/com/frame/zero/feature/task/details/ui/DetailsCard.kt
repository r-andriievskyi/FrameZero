package com.frame.zero.feature.task.details.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import com.frame.zero.feature.task.details.TaskMember
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.widgets.HorizontalSpacer
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import framezero.composeapp.features.task_details.generated.resources.Res
import framezero.composeapp.features.task_details.generated.resources.task_details_assignee
import framezero.composeapp.features.task_details.generated.resources.task_details_due_date
import framezero.composeapp.features.task_details.generated.resources.task_details_phase
import framezero.composeapp.features.task_details.generated.resources.task_details_reporter
import framezero.composeapp.features.task_details.generated.resources.task_details_today
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DetailsCard(
  assignee: TaskMember?,
  reporter: TaskMember?,
  dueDate: String?,
  isDueToday: Boolean,
  phase: String,
  modifier: Modifier = Modifier
) {
  val shape = RoundedCornerShape(AppTheme.radiusSystem.radius16)
  Column(
    modifier = modifier
      .fillMaxWidth()
      .clip(shape)
      .background(AppTheme.colorSystem.cardBackground)
      .padding(AppTheme.spacingSystem.space16)
  ) {
    // Assignee + Reporter row
    Row(modifier = Modifier.fillMaxWidth()) {
      Column(modifier = Modifier.weight(1f)) {
        SectionLabel(text = stringResource(Res.string.task_details_assignee))
        VerticalSpacer(AppTheme.spacingSystem.space8)
        assignee?.let { MemberRow(member = it) }
      }
      Column(modifier = Modifier.weight(1f)) {
        SectionLabel(text = stringResource(Res.string.task_details_reporter))
        VerticalSpacer(AppTheme.spacingSystem.space8)
        reporter?.let { MemberRow(member = it) }
      }
    }
    VerticalSpacer(AppTheme.spacingSystem.space16)

    // Due date + Phase row
    Row(modifier = Modifier.fillMaxWidth()) {
      Column(modifier = Modifier.weight(1f)) {
        SectionLabel(text = stringResource(Res.string.task_details_due_date))
        VerticalSpacer(AppTheme.spacingSystem.space8)
        dueDate?.let { date ->
          if (isDueToday) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = stringResource(Res.string.task_details_today),
                style = AppTheme.typographySystem.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = AppTheme.colorSystem.errorText
              )
              Text(
                text = "  ·  ",
                style = AppTheme.typographySystem.bodyMedium,
                color = AppTheme.colorSystem.errorText
              )
              Text(
                text = date,
                style = AppTheme.typographySystem.bodyMedium,
                color = AppTheme.colorSystem.errorText
              )
            }
          } else {
            Text(
              text = date,
              style = AppTheme.typographySystem.bodyMedium,
              color = AppTheme.colorSystem.textPrimary
            )
          }
        }
      }
      Column(modifier = Modifier.weight(1f)) {
        SectionLabel(text = stringResource(Res.string.task_details_phase))
        VerticalSpacer(AppTheme.spacingSystem.space8)
        Text(
          text = phase,
          style = AppTheme.typographySystem.bodyMedium,
          color = AppTheme.colorSystem.textPrimary
        )
      }
    }
  }
}

@Composable
internal fun MemberRow(
  member: TaskMember,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically
  ) {
    val avatarColor = member.avatarColorHex
      ?.let { parseHexColor(it) }
      ?: AppTheme.colorSystem.accentDim
    Box(
      modifier = Modifier
        .size(AvatarSize)
        .clip(CircleShape)
        .background(avatarColor),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = member.initials,
        style = AppTheme.typographySystem.labelMedium,
        color = AppTheme.colorSystem.textOnAccent
      )
    }
    HorizontalSpacer(AppTheme.spacingSystem.space8)
    Column {
      Text(
        text = member.name,
        style = AppTheme.typographySystem.titleSmall,
        color = AppTheme.colorSystem.textPrimary
      )
      VerticalSpacer(AppTheme.spacingSystem.space2)
      Text(
        text = member.role,
        style = AppTheme.typographySystem.bodySmall,
        color = AppTheme.colorSystem.textMuted
      )
    }
  }
}

@LightDarkPreview
@Composable
private fun DetailsCardDueTodayPreview() {
  AppTheme {
    DetailsCard(
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
      phase = "Production"
    )
  }
}

@LightDarkPreview
@Composable
private fun DetailsCardFutureDatePreview() {
  AppTheme {
    DetailsCard(
      assignee = TaskMember(
        initials = "SL",
        name = "Sara Lin",
        role = "Cinematographer",
        avatarColorHex = "#9C27B0"
      ),
      reporter = null,
      dueDate = "Jun 15, 2026",
      isDueToday = false,
      phase = "Pre-Production"
    )
  }
}

