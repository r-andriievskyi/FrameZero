package com.frame.zero.feature.production.ui.step_one

import androidx.compose.foundation.Image
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
import androidx.compose.ui.Modifier
import com.frame.zero.feature.production.ui.parseDateInput
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.asColorFilter
import com.frame.zero.shared.design_system.modifier.clickableWithRipple
import com.frame.zero.shared.design_system.widgets.SingleLineInputField
import framezero.composeapp.features.production.generated.resources.Res
import framezero.composeapp.features.production.generated.resources.date_picker_cancel
import framezero.composeapp.features.production.generated.resources.date_picker_ok
import framezero.composeapp.features.production.generated.resources.ic_calendar_days
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DateInputField(
  value: LocalDate?,
  placeholder: String,
  enabled: Boolean,
  onDateChange: (LocalDate) -> Unit,
  modifier: Modifier = Modifier
) {
  var showPicker by remember { mutableStateOf(false) }

  val displayValue = value?.let {
    val monthNum = it.month.ordinal + 1
    "${it.day.toString().padStart(2, '0')}.${monthNum.toString().padStart(2, '0')}.${it.year}"
  } ?: ""

  SingleLineInputField(
    value = displayValue,
    onValueChange = { raw ->
      parseDateInput(raw)?.let { onDateChange(it) }
    },
    placeholder = placeholder,
    enabled = enabled,
    modifier = modifier,
    trailingContent = {
      Image(
        painter = painterResource(Res.drawable.ic_calendar_days),
        colorFilter = AppTheme.colorSystem.textMuted.asColorFilter(),
        contentDescription = null,
        modifier = Modifier.clickableWithRipple(color = AppTheme.colorSystem.accentDim, enabled = enabled) { showPicker = true }
      )
    }
  )

  if (showPicker) {
    val initialMillis = value?.toEpochDays()?.times(86_400_000L)
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
    DatePickerDialog(
      onDismissRequest = { showPicker = false },
      confirmButton = {
        TextButton(onClick = {
          datePickerState.selectedDateMillis?.let { millis ->
            onDateChange(LocalDate.fromEpochDays((millis / 86_400_000L).toInt()))
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

@LightDarkPreview
@Composable
private fun DateInputFieldPreview() {
  AppTheme {
    DateInputField(
      value = LocalDate(2026, 6, 15),
      placeholder = "DD.MM.YYYY",
      enabled = true,
      onDateChange = {}
    )
  }
}

