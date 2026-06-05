package com.frame.zero.feature.production.ui.step_two

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.unit.dp
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.asColorFilter
import com.frame.zero.shared.design_system.modifier.clickableWithRipple
import framezero.composeapp.features.production.generated.resources.Res
import framezero.composeapp.features.production.generated.resources.crew_role_art
import framezero.composeapp.features.production.generated.resources.crew_role_director
import framezero.composeapp.features.production.generated.resources.crew_role_dp
import framezero.composeapp.features.production.generated.resources.crew_role_editor
import framezero.composeapp.features.production.generated.resources.crew_role_other
import framezero.composeapp.features.production.generated.resources.crew_role_producer
import framezero.composeapp.features.production.generated.resources.crew_role_sound
import framezero.composeapp.features.production.generated.resources.crew_role_writer
import framezero.composeapp.features.production.generated.resources.ic_chevron_down
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private val DropdownHeight = 32.dp
private val DropdownChevronSize = 16.dp

@Composable
internal fun RoleDropdown(
  selected: String,
  onSelect: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val crewRoles = listOf(
    stringResource(Res.string.crew_role_director),
    stringResource(Res.string.crew_role_producer),
    stringResource(Res.string.crew_role_writer),
    stringResource(Res.string.crew_role_dp),
    stringResource(Res.string.crew_role_editor),
    stringResource(Res.string.crew_role_sound),
    stringResource(Res.string.crew_role_art),
    stringResource(Res.string.crew_role_other)
  )
  var expanded by remember { mutableStateOf(false) }
  val shape = RoundedCornerShape(AppTheme.radiusSystem.radius8)

  Box(modifier = modifier) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clip(shape)
        .background(AppTheme.colorSystem.inputBackground)
        .border(AppTheme.borderSystem.hairline, AppTheme.colorSystem.border, shape)
        .clickableWithRipple(AppTheme.colorSystem.accentDim) { expanded = true }
        .padding(
          horizontal = AppTheme.spacingSystem.space16,
          vertical = AppTheme.spacingSystem.space8
        )
        .height(DropdownHeight),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(
        text = selected,
        style = AppTheme.typographySystem.bodyLarge,
        color = AppTheme.colorSystem.textPrimary
      )
      Image(
        painter = painterResource(Res.drawable.ic_chevron_down),
        colorFilter = AppTheme.colorSystem.textMuted.asColorFilter(),
        contentDescription = null,
        modifier = Modifier.size(DropdownChevronSize)
      )
    }

    DropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
      modifier = Modifier
        .fillMaxWidth(0.5f)
        .background(AppTheme.colorSystem.surfaceElevated)
    ) {
      crewRoles.forEach { role ->
        DropdownMenuItem(
          text = {
            Text(
              text = role,
              style = AppTheme.typographySystem.bodyMedium,
              color = if (role == selected) {
                AppTheme.colorSystem.accentText
              } else {
                AppTheme.colorSystem.textPrimary
              }
            )
          },
          onClick = {
            onSelect(role)
            expanded = false
          }
        )
      }
    }
  }
}

@LightDarkPreview
@Composable
private fun RoleDropdownPreview() {
  AppTheme {
    RoleDropdown(selected = "Director", onSelect = {})
  }
}
