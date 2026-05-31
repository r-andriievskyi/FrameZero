package com.frame.zero.feature.production.ui.widgets

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import com.frame.zero.feature.production.CrewMemberEntry
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.widgets.HorizontalSpacer

private val RemoveButtonSize = 24.dp

@Composable
internal fun CrewMemberRow(
  member: CrewMemberEntry,
  onRemove: () -> Unit,
  modifier: Modifier = Modifier
) {
  val shape = RoundedCornerShape(AppTheme.radiusSystem.radius8)
  Row(
    modifier = modifier
      .fillMaxWidth()
      .clip(shape)
      .border(AppTheme.borderSystem.hairline, AppTheme.colorSystem.cardBorder, shape)
      .padding(AppTheme.spacingSystem.space8),
    verticalAlignment = Alignment.CenterVertically
  ) {
    CrewAvatar(member)
    HorizontalSpacer(AppTheme.spacingSystem.space8)
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = member.name,
        style = AppTheme.typographySystem.bodyMedium,
        color = AppTheme.colorSystem.textPrimary
      )
      Text(
        text = member.role,
        style = AppTheme.typographySystem.caption,
        color = AppTheme.colorSystem.textMuted
      )
    }
    Box(
      modifier = Modifier
        .size(RemoveButtonSize)
        .clip(CircleShape)
        .clickable(onClick = onRemove),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = "✕",
        style = AppTheme.typographySystem.labelSmall,
        color = AppTheme.colorSystem.textMuted
      )
    }
  }
}

@LightDarkPreview
@Composable
private fun CrewMemberRowPreview() {
  AppTheme {
    CrewMemberRow(
      member = CrewMemberEntry(name = "Jane Smith", role = "Director"),
      onRemove = {}
    )
  }
}

