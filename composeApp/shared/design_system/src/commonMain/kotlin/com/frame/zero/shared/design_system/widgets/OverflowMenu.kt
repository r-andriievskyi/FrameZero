package com.frame.zero.shared.design_system.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.generated.resources.Res
import com.frame.zero.shared.design_system.generated.resources.cd_more_options
import com.frame.zero.shared.design_system.generated.resources.ic_dots_vertical
import com.frame.zero.shared.design_system.modifier.clickableWithRipple
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private val TriggerSize = 40.dp
private val MenuWidth = 200.dp

@Immutable
data class OverflowMenuItem(
  val text: String,
  val isDestructive: Boolean = false,
  val onClick: () -> Unit
)

@Composable
fun OverflowMenu(
  items: ImmutableList<OverflowMenuItem>,
  modifier: Modifier = Modifier
) {
  if (items.isEmpty()) return

  var expanded by remember { mutableStateOf(false) }
  val shape = RoundedCornerShape(AppTheme.radiusSystem.radius8)
  val moreOptionsLabel = stringResource(Res.string.cd_more_options)

  Box(modifier = modifier) {
    Box(
      modifier = Modifier
        .size(TriggerSize)
        .clip(shape)
        .border(
          width = AppTheme.borderSystem.hairline,
          color = AppTheme.colorSystem.cardBorder,
          shape = shape
        )
        .clickableWithRipple(
          color = AppTheme.colorSystem.accentDim,
          bounded = true,
          role = Role.Button,
          onClick = { expanded = true }
        )
        .semantics { contentDescription = moreOptionsLabel },
      contentAlignment = Alignment.Center
    ) {
      Icon(
        painterResource(Res.drawable.ic_dots_vertical),
        tint = AppTheme.colorSystem.textPrimary,
        contentDescription = null
      )
    }
    DropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
      modifier = Modifier
        .width(MenuWidth)
        .background(AppTheme.colorSystem.surfaceElevated)
    ) {
      items.forEach { item ->
        DropdownMenuItem(
          text = {
            Text(
              text = item.text,
              style = AppTheme.typographySystem.bodyMedium,
              color = if (item.isDestructive) {
                AppTheme.colorSystem.errorText
              } else {
                AppTheme.colorSystem.textPrimary
              }
            )
          },
          onClick = {
            expanded = false
            item.onClick()
          }
        )
      }
    }
  }
}

@LightDarkPreview
@Composable
private fun OverflowMenuPreview() {
  AppTheme {
    OverflowMenu(
      items = persistentListOf(
        OverflowMenuItem(text = "Edit", onClick = {}),
        OverflowMenuItem(text = "Delete", isDestructive = true, onClick = {})
      )
    )
  }
}
