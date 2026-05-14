package com.frame.zero.feature.account.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.discovery.playground.shared.design_system.AppTheme

private val AvatarSize = 56.dp
private val BorderWidth = 1.dp

@Composable
internal fun ProfileCard(
  name: String,
  role: String,
  initials: String,
  modifier: Modifier = Modifier,
) {
  val shape = RoundedCornerShape(AppTheme.radiusSystem.radius16)
  Row(
    modifier = modifier
      .fillMaxWidth()
      .clip(shape)
      .background(AppTheme.colorSystem.cardBackground, shape)
      .border(BorderWidth, AppTheme.colorSystem.cardBorder, shape)
      .padding(AppTheme.spacingSystem.space16),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(AvatarSize)
        .clip(RoundedCornerShape(AppTheme.radiusSystem.radius8))
        .background(AppTheme.colorSystem.accent),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = initials,
        style = AppTheme.typographySystem.titleMedium,
        color = AppTheme.colorSystem.textOnAccent,
        fontWeight = FontWeight.Bold
      )
    }
    Spacer(modifier = Modifier.width(AppTheme.spacingSystem.space16))
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = name,
        style = AppTheme.typographySystem.titleSmall,
        color = AppTheme.colorSystem.textPrimary
      )
      Spacer(modifier = Modifier.height(AppTheme.spacingSystem.space2))
      Text(
        text = role,
        style = AppTheme.typographySystem.bodySmall,
        color = AppTheme.colorSystem.textMuted
      )
    }
  }
}

@Preview
@Composable
private fun ProfileCardPreview() {
  AppTheme(darkTheme = true) {
    ProfileCard(
      name = "Maya Rivera",
      role = "Director · Studio Zero",
      initials = "MR"
    )
  }
}

