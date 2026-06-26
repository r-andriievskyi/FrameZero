package com.frame.zero.feature.task.create.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.dp
import com.frame.zero.feature.task.create.AssignableMemberUi
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.modifier.clickableWithRipple
import com.frame.zero.shared.design_system.widgets.HorizontalSpacer
import com.frame.zero.shared.design_system.widgets.SingleLineInputField
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import framezero.composeapp.features.task_create.generated.resources.Res
import framezero.composeapp.features.task_create.generated.resources.assignee_no_results
import framezero.composeapp.features.task_create.generated.resources.assignee_search_placeholder
import framezero.composeapp.features.task_create.generated.resources.assignee_unassign
import framezero.composeapp.features.task_create.generated.resources.assignee_unassigned
import org.jetbrains.compose.resources.stringResource

private val FieldHeight = 56.dp
private val AvatarSize = 32.dp
private val SheetListMaxHeight = 320.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AssigneeSelector(
  selected: AssignableMemberUi?,
  isPickerVisible: Boolean,
  query: String,
  members: ImmutableList<AssignableMemberUi>,
  onOpen: () -> Unit,
  onDismiss: () -> Unit,
  onQueryChange: (String) -> Unit,
  onSelect: (userId: String?) -> Unit,
  modifier: Modifier = Modifier
) {
  AssigneeField(selected = selected, onClick = onOpen, modifier = modifier)

  if (isPickerVisible) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
      val spacing = AppTheme.spacingSystem
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = spacing.space16)
          .padding(bottom = spacing.space24)
      ) {
        SingleLineInputField(
          value = query,
          onValueChange = onQueryChange,
          placeholder = stringResource(Res.string.assignee_search_placeholder)
        )
        VerticalSpacer(spacing.space12)
        LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = SheetListMaxHeight)) {
          item {
            AssigneeOptionRow(label = stringResource(Res.string.assignee_unassign), onClick = { onSelect(null) })
          }
          items(members, key = { it.userId }) { member ->
            AssigneeOptionRow(label = member.name, member = member, onClick = { onSelect(member.userId) })
          }
          if (members.isEmpty() && query.isNotBlank()) {
            item {
              Text(
                text = stringResource(Res.string.assignee_no_results),
                style = AppTheme.typographySystem.bodySmall,
                color = AppTheme.colorSystem.textMuted,
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
private fun AssigneeField(
  selected: AssignableMemberUi?,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val colors = AppTheme.colorSystem
  val spacing = AppTheme.spacingSystem
  val shape = RoundedCornerShape(AppTheme.radiusSystem.radius8)
  Row(
    modifier = modifier
      .fillMaxWidth()
      .height(FieldHeight)
      .clip(shape)
      .background(colors.inputBackground, shape)
      .border(width = AppTheme.borderSystem.hairline, color = colors.border, shape = shape)
      .clickableWithRipple(color = colors.accentDim, onClick = onClick)
      .padding(horizontal = spacing.space16),
    verticalAlignment = Alignment.CenterVertically
  ) {
    if (selected != null) {
      MemberAvatar(member = selected)
      HorizontalSpacer(spacing.space12)
      Text(
        text = selected.name,
        style = AppTheme.typographySystem.bodyLarge,
        color = colors.textPrimary,
        modifier = Modifier.weight(1f)
      )
    } else {
      Text(
        text = stringResource(Res.string.assignee_unassigned),
        style = AppTheme.typographySystem.bodyLarge,
        color = colors.textMuted,
        modifier = Modifier.weight(1f)
      )
    }
  }
}

@Composable
private fun AssigneeOptionRow(
  label: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  member: AssignableMemberUi? = null
) {
  val spacing = AppTheme.spacingSystem
  Row(
    modifier = modifier
      .fillMaxWidth()
      .clickableWithRipple(color = AppTheme.colorSystem.accentDim, onClick = onClick)
      .padding(vertical = spacing.space12),
    verticalAlignment = Alignment.CenterVertically
  ) {
    if (member != null) {
      MemberAvatar(member = member)
      HorizontalSpacer(spacing.space12)
    }
    Text(
      text = label,
      style = AppTheme.typographySystem.bodyLarge,
      color = AppTheme.colorSystem.textPrimary
    )
  }
}

@Composable
private fun MemberAvatar(
  member: AssignableMemberUi,
  modifier: Modifier = Modifier
) {
  val avatarColor = member.avatarColorHex?.let(::parseHexColor) ?: AppTheme.colorSystem.accentDim
  Box(
    modifier = modifier
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
}

@Suppress("MagicNumber")
private fun parseHexColor(hex: String): Color? =
  runCatching { Color(("FF" + hex.removePrefix("#")).toLong(16)) }.getOrNull()

@LightDarkPreview
@Composable
private fun AssigneeSelectorPreview() {
  AppTheme {
    AssigneeSelector(
      selected = AssignableMemberUi("u1", "Sara Lin", "SL", "#9C27B0"),
      isPickerVisible = false,
      query = "",
      members = persistentListOf(),
      onOpen = {},
      onDismiss = {},
      onQueryChange = {},
      onSelect = {},
      modifier = Modifier.padding(AppTheme.spacingSystem.space16)
    )
  }
}
