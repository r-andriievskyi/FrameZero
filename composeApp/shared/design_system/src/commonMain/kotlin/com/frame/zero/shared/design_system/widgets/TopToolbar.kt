package com.frame.zero.shared.design_system.widgets

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.generated.resources.Res
import com.frame.zero.shared.design_system.generated.resources.ic_chevron_left
import com.frame.zero.shared.design_system.modifier.clickableWithRipple
import org.jetbrains.compose.resources.painterResource

private val BackButtonSize = 40.dp

@Composable
fun TopToolbar(
  title: String,
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
  trailingContent: @Composable RowScope.() -> Unit = {}
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
    trailingContent()
  }
}

@Composable
private fun BackButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val shape = RoundedCornerShape(AppTheme.radiusSystem.radius8)
  Box(
    modifier = modifier
      .size(BackButtonSize)
      .clip(shape)
      .background(AppTheme.colorSystem.cardBackground)
      .border(width = AppTheme.borderSystem.hairline, color = AppTheme.colorSystem.border, shape = shape)
      .clickableWithRipple(
        color = AppTheme.colorSystem.accentDim,
        bounded = true,
        onClick = onClick
      ),
    contentAlignment = Alignment.Center
  ) {
    Image(
      painter = painterResource(Res.drawable.ic_chevron_left),
      colorFilter = ColorFilter.tint(AppTheme.colorSystem.textPrimary),
      contentDescription = null
    )
  }
}

@Preview
@Composable
private fun TopToolbarPreview() {
  AppTheme(darkTheme = true) {
    TopToolbar(
      title = "Summer Campaign 2026",
      onBack = {}
    )
  }
}

@Preview
@Composable
private fun TopToolbarWithOverflowPreview() {
  AppTheme(darkTheme = true) {
    TopToolbar(
      title = "Summer Campaign 2026",
      onBack = {},
      trailingContent = {
        OverflowMenu(
          items = listOf(
            OverflowMenuItem(text = "Delete", isDestructive = true, onClick = {})
          )
        )
      }
    )
  }
}
