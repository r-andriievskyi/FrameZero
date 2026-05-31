package com.frame.zero.feature.production.ui.widgets

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.widgets.SingleLineInputField
import framezero.composeapp.features.production.generated.resources.Res
import framezero.composeapp.features.production.generated.resources.budget_placeholder
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun BudgetInputField(
  budgetCents: Long?,
  onBudgetChange: (Long?) -> Unit,
  enabled: Boolean,
  modifier: Modifier = Modifier
) {
  val displayValue = budgetCents?.let { (it / 100).toString() } ?: ""
  SingleLineInputField(
    value = displayValue,
    onValueChange = { raw ->
      if (raw.isBlank()) {
        onBudgetChange(null)
      } else {
        raw.filter { it.isDigit() }.toLongOrNull()?.let { onBudgetChange(it * 100) }
      }
    },
    placeholder = stringResource(Res.string.budget_placeholder),
    enabled = enabled,
    modifier = modifier,
    leadingContent = {
      Text(
        text = "$",
        style = AppTheme.typographySystem.bodyLarge,
        color = AppTheme.colorSystem.textMuted
      )
    }
  )
}

@LightDarkPreview
@Composable
private fun BudgetInputFieldPreview() {
  AppTheme {
    BudgetInputField(budgetCents = 50_000_00L, onBudgetChange = {}, enabled = true)
  }
}

