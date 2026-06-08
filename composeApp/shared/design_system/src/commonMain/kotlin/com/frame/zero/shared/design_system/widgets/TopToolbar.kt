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
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.generated.resources.Res
import com.frame.zero.shared.design_system.generated.resources.cd_back
import com.frame.zero.shared.design_system.generated.resources.ic_chevron_left
import com.frame.zero.shared.design_system.modifier.clickableWithRipple
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private val BackButtonSize = 40.dp

@Composable
fun TopToolbar(
  title: String,
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
  trailingContent: @Composable RowScope.() -> Unit = {}
) {
  val spacingSystem = AppTheme.spacingSystem
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(
        horizontal = spacingSystem.space16,
        vertical = spacingSystem.space16
      ),
    verticalAlignment = Alignment.CenterVertically
  ) {
    BackButton(onClick = onBack)
    HorizontalSpacer(spacingSystem.space8)
    Text(
      modifier = Modifier.weight(1f),
      text = title,
      style = AppTheme.typographySystem.titleExtraLarge,
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
  val shape = rememberRoundedCornerShape(AppTheme.radiusSystem.radius8)
  val colorSystem = AppTheme.colorSystem
  Box(
    modifier = modifier
      .minimumInteractiveComponentSize()
      .size(BackButtonSize)
      .clip(shape)
      .background(colorSystem.cardBackground)
      .border(width = AppTheme.borderSystem.hairline, color = colorSystem.border, shape = shape)
      .clickableWithRipple(
        color = colorSystem.accentDim,
        bounded = true,
        role = Role.Button,
        onClick = onClick
      ),
    contentAlignment = Alignment.Center
  ) {
    Image(
      painter = painterResource(Res.drawable.ic_chevron_left),
      colorFilter = ColorFilter.tint(colorSystem.textPrimary),
      contentDescription = stringResource(Res.string.cd_back)
    )
  }
}

@LightDarkPreview
@Composable
private fun TopToolbarWithOverflowPreview() {
  AppTheme {
    TopToolbar(
      title = "Summer Campaign 2026",
      onBack = {},
      trailingContent = {
        OverflowMenu(
          items = persistentListOf(
            OverflowMenuItem(text = "Delete", isDestructive = true, onClick = {})
          )
        )
      }
    )
  }
}
