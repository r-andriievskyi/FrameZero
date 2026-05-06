package com.frame.zero.feature.production.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.discovery.playground.shared.design_system.AppTheme
import com.discovery.playground.shared.design_system.widgets.CtaButton
import com.discovery.playground.shared.design_system.widgets.SingleLineInputField
import com.discovery.playground.shared.design_system.widgets.VerticalSpacer
import com.frame.zero.domain.production.Genre
import com.frame.zero.domain.production.ProductionPhase
import com.frame.zero.feature.production.CreateProductionComponent
import com.frame.zero.feature.production.CreateProductionIntent
import com.frame.zero.feature.production.CreateProductionState
import kotlinx.datetime.LocalDate

@Composable
fun CreateProductionContent(component: CreateProductionComponent) {
  val state by component.state.collectAsState()
  CreateProductionScreen(
    state = state,
    onIntent = component::onIntent,
    onBack = component.onBack
  )
}

@Composable
private fun CreateProductionScreen(
  state: CreateProductionState,
  onIntent: (CreateProductionIntent) -> Unit,
  onBack: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(AppTheme.colorSystem.background)
  ) {
    TopBar(onBack = onBack)

    Column(
      modifier = Modifier
        .weight(1f)
        .verticalScroll(rememberScrollState())
        .padding(horizontal = AppTheme.spacingSystem.space16)
    ) {
      VerticalSpacer(AppTheme.spacingSystem.space24)

      FieldLabel("Title")
      VerticalSpacer(AppTheme.spacingSystem.space8)
      SingleLineInputField(
        value = state.title,
        onValueChange = { onIntent(CreateProductionIntent.TitleChanged(it)) },
        placeholder = "e.g. Echoes of Silence",
        enabled = !state.isLoading
      )

      VerticalSpacer(AppTheme.spacingSystem.space16)

      FieldLabel("Genre")
      VerticalSpacer(AppTheme.spacingSystem.space8)
      EnumSelector(
        options = Genre.entries,
        selected = state.genre,
        label = { it.displayLabel() },
        onSelect = { onIntent(CreateProductionIntent.GenreChanged(it)) },
        enabled = !state.isLoading
      )

      VerticalSpacer(AppTheme.spacingSystem.space16)

      FieldLabel("Phase")
      VerticalSpacer(AppTheme.spacingSystem.space8)
      EnumSelector(
        options = ProductionPhase.entries,
        selected = state.phase,
        label = { it.displayLabel() },
        onSelect = { onIntent(CreateProductionIntent.PhaseChanged(it)) },
        enabled = !state.isLoading
      )

      VerticalSpacer(AppTheme.spacingSystem.space16)

      FieldLabel("Logline (optional)")
      VerticalSpacer(AppTheme.spacingSystem.space8)
      SingleLineInputField(
        value = state.logline,
        onValueChange = { onIntent(CreateProductionIntent.LoglineChanged(it)) },
        placeholder = "One-sentence pitch",
        enabled = !state.isLoading
      )

      VerticalSpacer(AppTheme.spacingSystem.space16)

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacingSystem.space8)
      ) {
        Column(modifier = Modifier.weight(1f)) {
          FieldLabel("Start date")
          VerticalSpacer(AppTheme.spacingSystem.space8)
          DateInputField(
            value = state.startDate,
            placeholder = "YYYY-MM-DD",
            enabled = !state.isLoading,
            onDateChange = { onIntent(CreateProductionIntent.StartDateChanged(it)) }
          )
        }
        Column(modifier = Modifier.weight(1f)) {
          FieldLabel("Wrap date")
          VerticalSpacer(AppTheme.spacingSystem.space8)
          DateInputField(
            value = state.wrapDate,
            placeholder = "YYYY-MM-DD",
            enabled = !state.isLoading,
            onDateChange = { onIntent(CreateProductionIntent.WrapDateChanged(it)) }
          )
        }
      }

      state.error?.let { error ->
        VerticalSpacer(AppTheme.spacingSystem.space16)
        Text(
          text = error,
          style = AppTheme.typographySystem.bodySmall,
          color = AppTheme.colorSystem.errorText
        )
      }

      VerticalSpacer(AppTheme.spacingSystem.space24)

      CtaButton(
        text = if (state.isLoading) "Creating…" else "Create production",
        modifier = Modifier.fillMaxWidth(),
        onClick = { if (!state.isLoading) onIntent(CreateProductionIntent.Submit) }
      )

      VerticalSpacer(AppTheme.spacingSystem.space24)
    }
  }
}

@Composable
private fun TopBar(onBack: () -> Unit) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(
        horizontal = AppTheme.spacingSystem.space16,
        vertical = AppTheme.spacingSystem.space16
      ),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .clip(RoundedCornerShape(AppTheme.radiusSystem.radiusMax))
        .clickable(onClick = onBack)
        .padding(AppTheme.spacingSystem.space8)
    ) {
      Text(
        text = "←",
        style = AppTheme.typographySystem.titleMedium,
        color = AppTheme.colorSystem.textPrimary
      )
    }
    Text(
      text = "New Production",
      style = AppTheme.typographySystem.titleLarge,
      color = AppTheme.colorSystem.textPrimary,
      modifier = Modifier.padding(start = AppTheme.spacingSystem.space8)
    )
  }
}

@Composable
private fun FieldLabel(text: String) {
  Text(
    text = text,
    style = AppTheme.typographySystem.labelMedium,
    color = AppTheme.colorSystem.textSecondary
  )
}

@Composable
private fun <T> EnumSelector(
  options: List<T>,
  selected: T,
  label: (T) -> String,
  onSelect: (T) -> Unit,
  enabled: Boolean
) {
  val shape = RoundedCornerShape(AppTheme.radiusSystem.radius8)
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(shape)
      .background(AppTheme.colorSystem.inputBackground)
      .border(1.dp, AppTheme.colorSystem.border, shape)
  ) {
    options.forEach { option ->
      val isSelected = option == selected
      Box(
        modifier = Modifier
          .weight(1f)
          .clip(shape)
          .background(if (isSelected) AppTheme.colorSystem.accent else AppTheme.colorSystem.inputBackground)
          .clickable(enabled = enabled) { onSelect(option) }
          .padding(vertical = AppTheme.spacingSystem.space8),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = label(option),
          style = AppTheme.typographySystem.labelSmall,
          color = if (isSelected) AppTheme.colorSystem.textOnAccent else AppTheme.colorSystem.textMuted
        )
      }
    }
  }
}

@Composable
private fun DateInputField(
  value: LocalDate?,
  placeholder: String,
  enabled: Boolean,
  onDateChange: (LocalDate) -> Unit
) {
  SingleLineInputField(
    value = value?.toString() ?: "",
    onValueChange = { raw ->
      runCatching { LocalDate.parse(raw) }.onSuccess { onDateChange(it) }
    },
    placeholder = placeholder,
    enabled = enabled
  )
}

private fun Genre.displayLabel(): String = name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }

private fun ProductionPhase.displayLabel(): String =
  when (this) {
    ProductionPhase.DEVELOPMENT -> "Dev"
    ProductionPhase.PRE_PRODUCTION -> "Pre"
    ProductionPhase.PRODUCTION -> "Prod"
    ProductionPhase.POST_PRODUCTION -> "Post"
    ProductionPhase.DISTRIBUTION -> "Dist"
  }
