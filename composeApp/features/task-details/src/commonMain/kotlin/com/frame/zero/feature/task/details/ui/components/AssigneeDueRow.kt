package com.frame.zero.feature.task.details.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.frame.zero.core.format.formatMedium
import com.frame.zero.feature.task.details.TaskMember
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.widgets.HorizontalSpacer
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import framezero.composeapp.features.task_details.generated.resources.Res
import framezero.composeapp.features.task_details.generated.resources.task_details_assignee
import framezero.composeapp.features.task_details.generated.resources.task_details_due_date
import framezero.composeapp.features.task_details.generated.resources.task_details_today
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

private val AvatarSize = 36.dp

@Composable
internal fun AssigneeDueRow(
  assignee: TaskMember?,
  dueDate: LocalDate?,
  isDueToday: Boolean,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.Top
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = stringResource(Res.string.task_details_assignee),
        style = AppTheme.typographySystem.labelLarge,
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
            text = date.formatMedium(),
            style = AppTheme.typographySystem.titleSmall,
            color = AppTheme.colorSystem.textPrimary
          )
        }
      }
    }
  }
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
private fun AssigneeDueRowPreview() {
  AppTheme {
    AssigneeDueRow(
      assignee = TaskMember(
        initials = "MR",
        name = "Maya Rivera",
        avatarColorHex = "#0097A7"
      ),
      dueDate = LocalDate(2026, 4, 26),
      isDueToday = true,
      modifier = Modifier
        .background(AppTheme.colorSystem.background)
        .padding(AppTheme.spacingSystem.space16)
    )
  }
}
