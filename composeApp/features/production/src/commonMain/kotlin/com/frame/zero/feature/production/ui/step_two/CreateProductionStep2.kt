package com.frame.zero.feature.production.ui.step_two

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.asColorFilter
import com.frame.zero.ui.asString
import com.frame.zero.shared.design_system.modifier.clickableWithRipple
import com.frame.zero.shared.design_system.widgets.CtaButton
import com.frame.zero.shared.design_system.widgets.SingleLineInputField
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import com.frame.zero.feature.production.CreateProductionIntent
import com.frame.zero.feature.production.CreateProductionState
import com.frame.zero.feature.production.CrewMemberEntry
import com.frame.zero.feature.production.ui.widgets.ErrorText
import com.frame.zero.feature.production.ui.widgets.FieldLabel
import com.frame.zero.shared.design_system.LightDarkPreview
import framezero.composeapp.features.production.generated.resources.Res
import framezero.composeapp.features.production.generated.resources.cd_add_crew_member
import framezero.composeapp.features.production.generated.resources.create_button_continue
import framezero.composeapp.features.production.generated.resources.create_button_skip_continue
import framezero.composeapp.features.production.generated.resources.create_crew_empty
import framezero.composeapp.features.production.generated.resources.create_crew_owner_note
import framezero.composeapp.features.production.generated.resources.create_field_name
import framezero.composeapp.features.production.generated.resources.create_field_role
import framezero.composeapp.features.production.generated.resources.create_placeholder_name
import framezero.composeapp.features.production.generated.resources.create_step3_subtitle
import framezero.composeapp.features.production.generated.resources.create_step3_title
import framezero.composeapp.features.production.generated.resources.ic_plus
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private val AddCrewButtonSize = 48.dp

@Composable
internal fun Step2Content(
  state: CreateProductionState,
  onIntent: (CreateProductionIntent) -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = AppTheme.spacingSystem.space16)
  ) {
    Text(
      text = stringResource(Res.string.create_step3_title),
      style = AppTheme.typographySystem.displayMedium,
      color = AppTheme.colorSystem.textPrimary
    )
    VerticalSpacer(AppTheme.spacingSystem.space4)
    Text(
      text = stringResource(Res.string.create_step3_subtitle),
      style = AppTheme.typographySystem.bodyMedium,
      color = AppTheme.colorSystem.textSecondary
    )

    VerticalSpacer(AppTheme.spacingSystem.space24)

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(AppTheme.spacingSystem.space8),
      verticalAlignment = Alignment.Bottom
    ) {
      Column(modifier = Modifier.weight(1f)) {
        FieldLabel(stringResource(Res.string.create_field_name))
        VerticalSpacer(AppTheme.spacingSystem.space8)
        SingleLineInputField(
          value = state.crewNameInput,
          onValueChange = { onIntent(CreateProductionIntent.CrewNameChanged(it)) },
          placeholder = stringResource(Res.string.create_placeholder_name),
          enabled = !state.isLoading
        )
      }
      Column(modifier = Modifier.weight(1f)) {
        FieldLabel(stringResource(Res.string.create_field_role))
        VerticalSpacer(AppTheme.spacingSystem.space8)
        RoleDropdown(
          selected = state.crewRoleInput,
          onSelect = { onIntent(CreateProductionIntent.CrewRoleChanged(it)) }
        )
      }
      val addCrewLabel = stringResource(Res.string.cd_add_crew_member)
      Box(
        modifier = Modifier
          .size(AddCrewButtonSize)
          .clip(RoundedCornerShape(AppTheme.radiusSystem.radius8))
          .background(AppTheme.colorSystem.accent)
          .clickableWithRipple(
            color = AppTheme.colorSystem.textOnAccent,
            role = Role.Button
          ) {
            if (state.crewNameInput.isNotBlank()) {
              onIntent(CreateProductionIntent.AddCrewMember)
            }
          }
          .semantics { contentDescription = addCrewLabel },
        contentAlignment = Alignment.Center
      ) {
        Image(
          painterResource(Res.drawable.ic_plus),
          colorFilter = AppTheme.colorSystem.textOnAccent.asColorFilter(),
          contentDescription = null
        )
      }
    }

    VerticalSpacer(AppTheme.spacingSystem.space24)

    if (state.crewMembers.isEmpty()) {
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "🎬",
          style = AppTheme.typographySystem.displayLarge
        )
        VerticalSpacer(AppTheme.spacingSystem.space8)
        Text(
          text = stringResource(Res.string.create_crew_empty),
          style = AppTheme.typographySystem.bodyMedium,
          color = AppTheme.colorSystem.textMuted,
          textAlign = TextAlign.Center
        )
      }
    } else {
      state.crewMembers.forEachIndexed { index, member ->
        CrewMemberRow(
          member = member,
          onRemove = { onIntent(CreateProductionIntent.RemoveCrewMember(index)) }
        )
        if (index < state.crewMembers.lastIndex) {
          VerticalSpacer(AppTheme.spacingSystem.space8)
        }
      }
    }

    VerticalSpacer(AppTheme.spacingSystem.space16)

    Text(
      text = stringResource(Res.string.create_crew_owner_note),
      style = AppTheme.typographySystem.bodySmall,
      color = AppTheme.colorSystem.accentText,
      modifier = Modifier.fillMaxWidth(),
      textAlign = TextAlign.Center
    )

    state.error?.let { error ->
      VerticalSpacer(AppTheme.spacingSystem.space16)
      ErrorText(error.asString())
    }

    VerticalSpacer(AppTheme.spacingSystem.space24)

    CtaButton(
      text = if (state.crewMembers.isEmpty()) {
        stringResource(Res.string.create_button_skip_continue)
      } else {
        stringResource(Res.string.create_button_continue)
      },
      modifier = Modifier.fillMaxWidth(),
      onClick = { onIntent(CreateProductionIntent.NextStep) }
    )

    VerticalSpacer(AppTheme.spacingSystem.space24)
  }
}

@LightDarkPreview
@Composable
private fun Step2ContentEmptyCrewPreview() {
  AppTheme {
    Step2Content(
      state = CreateProductionState(),
      onIntent = {}
    )
  }
}

@LightDarkPreview
@Composable
private fun Step2ContentWithCrewPreview() {
  AppTheme {
    Step2Content(
      state = CreateProductionState(
        crewMembers = listOf(
          CrewMemberEntry("Jane Smith", "Director"),
          CrewMemberEntry("John Doe", "Producer")
        )
      ),
      onIntent = {}
    )
  }
}
