package com.frame.zero.feature.production.ui.step_two

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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
import com.frame.zero.feature.production.ui.widgets.CrewAvatar
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.asColorFilter
import com.frame.zero.shared.design_system.modifier.clickableWithRipple
import com.frame.zero.shared.design_system.widgets.HorizontalSpacer
import framezero.composeapp.features.production.generated.resources.Res
import framezero.composeapp.features.production.generated.resources.ic_cross
import org.jetbrains.compose.resources.painterResource

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
      .border(AppTheme.borderSystem.hairline, AppTheme.colorSystem.border, shape)
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
        .clickableWithRipple(color = AppTheme.colorSystem.accentDim, onClick = onRemove)
        .padding(AppTheme.spacingSystem.space4),
      contentAlignment = Alignment.Center
    ) {
      Image(
        painter = painterResource(Res.drawable.ic_cross),
        colorFilter = AppTheme.colorSystem.textMuted.asColorFilter(),
        contentDescription = null
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

