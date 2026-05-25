package com.frame.zero.feature.production.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.widgets.HorizontalSpacer
import com.frame.zero.shared.design_system.widgets.SingleLineInputField
import com.frame.zero.domain.production.ProductionPhase
import com.frame.zero.feature.production.CrewMemberEntry
import framezero.composeapp.features.production.generated.resources.Res
import framezero.composeapp.features.production.generated.resources.budget_placeholder
import framezero.composeapp.features.production.generated.resources.crew_role_art
import framezero.composeapp.features.production.generated.resources.crew_role_director
import framezero.composeapp.features.production.generated.resources.crew_role_dp
import framezero.composeapp.features.production.generated.resources.crew_role_editor
import framezero.composeapp.features.production.generated.resources.crew_role_other
import framezero.composeapp.features.production.generated.resources.crew_role_producer
import framezero.composeapp.features.production.generated.resources.crew_role_sound
import framezero.composeapp.features.production.generated.resources.crew_role_writer
import framezero.composeapp.features.production.generated.resources.date_picker_cancel
import framezero.composeapp.features.production.generated.resources.date_picker_ok
import framezero.composeapp.features.production.generated.resources.ic_calendar_days
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private val PhaseDotSize = 12.dp
private val RemoveButtonSize = 24.dp
private val CrewAvatarSize = 36.dp
private val DropdownHeight = 32.dp
private val DropdownMenuTopPadding = 52.dp

private val visiblePhases = listOf(
  ProductionPhase.IDEA,
  ProductionPhase.DEVELOPMENT,
  ProductionPhase.FINANCING,
  ProductionPhase.PRE_PRODUCTION,
  ProductionPhase.PRODUCTION,
  ProductionPhase.POST_PRODUCTION,
  ProductionPhase.MARKETING,
  ProductionPhase.DISTRIBUTION,
  ProductionPhase.RELEASE
)

// ── Field label ──────────────────────────────────────────────────────

@Composable
internal fun FieldLabel(
  text: String,
  modifier: Modifier = Modifier
) {
  Text(
    text = text,
    style = AppTheme.typographySystem.labelSmall,
    color = AppTheme.colorSystem.textSecondary,
    modifier = modifier
  )
}

// ── Error text ───────────────────────────────────────────────────────

@Composable
internal fun ErrorText(
  error: String,
  modifier: Modifier = Modifier
) {
  Text(
    text = error,
    style = AppTheme.typographySystem.bodySmall,
    color = AppTheme.colorSystem.errorText,
    modifier = modifier
  )
}

// ── Genre chip ───────────────────────────────────────────────────────

@Composable
internal fun GenreChip(
  label: String,
  isSelected: Boolean,
  onClick: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val shape = RoundedCornerShape(AppTheme.radiusSystem.radiusMax)
  Box(
    modifier = modifier
      .clip(shape)
      .background(
        if (isSelected) {
          AppTheme.colorSystem.accentSurface
        } else {
          AppTheme.colorSystem.cardBackground
        }
      )
      .border(
        BorderWidth,
        if (isSelected) AppTheme.colorSystem.accent else AppTheme.colorSystem.cardBorder,
        shape
      )
      .clickable(onClick = onClick)
      .padding(
        horizontal = AppTheme.spacingSystem.space16,
        vertical = AppTheme.spacingSystem.space8
      )
  ) {
    Text(
      text = label,
      style = AppTheme.typographySystem.labelSmall,
      color = if (isSelected) {
        AppTheme.colorSystem.accentText
      } else {
        AppTheme.colorSystem.textSecondary
      }
    )
  }
}

// ── Phase selector ───────────────────────────────────────────────────

@Composable
internal fun PhaseSelector(
  selected: ProductionPhase,
  onSelect: (ProductionPhase) -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(AppTheme.spacingSystem.space8)
  ) {
    visiblePhases.forEach { phase ->
      val isSelected = phase == selected
      val shape = RoundedCornerShape(AppTheme.radiusSystem.radius8)
      val borderColor = if (isSelected) phase.dotColor() else AppTheme.colorSystem.cardBorder
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(shape)
          .background(AppTheme.colorSystem.cardBackground)
          .border(
            width = if (isSelected) SelectedBorderWidth else BorderWidth,
            color = borderColor,
            shape = shape
          )
          .clickable { onSelect(phase) }
          .padding(
            horizontal = AppTheme.spacingSystem.space16,
            vertical = AppTheme.spacingSystem.space16
          ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(PhaseDotSize)
              .clip(CircleShape)
              .background(phase.dotColor())
          )
          HorizontalSpacer(AppTheme.spacingSystem.space8)
          Text(
            text = phase.label(),
            style = AppTheme.typographySystem.bodyMedium,
            color = if (isSelected) {
              AppTheme.colorSystem.textPrimary
            } else {
              AppTheme.colorSystem.textMuted
            }
          )
        }
      }
    }
  }
}

// ── Crew avatar ──────────────────────────────────────────────────────

@Composable
internal fun CrewAvatar(
  member: CrewMemberEntry,
  modifier: Modifier = Modifier
) {
  val initials = member.name.trim().split("\\s+".toRegex()).let { parts ->
    when {
      parts.size >= 2 ->
        "${parts.first().first().uppercaseChar()}${parts.last().first().uppercaseChar()}"
      parts.isNotEmpty() && parts[0].isNotEmpty() ->
        parts[0].first().uppercaseChar().toString()
      else -> "?"
    }
  }
  Box(
    modifier = modifier
      .size(CrewAvatarSize)
      .clip(CircleShape)
      .background(AppTheme.colorSystem.accent),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = initials,
      style = AppTheme.typographySystem.labelSmall,
      color = AppTheme.colorSystem.textOnAccent
    )
  }
}

// ── Crew member row ──────────────────────────────────────────────────

@Composable
internal fun CrewMemberRow(
  member: CrewMemberEntry,
  onRemove: () -> Unit,
  modifier: Modifier = Modifier
) {
  val shape = RoundedCornerShape(AppTheme.radiusSystem.radius8)
  Row(
    modifier = modifier
      .fillMaxWidth()
      .clip(shape)
      .border(BorderWidth, AppTheme.colorSystem.cardBorder, shape)
      .padding(AppTheme.spacingSystem.space8),
    verticalAlignment = Alignment.CenterVertically
  ) {
    CrewAvatar(member)
    HorizontalSpacer(AppTheme.spacingSystem.space8)
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = member.name,
        style = AppTheme.typographySystem.bodyMedium,
        color = AppTheme.colorSystem.textPrimary
      )
      Text(
        text = member.role,
        style = AppTheme.typographySystem.caption,
        color = AppTheme.colorSystem.textMuted
      )
    }
    Box(
      modifier = Modifier
        .size(RemoveButtonSize)
        .clip(CircleShape)
        .clickable(onClick = onRemove),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = "✕",
        style = AppTheme.typographySystem.labelSmall,
        color = AppTheme.colorSystem.textMuted
      )
    }
  }
}

// ── Role dropdown ────────────────────────────────────────────────────

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
        .border(BorderWidth, AppTheme.colorSystem.border, shape)
        .clickable { expanded = !expanded }
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
      Text(
        text = "⌄",
        style = AppTheme.typographySystem.bodyLarge,
        color = AppTheme.colorSystem.textMuted
      )
    }

    if (expanded) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = DropdownMenuTopPadding)
          .clip(shape)
          .background(AppTheme.colorSystem.surfaceElevated)
          .border(BorderWidth, AppTheme.colorSystem.border, shape)
      ) {
        crewRoles.forEach { role ->
          Text(
            text = role,
            style = AppTheme.typographySystem.bodyMedium,
            color = if (role == selected) {
              AppTheme.colorSystem.accentText
            } else {
              AppTheme.colorSystem.textPrimary
            },
            modifier = Modifier
              .fillMaxWidth()
              .clickable {
                onSelect(role)
                expanded = false
              }
              .padding(
                horizontal = AppTheme.spacingSystem.space16,
                vertical = AppTheme.spacingSystem.space8
              )
          )
        }
      }
    }
  }
}

// ── Budget input ─────────────────────────────────────────────────────

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

// ── Date input ───────────────────────────────────────────────────────

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
        contentDescription = null,
        modifier = Modifier.clickable(enabled = enabled) { showPicker = true }
      )
    }
  )

  if (showPicker) {
    val initialMillis = value?.toEpochDays()?.toLong()?.times(86_400_000L)
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

@Preview
@Composable
private fun GenreChipSelectedPreview() {
  AppTheme(darkTheme = true) {
    GenreChip(label = "Drama", isSelected = true, onClick = {})
  }
}

@Preview
@Composable
private fun GenreChipUnselectedPreview() {
  AppTheme(darkTheme = true) {
    GenreChip(label = "Comedy", isSelected = false, onClick = {})
  }
}

@Preview
@Composable
private fun PhaseSelectorPreview() {
  AppTheme(darkTheme = true) {
    PhaseSelector(
      selected = ProductionPhase.PRE_PRODUCTION,
      onSelect = {}
    )
  }
}

@Preview
@Composable
private fun CrewMemberRowPreview() {
  AppTheme(darkTheme = true) {
    CrewMemberRow(
      member = CrewMemberEntry(name = "Jane Smith", role = "Director"),
      onRemove = {}
    )
  }
}

@Preview
@Composable
private fun CrewAvatarPreview() {
  AppTheme(darkTheme = true) {
    CrewAvatar(member = CrewMemberEntry(name = "Jane Smith", role = "Director"))
  }
}

@Preview
@Composable
private fun RoleDropdownPreview() {
  AppTheme(darkTheme = true) {
    RoleDropdown(selected = "Director", onSelect = {})
  }
}

@Preview
@Composable
private fun BudgetInputFieldPreview() {
  AppTheme(darkTheme = true) {
    BudgetInputField(budgetCents = 50_000_00L, onBudgetChange = {}, enabled = true)
  }
}

@Preview
@Composable
private fun DateInputFieldPreview() {
  AppTheme(darkTheme = true) {
    DateInputField(
      value = LocalDate(2026, 6, 15),
      placeholder = "DD.MM.YYYY",
      enabled = true,
      onDateChange = {}
    )
  }
}

@Preview
@Composable
private fun FieldLabelPreview() {
  AppTheme(darkTheme = true) {
    FieldLabel(text = "Production Title")
  }
}

@Preview
@Composable
private fun ErrorTextPreview() {
  AppTheme(darkTheme = true) {
    ErrorText(error = "Title cannot be empty")
  }
}
