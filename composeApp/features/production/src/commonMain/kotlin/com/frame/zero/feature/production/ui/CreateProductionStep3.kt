package com.frame.zero.feature.production.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.discovery.playground.shared.design_system.AppTheme
import com.discovery.playground.shared.design_system.widgets.CtaButton
import com.discovery.playground.shared.design_system.widgets.SingleLineInputField
import com.discovery.playground.shared.design_system.widgets.VerticalSpacer
import com.frame.zero.feature.production.CreateProductionIntent
import com.frame.zero.feature.production.CreateProductionState
import com.frame.zero.feature.production.CrewMemberEntry
import framezero.composeapp.features.production.generated.resources.Res
import framezero.composeapp.features.production.generated.resources.create_button_continue
import framezero.composeapp.features.production.generated.resources.create_button_skip_continue
import framezero.composeapp.features.production.generated.resources.create_crew_empty
import framezero.composeapp.features.production.generated.resources.create_crew_owner_note
import framezero.composeapp.features.production.generated.resources.create_field_name
import framezero.composeapp.features.production.generated.resources.create_field_role
import framezero.composeapp.features.production.generated.resources.create_placeholder_name
import framezero.composeapp.features.production.generated.resources.create_step3_subtitle
import framezero.composeapp.features.production.generated.resources.create_step3_title
import org.jetbrains.compose.resources.stringResource

private val AddCrewButtonSize = 48.dp

// ── Step 3: Build your team ──────────────────────────────────────────

@Composable
internal fun Step3Content(
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
      Box(
        modifier = Modifier
          .size(AddCrewButtonSize)
          .clip(RoundedCornerShape(AppTheme.radiusSystem.radius8))
          .background(AppTheme.colorSystem.accent)
          .clickable {
            if (state.crewNameInput.isNotBlank()) {
              onIntent(CreateProductionIntent.AddCrewMember)
            }
          },
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "+",
          style = AppTheme.typographySystem.titleMedium,
          color = AppTheme.colorSystem.textOnAccent
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
      ErrorText(error)
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

// ── Previews ─────────────────────────────────────────────────────────

@Preview
@Composable
private fun Step3ContentEmptyCrewPreview() {
  AppTheme(darkTheme = true) {
    Step3Content(
      state = CreateProductionState(),
      onIntent = {}
    )
  }
}

@Preview
@Composable
private fun Step3ContentWithCrewPreview() {
  AppTheme(darkTheme = true) {
    Step3Content(
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
