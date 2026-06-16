package com.frame.zero.feature.task.create.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.frame.zero.feature.task.create.DueDateQuickOption
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.modifier.clickableWithRipple
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import framezero.composeapp.features.task_create.generated.resources.Res
import framezero.composeapp.features.task_create.generated.resources.date_picker_cancel
import framezero.composeapp.features.task_create.generated.resources.date_picker_ok
import framezero.composeapp.features.task_create.generated.resources.due_date_placeholder
import framezero.composeapp.features.task_create.generated.resources.due_next_week
import framezero.composeapp.features.task_create.generated.resources.due_this_week
import framezero.composeapp.features.task_create.generated.resources.due_today
import framezero.composeapp.features.task_create.generated.resources.due_tomorrow
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

private const val MILLIS_PER_DAY = 86_400_000L
private val FieldHeight = 48.dp

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun DueDateSection(
  dueDate: LocalDate?,
  onQuickSelect: (DueDateQuickOption) -> Unit,
  onDateChange: (LocalDate?) -> Unit,
  modifier: Modifier = Modifier
) {
  val colors = AppTheme.colorSystem
  val spacing = AppTheme.spacingSystem
  var showPicker by remember { mutableStateOf(false) }

  Column(modifier = modifier.fillMaxWidth()) {
    FlowRow(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(spacing.space8),
      verticalArrangement = Arrangement.spacedBy(spacing.space8)
    ) {
      QuickDateChip(stringResource(Res.string.due_today)) { onQuickSelect(DueDateQuickOption.TODAY) }
      QuickDateChip(stringResource(Res.string.due_tomorrow)) { onQuickSelect(DueDateQuickOption.TOMORROW) }
      QuickDateChip(stringResource(Res.string.due_this_week)) { onQuickSelect(DueDateQuickOption.THIS_WEEK) }
      QuickDateChip(stringResource(Res.string.due_next_week)) { onQuickSelect(DueDateQuickOption.NEXT_WEEK) }
    }

    VerticalSpacer(spacing.space12)

    val shape = RoundedCornerShape(AppTheme.radiusSystem.radius8)
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(FieldHeight)
        .clip(shape)
        .background(colors.inputBackground, shape)
        .border(width = AppTheme.borderSystem.hairline, color = colors.border, shape = shape)
        .clickableWithRipple(color = colors.accentDim) { showPicker = true }
        .padding(horizontal = spacing.space16),
      contentAlignment = Alignment.CenterStart
    ) {
      Text(
        text = dueDate?.let(::formatDate) ?: stringResource(Res.string.due_date_placeholder),
        style = AppTheme.typographySystem.bodyLarge,
        color = if (dueDate != null) colors.textPrimary else colors.textMuted
      )
    }
  }

  if (showPicker) {
    val initialMillis = dueDate?.toEpochDays()?.times(MILLIS_PER_DAY)
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
    DatePickerDialog(
      onDismissRequest = { showPicker = false },
      confirmButton = {
        TextButton(onClick = {
          datePickerState.selectedDateMillis?.let { millis ->
            onDateChange(LocalDate.fromEpochDays((millis / MILLIS_PER_DAY).toInt()))
          }
          showPicker = false
        }) {
          Text(stringResource(Res.string.date_picker_ok))
        }
      },
      dismissButton = {
        TextButton(onClick = { showPicker = false }) {
          Text(stringResource(Res.string.date_picker_cancel))
        }
      }
    ) {
      DatePicker(state = datePickerState)
    }
  }
}

@Composable
private fun QuickDateChip(
  label: String,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  val colors = AppTheme.colorSystem
  val shape = RoundedCornerShape(AppTheme.radiusSystem.radiusMax)
  Text(
    text = label,
    style = AppTheme.typographySystem.labelMedium,
    color = colors.textPrimary,
    modifier = modifier
      .clip(shape)
      .background(colors.inputBackground, shape)
      .border(width = AppTheme.borderSystem.hairline, color = colors.border, shape = shape)
      .clickableWithRipple(color = colors.accentDim, onClick = onClick)
      .padding(
        horizontal = AppTheme.spacingSystem.space16,
        vertical = AppTheme.spacingSystem.space8
      )
  )
}

private fun formatDate(date: LocalDate): String {
  val day = date.day.toString().padStart(2, '0')
  val month = (date.month.ordinal + 1).toString().padStart(2, '0')
  return "$day.$month.${date.year}"
}

@LightDarkPreview
@Composable
private fun DueDateSectionPreview() {
  AppTheme {
    DueDateSection(
      dueDate = LocalDate(2026, 5, 2),
      onQuickSelect = {},
      onDateChange = {},
      modifier = Modifier.padding(AppTheme.spacingSystem.space16)
    )
  }
}
