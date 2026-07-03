package com.frame.zero.feature.task.details.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.frame.zero.feature.task.details.AssignableMemberUi
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.modifier.clickableWithRipple
import com.frame.zero.shared.design_system.widgets.HorizontalSpacer
import com.frame.zero.shared.design_system.widgets.SingleLineInputField
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import framezero.composeapp.features.task_details.generated.resources.Res
import framezero.composeapp.features.task_details.generated.resources.task_details_participants
import framezero.composeapp.features.task_details.generated.resources.task_details_participants_manage
import framezero.composeapp.features.task_details.generated.resources.task_details_participants_no_results
import framezero.composeapp.features.task_details.generated.resources.task_details_participants_none
import framezero.composeapp.features.task_details.generated.resources.task_details_participants_search_placeholder
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.stringResource

private val ChipAvatarSize = 24.dp
private val RowAvatarSize = 32.dp
private val SheetListMaxHeight = 320.dp

/**
 * Read display of the task's participants plus the entry point to manage them: a chip per
 * current participant and a "Manage" affordance that opens a multi-select picker of the
 * production's assignable members (mirrors task-create's participants picker).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ParticipantsSection(
  participants: ImmutableList<AssignableMemberUi>,
  isPickerVisible: Boolean,
  query: String,
  members: ImmutableList<AssignableMemberUi>,
  isUpdating: Boolean,
  onOpen: () -> Unit,
  onDismiss: () -> Unit,
  onQueryChange: (String) -> Unit,
  onToggle: (userId: String) -> Unit,
  modifier: Modifier = Modifier
) {
  val colors = AppTheme.colorSystem
  val spacing = AppTheme.spacingSystem

  Column(modifier = modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = stringResource(Res.string.task_details_participants),
        style = AppTheme.typographySystem.labelLarge,
        color = colors.textMuted
      )
      Text(
        text = stringResource(Res.string.task_details_participants_manage),
        style = AppTheme.typographySystem.labelLarge,
        color = colors.textSecondary,
        modifier = Modifier.clickableWithRipple(color = colors.accentDim, onClick = onOpen)
      )
    }
    VerticalSpacer(spacing.space8)

    if (participants.isEmpty()) {
      Text(
        text = stringResource(Res.string.task_details_participants_none),
        style = AppTheme.typographySystem.bodyMedium,
        color = colors.textMuted
      )
    } else {
      @OptIn(ExperimentalLayoutApi::class)
      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(spacing.space8),
        verticalArrangement = Arrangement.spacedBy(spacing.space8)
      ) {
        participants.forEach { member -> ParticipantChip(member = member) }
      }
    }
  }

  if (isPickerVisible) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = spacing.space16)
          .padding(bottom = spacing.space24)
      ) {
        SingleLineInputField(
          value = query,
          onValueChange = onQueryChange,
          placeholder = stringResource(Res.string.task_details_participants_search_placeholder)
        )
        VerticalSpacer(spacing.space12)
        LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = SheetListMaxHeight)) {
          items(members, key = { it.userId }) { member ->
            ParticipantOptionRow(
              member = member,
              isSelected = participants.any { it.userId == member.userId },
              enabled = !isUpdating,
              onClick = { onToggle(member.userId) }
            )
          }
          if (members.isEmpty() && query.isNotBlank()) {
            item {
              Text(
                text = stringResource(Res.string.task_details_participants_no_results),
                style = AppTheme.typographySystem.bodySmall,
                color = colors.textMuted,
                modifier = Modifier.padding(vertical = spacing.space12)
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ParticipantChip(
  member: AssignableMemberUi,
  modifier: Modifier = Modifier
) {
  val colors = AppTheme.colorSystem
  val spacing = AppTheme.spacingSystem
  val shape = RoundedCornerShape(AppTheme.radiusSystem.radius8)
  Row(
    modifier = modifier
      .clip(shape)
      .background(colors.accentDim, shape)
      .padding(horizontal = spacing.space8, vertical = spacing.space4),
    verticalAlignment = Alignment.CenterVertically
  ) {
    MemberAvatar(member = member, size = ChipAvatarSize)
    HorizontalSpacer(spacing.space8)
    Text(
      text = member.name,
      style = AppTheme.typographySystem.labelMedium,
      color = colors.textOnAccent
    )
  }
}

@Composable
private fun ParticipantOptionRow(
  member: AssignableMemberUi,
  isSelected: Boolean,
  enabled: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val colors = AppTheme.colorSystem
  val spacing = AppTheme.spacingSystem
  val shape = RoundedCornerShape(AppTheme.radiusSystem.radius8)
  Row(
    modifier = modifier
      .fillMaxWidth()
      .clip(shape)
      .background(if (isSelected) colors.accentDim else Color.Transparent, shape)
      .clickableWithRipple(color = colors.accentDim, onClick = onClick, enabled = enabled)
      .padding(horizontal = spacing.space8, vertical = spacing.space12),
    verticalAlignment = Alignment.CenterVertically
  ) {
    MemberAvatar(member = member, size = RowAvatarSize)
    HorizontalSpacer(spacing.space12)
    Text(
      text = member.name,
      style = AppTheme.typographySystem.bodyLarge,
      color = if (isSelected) colors.textOnAccent else colors.textPrimary,
      modifier = Modifier.weight(1f)
    )
  }
}

@Composable
private fun MemberAvatar(
  member: AssignableMemberUi,
  size: Dp,
  modifier: Modifier = Modifier
) {
  val avatarColor = member.avatarColorHex?.let(::parseHexColor) ?: AppTheme.colorSystem.accentDim
  Box(
    modifier = modifier
      .size(size)
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
}

@Suppress("MagicNumber")
private fun parseHexColor(hex: String): Color? =
  runCatching { Color(("FF" + hex.removePrefix("#")).toLong(16)) }.getOrNull()

@LightDarkPreview
@Composable
private fun ParticipantsSectionPreview() {
  AppTheme {
    ParticipantsSection(
      participants = persistentListOf(
        AssignableMemberUi("u1", "Sara Lin", "SL", "#9C27B0"),
        AssignableMemberUi("u2", "Jake Morse", "JM", "#009688")
      ),
      isPickerVisible = false,
      query = "",
      members = persistentListOf(),
      isUpdating = false,
      onOpen = {},
      onDismiss = {},
      onQueryChange = {},
      onToggle = {},
      modifier = Modifier
        .background(AppTheme.colorSystem.background)
        .padding(AppTheme.spacingSystem.space16)
    )
  }
}
