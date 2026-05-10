package com.frame.zero.feature.production.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.discovery.playground.shared.design_system.AppTheme
import com.discovery.playground.shared.design_system.widgets.CtaButton
import com.discovery.playground.shared.design_system.widgets.VerticalSpacer
import com.frame.zero.domain.production.ProductionPhase
import com.frame.zero.feature.production.CreateProductionIntent
import com.frame.zero.feature.production.CreateProductionState
import framezero.composeapp.features.production.generated.resources.Res
import framezero.composeapp.features.production.generated.resources.create_button_continue
import framezero.composeapp.features.production.generated.resources.create_field_budget
import framezero.composeapp.features.production.generated.resources.create_field_current_phase
import framezero.composeapp.features.production.generated.resources.create_field_start_date
import framezero.composeapp.features.production.generated.resources.create_field_wrap_date
import framezero.composeapp.features.production.generated.resources.create_placeholder_date
import framezero.composeapp.features.production.generated.resources.create_step2_subtitle
import framezero.composeapp.features.production.generated.resources.create_step2_title
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

// ── Step 2: Timeline & Phase ─────────────────────────────────────────

@Composable
internal fun Step2Content(
  state: CreateProductionState,
  onIntent: (CreateProductionIntent) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = AppTheme.spacingSystem.space16),
  ) {
    Text(
      text = stringResource(Res.string.create_step2_title),
      style = AppTheme.typographySystem.displayMedium,
      color = AppTheme.colorSystem.textPrimary,
    )
    VerticalSpacer(AppTheme.spacingSystem.space4)
    Text(
      text = stringResource(Res.string.create_step2_subtitle),
      style = AppTheme.typographySystem.bodyMedium,
      color = AppTheme.colorSystem.textSecondary,
    )

    VerticalSpacer(AppTheme.spacingSystem.space24)

    FieldLabel(stringResource(Res.string.create_field_current_phase))
    VerticalSpacer(AppTheme.spacingSystem.space8)
    PhaseSelector(
      selected = state.phase,
      onSelect = { onIntent(CreateProductionIntent.PhaseChanged(it)) },
    )

    VerticalSpacer(AppTheme.spacingSystem.space24)

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(AppTheme.spacingSystem.space8),
    ) {
      Column(modifier = Modifier.weight(1f)) {
        FieldLabel(stringResource(Res.string.create_field_start_date))
        VerticalSpacer(AppTheme.spacingSystem.space8)
        DateInputField(
          value = state.startDate,
          placeholder = stringResource(Res.string.create_placeholder_date),
          enabled = !state.isLoading,
          onDateChange = { onIntent(CreateProductionIntent.StartDateChanged(it)) },
        )
      }
      Column(modifier = Modifier.weight(1f)) {
        FieldLabel(stringResource(Res.string.create_field_wrap_date))
        VerticalSpacer(AppTheme.spacingSystem.space8)
        DateInputField(
          value = state.wrapDate,
          placeholder = stringResource(Res.string.create_placeholder_date),
          enabled = !state.isLoading,
          onDateChange = { onIntent(CreateProductionIntent.WrapDateChanged(it)) },
        )
      }
    }

    VerticalSpacer(AppTheme.spacingSystem.space24)

    FieldLabel(stringResource(Res.string.create_field_budget))
    VerticalSpacer(AppTheme.spacingSystem.space8)
    BudgetInputField(
      budgetCents = state.budgetCents,
      onBudgetChange = { onIntent(CreateProductionIntent.BudgetChanged(it)) },
      enabled = !state.isLoading,
    )

    state.error?.let { error ->
      VerticalSpacer(AppTheme.spacingSystem.space16)
      ErrorText(error)
    }

    VerticalSpacer(AppTheme.spacingSystem.space24)

    CtaButton(
      text = stringResource(Res.string.create_button_continue),
      modifier = Modifier.fillMaxWidth(),
      onClick = { onIntent(CreateProductionIntent.NextStep) },
    )

    VerticalSpacer(AppTheme.spacingSystem.space24)
  }
}

// ── Previews ─────────────────────────────────────────────────────────

@Preview
@Composable
private fun Step2ContentPreview() {
  AppTheme(darkTheme = true) {
    Step2Content(
      state = CreateProductionState(
        phase = ProductionPhase.PRE_PRODUCTION,
        startDate = LocalDate(2026, 8, 1),
        wrapDate = LocalDate(2026, 12, 15),
        budgetCents = 500_000_00L,
      ),
      onIntent = {},
    )
  }
}

@Preview
@Composable
private fun Step2ContentEmptyPreview() {
  AppTheme(darkTheme = true) {
    Step2Content(
      state = CreateProductionState(),
      onIntent = {},
    )
  }
}


