package com.frame.zero.feature.production.details.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.discovery.playground.shared.design_system.AppTheme
import com.discovery.playground.shared.design_system.widgets.HorizontalSpacer

private val ActionButtonSize = 40.dp
private val OverflowMenuWidth = 200.dp
private val CardBorderWidth = 1.dp

@Composable
internal fun DetailsTopBar(
  title: String,
  canDelete: Boolean,
  onBack: () -> Unit,
  onDeleteClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(
        horizontal = AppTheme.spacingSystem.space16,
        vertical = AppTheme.spacingSystem.space16
      ),
    verticalAlignment = Alignment.CenterVertically
  ) {
    BackButton(onClick = onBack)
    HorizontalSpacer(AppTheme.spacingSystem.space8)
    Text(
      modifier = Modifier.weight(1f),
      text = title,
      style = AppTheme.typographySystem.titleLarge,
      color = AppTheme.colorSystem.textPrimary,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
    if (canDelete) {
      OverflowMenuButton(onDeleteClick = onDeleteClick)
    }
  }
}

@Composable
private fun BackButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .size(ActionButtonSize)
      .clip(RoundedCornerShape(AppTheme.radiusSystem.radius8))
      .background(AppTheme.colorSystem.cardBackground)
      .clickable(onClick = onClick),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = "‹",
      style = AppTheme.typographySystem.titleLarge,
      color = AppTheme.colorSystem.textPrimary
    )
  }
}

@Composable
private fun OverflowMenuButton(
  onDeleteClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  var expanded by remember { mutableStateOf(false) }
  Box(modifier = modifier) {
    Box(
      modifier = Modifier
        .size(ActionButtonSize)
        .clip(RoundedCornerShape(AppTheme.radiusSystem.radius8))
        .border(
          width = CardBorderWidth,
          color = AppTheme.colorSystem.cardBorder,
          shape = RoundedCornerShape(AppTheme.radiusSystem.radius8)
        )
        .clickable { expanded = true },
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = "···",
        style = AppTheme.typographySystem.titleMedium,
        color = AppTheme.colorSystem.textPrimary
      )
    }
    DropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
      modifier = Modifier
        .width(OverflowMenuWidth)
        .background(AppTheme.colorSystem.surfaceElevated)
    ) {
      DropdownMenuItem(
        text = {
          Text(
            text = "Delete production",
            style = AppTheme.typographySystem.bodyMedium,
            color = AppTheme.colorSystem.errorText
          )
        },
        onClick = {
          expanded = false
          onDeleteClick()
        }
      )
    }
  }
}

@Preview
@Composable
private fun DetailsTopBarPreview() {
  AppTheme(darkTheme = true) {
    DetailsTopBar(
      title = "Summer Campaign 2026",
      canDelete = true,
      onBack = {},
      onDeleteClick = {}
    )
  }
}

