package com.frame.zero.feature.production.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.frame.zero.feature.production.CrewMemberEntry
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview

private val CrewAvatarSize = 36.dp

@Composable
internal fun CrewAvatar(
  member: CrewMemberEntry,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .size(CrewAvatarSize)
      .clip(CircleShape)
      .background(AppTheme.colorSystem.accent),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = member.initials,
      style = AppTheme.typographySystem.labelSmall,
      color = AppTheme.colorSystem.textOnAccent
    )
  }
}

@LightDarkPreview
@Composable
private fun CrewAvatarPreview() {
  AppTheme {
    CrewAvatar(member = CrewMemberEntry(name = "Jane Smith", role = "Director"))
  }
}
