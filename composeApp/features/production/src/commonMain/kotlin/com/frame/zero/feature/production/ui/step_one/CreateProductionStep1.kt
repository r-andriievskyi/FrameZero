package com.frame.zero.feature.production.ui.step_one

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.widgets.CtaButton
import com.frame.zero.shared.design_system.widgets.SingleLineInputField
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import com.frame.zero.domain.production.Genre
import com.frame.zero.feature.production.CreateProductionIntent
import com.frame.zero.feature.production.CreateProductionState
import com.frame.zero.feature.production.ui.widgets.ErrorText
import com.frame.zero.feature.production.ui.widgets.FieldLabel
import com.frame.zero.feature.production.ui.widgets.GenreChip
import com.frame.zero.feature.production.ui.displayLabel
import framezero.composeapp.features.production.generated.resources.Res
import framezero.composeapp.features.production.generated.resources.create_button_continue
import framezero.composeapp.features.production.generated.resources.create_field_budget
import framezero.composeapp.features.production.generated.resources.create_field_genre
import framezero.composeapp.features.production.generated.resources.create_field_production_title
import framezero.composeapp.features.production.generated.resources.create_field_start_date
import framezero.composeapp.features.production.generated.resources.create_field_wrap_date
import framezero.composeapp.features.production.generated.resources.create_placeholder_date
import framezero.composeapp.features.production.generated.resources.create_placeholder_production_title
import framezero.composeapp.features.production.generated.resources.create_step1_subtitle
import framezero.composeapp.features.production.generated.resources.create_step1_title
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

// ── Step 1: What are you making? ─────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun Step1Content(
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
      text = stringResource(Res.string.create_step1_title),
      style = AppTheme.typographySystem.displayMedium,
      color = AppTheme.colorSystem.textPrimary
    )
    VerticalSpacer(AppTheme.spacingSystem.space4)
    Text(
      text = stringResource(Res.string.create_step1_subtitle),
      style = AppTheme.typographySystem.bodyMedium,
      color = AppTheme.colorSystem.textSecondary
    )

    VerticalSpacer(AppTheme.spacingSystem.space24)

    FieldLabel(stringResource(Res.string.create_field_production_title))
    VerticalSpacer(AppTheme.spacingSystem.space8)
    SingleLineInputField(
      value = state.title,
      onValueChange = { onIntent(CreateProductionIntent.TitleChanged(it)) },
      placeholder = stringResource(Res.string.create_placeholder_production_title),
      enabled = !state.isLoading
    )

    VerticalSpacer(AppTheme.spacingSystem.space24)

    FieldLabel(stringResource(Res.string.create_field_genre))
    VerticalSpacer(AppTheme.spacingSystem.space8)
    FlowRow(
      horizontalArrangement = Arrangement.spacedBy(AppTheme.spacingSystem.space8),
      verticalArrangement = Arrangement.spacedBy(AppTheme.spacingSystem.space8)
    ) {
      Genre.entries.forEach { genre ->
        GenreChip(
          label = genre.displayLabel(),
          isSelected = genre == state.genre,
          onClick = { onIntent(CreateProductionIntent.GenreChanged(genre)) }
        )
      }
    }

    VerticalSpacer(AppTheme.spacingSystem.space24)

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(AppTheme.spacingSystem.space8)
    ) {
      Column(modifier = Modifier.weight(1f)) {
        FieldLabel(stringResource(Res.string.create_field_start_date))
        VerticalSpacer(AppTheme.spacingSystem.space8)
        DateInputField(
          value = state.startDate,
          placeholder = stringResource(Res.string.create_placeholder_date),
          enabled = !state.isLoading,
          onDateChange = { onIntent(CreateProductionIntent.StartDateChanged(it)) }
        )
      }
      Column(modifier = Modifier.weight(1f)) {
        FieldLabel(stringResource(Res.string.create_field_wrap_date))
        VerticalSpacer(AppTheme.spacingSystem.space8)
        DateInputField(
          value = state.wrapDate,
          placeholder = stringResource(Res.string.create_placeholder_date),
          enabled = !state.isLoading,
          onDateChange = { onIntent(CreateProductionIntent.WrapDateChanged(it)) }
        )
      }
    }

    VerticalSpacer(AppTheme.spacingSystem.space24)

    FieldLabel(stringResource(Res.string.create_field_budget))
    VerticalSpacer(AppTheme.spacingSystem.space8)
    BudgetInputField(
      budgetCents = state.budgetCents,
      onBudgetChange = { onIntent(CreateProductionIntent.BudgetChanged(it)) },
      enabled = !state.isLoading
    )

    state.error?.let { error ->
      VerticalSpacer(AppTheme.spacingSystem.space16)
      ErrorText(error)
    }

    VerticalSpacer(AppTheme.spacingSystem.space24)

    CtaButton(
      text = stringResource(Res.string.create_button_continue),
      modifier = Modifier.fillMaxWidth(),
      onClick = { onIntent(CreateProductionIntent.NextStep) }
    )

    VerticalSpacer(AppTheme.spacingSystem.space24)
  }
}

@Preview
@Composable
private fun Step1ContentPreview() {
  AppTheme(darkTheme = true) {
    Step1Content(
      state = CreateProductionState(
        title = "Echoes of Silence",
        genre = Genre.DRAMA,
        startDate = LocalDate(2026, 8, 1),
        wrapDate = LocalDate(2026, 12, 15),
        budgetCents = 500_000_00L
      ),
      onIntent = {}
    )
  }
}

@Preview
@Composable
private fun Step1ContentEmptyPreview() {
  AppTheme(darkTheme = true) {
    Step1Content(
      state = CreateProductionState(),
      onIntent = {}
    )
  }
}
