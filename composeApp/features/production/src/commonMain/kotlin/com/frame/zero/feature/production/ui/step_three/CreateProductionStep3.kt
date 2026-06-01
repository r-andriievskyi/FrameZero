package com.frame.zero.feature.production.ui.step_three

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.frame.zero.domain.production.Genre
import com.frame.zero.feature.production.CreateProductionIntent
import com.frame.zero.feature.production.CreateProductionState
import com.frame.zero.feature.production.CrewMemberEntry
import com.frame.zero.feature.production.ui.displayLabel
import com.frame.zero.feature.production.ui.formatDisplay
import com.frame.zero.feature.production.ui.widgets.CrewAvatar
import com.frame.zero.feature.production.ui.widgets.ErrorText
import com.frame.zero.feature.production.ui.widgets.FieldLabel
import com.frame.zero.feature.production.ui.widgets.GenreChip
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.widgets.CtaButton
import com.frame.zero.ui.asString
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import framezero.composeapp.features.production.generated.resources.Res
import framezero.composeapp.features.production.generated.resources.create_button_create
import framezero.composeapp.features.production.generated.resources.create_button_creating
import framezero.composeapp.features.production.generated.resources.create_crew_label
import framezero.composeapp.features.production.generated.resources.create_review_budget
import framezero.composeapp.features.production.generated.resources.create_review_crew
import framezero.composeapp.features.production.generated.resources.create_review_members_count
import framezero.composeapp.features.production.generated.resources.create_review_not_set
import framezero.composeapp.features.production.generated.resources.create_review_start
import framezero.composeapp.features.production.generated.resources.create_review_untitled
import framezero.composeapp.features.production.generated.resources.create_review_wrap
import framezero.composeapp.features.production.generated.resources.create_step4_subtitle
import framezero.composeapp.features.production.generated.resources.create_step4_title
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

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
      text = stringResource(Res.string.create_step4_title),
      style = AppTheme.typographySystem.displayMedium,
      color = AppTheme.colorSystem.textPrimary
    )
    VerticalSpacer(AppTheme.spacingSystem.space4)
    Text(
      text = stringResource(Res.string.create_step4_subtitle),
      style = AppTheme.typographySystem.bodyMedium,
      color = AppTheme.colorSystem.textSecondary
    )

    VerticalSpacer(AppTheme.spacingSystem.space24)

    ReviewCard(state = state)

    VerticalSpacer(AppTheme.spacingSystem.space24)

    if (state.crewMembers.isNotEmpty()) {
      FieldLabel(stringResource(Res.string.create_crew_label, state.crewMembers.size))
      VerticalSpacer(AppTheme.spacingSystem.space8)
      Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacingSystem.space8)) {
        state.crewMembers.forEach { member ->
          CrewAvatar(member)
        }
      }
      VerticalSpacer(AppTheme.spacingSystem.space24)
    }

    state.error?.let { error ->
      ErrorText(error.asString())
      VerticalSpacer(AppTheme.spacingSystem.space16)
    }

    CtaButton(
      text = if (state.isLoading) {
        stringResource(Res.string.create_button_creating)
      } else {
        stringResource(Res.string.create_button_create)
      },
      modifier = Modifier.fillMaxWidth(),
      onClick = { if (!state.isLoading) onIntent(CreateProductionIntent.Submit) }
    )

    VerticalSpacer(AppTheme.spacingSystem.space24)
  }
}

// ── Review card ──────────────────────────────────────────────────────

@Composable
internal fun ReviewCard(
  state: CreateProductionState,
  modifier: Modifier = Modifier
) {
  val cardShape = RoundedCornerShape(AppTheme.radiusSystem.radius16)

  Column(
    modifier = modifier
      .fillMaxWidth()
      .clip(cardShape)
      .border(AppTheme.borderSystem.hairline, AppTheme.colorSystem.cardBorder, cardShape)
      .background(AppTheme.colorSystem.cardBackground)
  ) {
    Column(modifier = Modifier.padding(AppTheme.spacingSystem.space16)) {
      Text(
        text = state.title.ifBlank { stringResource(Res.string.create_review_untitled) },
        style = AppTheme.typographySystem.titleLarge,
        color = AppTheme.colorSystem.textPrimary
      )

      VerticalSpacer(AppTheme.spacingSystem.space8)

      GenreChip(
        label = state.genre.displayLabel(),
        isSelected = true
      )

      VerticalSpacer(AppTheme.spacingSystem.space16)

      Row(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = stringResource(Res.string.create_review_start),
            style = AppTheme.typographySystem.caption,
            color = AppTheme.colorSystem.textMuted
          )
          Text(
            text = state.startDate?.formatDisplay()
              ?: stringResource(Res.string.create_review_not_set),
            style = AppTheme.typographySystem.bodyMedium,
            color = AppTheme.colorSystem.textPrimary
          )
        }
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = stringResource(Res.string.create_review_wrap),
            style = AppTheme.typographySystem.caption,
            color = AppTheme.colorSystem.textMuted
          )
          Text(
            text = state.wrapDate?.formatDisplay() ?: stringResource(Res.string.create_review_not_set),
            style = AppTheme.typographySystem.bodyMedium,
            color = AppTheme.colorSystem.textPrimary
          )
        }
      }

      VerticalSpacer(AppTheme.spacingSystem.space8)

      Row(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = stringResource(Res.string.create_review_budget),
            style = AppTheme.typographySystem.caption,
            color = AppTheme.colorSystem.textMuted
          )
          Text(
            text = state.budgetDisplay
              ?: stringResource(Res.string.create_review_not_set),
            style = AppTheme.typographySystem.bodyMedium,
            color = AppTheme.colorSystem.textPrimary
          )
        }
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = stringResource(Res.string.create_review_crew),
            style = AppTheme.typographySystem.caption,
            color = AppTheme.colorSystem.textMuted
          )
          Text(
            text = stringResource(
              Res.string.create_review_members_count,
              state.crewMembers.size + 1
            ),
            style = AppTheme.typographySystem.bodyMedium,
            color = AppTheme.colorSystem.textPrimary
          )
        }
      }
    }
  }
}

@LightDarkPreview
@Composable
private fun Step3ContentPreview() {
  AppTheme {
    Step3Content(
      state = CreateProductionState(
        title = "Echoes of Silence",
        genre = Genre.DRAMA,
        startDate = LocalDate(2026, 8, 1),
        wrapDate = LocalDate(2026, 12, 15),
        budgetCents = 500_000_00L,
        crewMembers = listOf(
          CrewMemberEntry("Jane Smith", "Director"),
          CrewMemberEntry("John Doe", "Producer")
        )
      ),
      onIntent = {}
    )
  }
}

@LightDarkPreview
@Composable
private fun ReviewCardPreview() {
  AppTheme {
    ReviewCard(
      state = CreateProductionState(
        title = "Echoes of Silence",
        genre = Genre.DRAMA,
        startDate = LocalDate(2026, 8, 1),
        wrapDate = LocalDate(2026, 12, 15),
        budgetCents = 150_000_00L,
        crewMembers = listOf(CrewMemberEntry("Jane Smith", "Director"))
      )
    )
  }
}

@LightDarkPreview
@Composable
private fun ReviewCardEmptyPreview() {
  AppTheme {
    ReviewCard(state = CreateProductionState())
  }
}
