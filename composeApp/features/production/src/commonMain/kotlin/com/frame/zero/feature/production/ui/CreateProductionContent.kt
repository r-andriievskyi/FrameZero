package com.frame.zero.feature.production.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.discovery.playground.shared.design_system.AppTheme
import com.discovery.playground.shared.design_system.widgets.CtaButton
import com.discovery.playground.shared.design_system.widgets.HorizontalSpacer
import com.discovery.playground.shared.design_system.widgets.SingleLineInputField
import com.discovery.playground.shared.design_system.widgets.VerticalSpacer
import com.frame.zero.domain.production.Genre
import com.frame.zero.domain.production.ProductionPhase
import com.frame.zero.feature.production.CreateProductionComponent
import com.frame.zero.feature.production.CreateProductionIntent
import com.frame.zero.feature.production.CreateProductionState
import com.frame.zero.feature.production.CrewMemberEntry
import framezero.composeapp.features.production.generated.resources.Res
import framezero.composeapp.features.production.generated.resources.create_button_continue
import framezero.composeapp.features.production.generated.resources.create_button_create
import framezero.composeapp.features.production.generated.resources.create_button_creating
import framezero.composeapp.features.production.generated.resources.create_button_skip_continue
import framezero.composeapp.features.production.generated.resources.create_crew_empty
import framezero.composeapp.features.production.generated.resources.create_crew_label
import framezero.composeapp.features.production.generated.resources.create_crew_owner_note
import framezero.composeapp.features.production.generated.resources.create_field_budget
import framezero.composeapp.features.production.generated.resources.create_field_current_phase
import framezero.composeapp.features.production.generated.resources.create_field_genre
import framezero.composeapp.features.production.generated.resources.create_field_logline
import framezero.composeapp.features.production.generated.resources.create_field_name
import framezero.composeapp.features.production.generated.resources.create_field_production_title
import framezero.composeapp.features.production.generated.resources.create_field_role
import framezero.composeapp.features.production.generated.resources.create_field_start_date
import framezero.composeapp.features.production.generated.resources.create_field_wrap_date
import framezero.composeapp.features.production.generated.resources.create_placeholder_date
import framezero.composeapp.features.production.generated.resources.create_placeholder_logline
import framezero.composeapp.features.production.generated.resources.create_placeholder_name
import framezero.composeapp.features.production.generated.resources.create_placeholder_production_title
import framezero.composeapp.features.production.generated.resources.create_review_budget
import framezero.composeapp.features.production.generated.resources.create_review_crew
import framezero.composeapp.features.production.generated.resources.create_review_members_count
import framezero.composeapp.features.production.generated.resources.create_review_not_set
import framezero.composeapp.features.production.generated.resources.create_review_start
import framezero.composeapp.features.production.generated.resources.create_review_untitled
import framezero.composeapp.features.production.generated.resources.create_review_wrap
import framezero.composeapp.features.production.generated.resources.create_step1_subtitle
import framezero.composeapp.features.production.generated.resources.create_step1_title
import framezero.composeapp.features.production.generated.resources.create_step2_subtitle
import framezero.composeapp.features.production.generated.resources.create_step2_title
import framezero.composeapp.features.production.generated.resources.create_step3_subtitle
import framezero.composeapp.features.production.generated.resources.create_step3_title
import framezero.composeapp.features.production.generated.resources.create_step4_subtitle
import framezero.composeapp.features.production.generated.resources.create_step4_title
import framezero.composeapp.features.production.generated.resources.create_step_indicator
import framezero.composeapp.features.production.generated.resources.create_title
import framezero.composeapp.features.production.generated.resources.crew_role_art
import framezero.composeapp.features.production.generated.resources.crew_role_director
import framezero.composeapp.features.production.generated.resources.crew_role_dp
import framezero.composeapp.features.production.generated.resources.crew_role_editor
import framezero.composeapp.features.production.generated.resources.crew_role_other
import framezero.composeapp.features.production.generated.resources.crew_role_producer
import framezero.composeapp.features.production.generated.resources.crew_role_sound
import framezero.composeapp.features.production.generated.resources.crew_role_writer
import framezero.composeapp.features.production.generated.resources.phase_development
import framezero.composeapp.features.production.generated.resources.phase_distribution
import framezero.composeapp.features.production.generated.resources.phase_post_production
import framezero.composeapp.features.production.generated.resources.phase_pre_production
import framezero.composeapp.features.production.generated.resources.phase_production
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource

@Composable
fun CreateProductionContent(component: CreateProductionComponent) {
  val state by component.state.collectAsState()
  CreateProductionScreen(
    state = state,
    onIntent = component::onIntent,
    onBack = {
      if (state.currentStep > 1) {
        component.onIntent(CreateProductionIntent.PreviousStep)
      } else {
        component.onBack()
      }
    }
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
      .systemBarsPadding()
  ) {
    TopBar(step = state.currentStep, totalSteps = state.totalSteps, onBack = onBack)

    StepIndicator(currentStep = state.currentStep, totalSteps = state.totalSteps)

    VerticalSpacer(AppTheme.spacingSystem.space24)

    AnimatedContent(
      targetState = state.currentStep,
      transitionSpec = {
        if (targetState > initialState) {
          slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
        } else {
          slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
        }
      },
      modifier = Modifier.weight(1f)
    ) { step ->
      when (step) {
        1 -> Step1Content(state = state, onIntent = onIntent)
        2 -> Step2Content(state = state, onIntent = onIntent)
        3 -> Step3Content(state = state, onIntent = onIntent)
        4 -> Step4Content(state = state, onIntent = onIntent)
      }
    }
  }
}

// ── Top bar ──────────────────────────────────────────────────────────

@Composable
private fun TopBar(step: Int, totalSteps: Int, onBack: () -> Unit) {
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
        .size(40.dp)
        .clip(CircleShape)
        .background(AppTheme.colorSystem.cardBackground)
        .clickable(onClick = onBack),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = "‹",
        style = AppTheme.typographySystem.titleLarge,
        color = AppTheme.colorSystem.textPrimary
      )
    }
    HorizontalSpacer(AppTheme.spacingSystem.space8)
    Column {
      Text(
        text = stringResource(Res.string.create_title),
        style = AppTheme.typographySystem.titleMedium,
        color = AppTheme.colorSystem.textPrimary
      )
      Text(
        text = stringResource(Res.string.create_step_indicator, step, totalSteps),
        style = AppTheme.typographySystem.caption,
        color = AppTheme.colorSystem.textMuted
      )
    }
  }
}

// ── Step indicator (dots) ────────────────────────────────────────────

@Composable
private fun StepIndicator(currentStep: Int, totalSteps: Int) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.Center,
    verticalAlignment = Alignment.CenterVertically
  ) {
    for (i in 1..totalSteps) {
      val isActive = i <= currentStep
      val shape = RoundedCornerShape(AppTheme.radiusSystem.radiusMax)
      Box(
        modifier = Modifier
          .width(if (i == currentStep) 24.dp else 8.dp)
          .height(8.dp)
          .clip(shape)
          .background(
            if (isActive) AppTheme.colorSystem.accent
            else AppTheme.colorSystem.cardBackground
          )
      )
      if (i < totalSteps) HorizontalSpacer(AppTheme.spacingSystem.space4)
    }
  }
}

// ── Step 1: What are you making? ─────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Step1Content(
  state: CreateProductionState,
  onIntent: (CreateProductionIntent) -> Unit
) {
  Column(
    modifier = Modifier
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

    FieldLabel(stringResource(Res.string.create_field_logline))
    VerticalSpacer(AppTheme.spacingSystem.space8)
    SingleLineInputField(
      value = state.logline,
      onValueChange = { onIntent(CreateProductionIntent.LoglineChanged(it)) },
      placeholder = stringResource(Res.string.create_placeholder_logline),
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

// ── Step 2: Timeline & Phase ─────────────────────────────────────────

@Composable
private fun Step2Content(
  state: CreateProductionState,
  onIntent: (CreateProductionIntent) -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = AppTheme.spacingSystem.space16)
  ) {
    Text(
      text = stringResource(Res.string.create_step2_title),
      style = AppTheme.typographySystem.displayMedium,
      color = AppTheme.colorSystem.textPrimary
    )
    VerticalSpacer(AppTheme.spacingSystem.space4)
    Text(
      text = stringResource(Res.string.create_step2_subtitle),
      style = AppTheme.typographySystem.bodyMedium,
      color = AppTheme.colorSystem.textSecondary
    )

    VerticalSpacer(AppTheme.spacingSystem.space24)

    FieldLabel(stringResource(Res.string.create_field_current_phase))
    VerticalSpacer(AppTheme.spacingSystem.space8)
    PhaseSelector(
      selected = state.phase,
      onSelect = { onIntent(CreateProductionIntent.PhaseChanged(it)) }
    )

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

// ── Step 3: Build your team ──────────────────────────────────────────

@Composable
private fun Step3Content(
  state: CreateProductionState,
  onIntent: (CreateProductionIntent) -> Unit
) {
  Column(
    modifier = Modifier
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
          .size(48.dp)
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

// ── Step 4: Review & create ──────────────────────────────────────────

@Composable
private fun Step4Content(
  state: CreateProductionState,
  onIntent: (CreateProductionIntent) -> Unit
) {
  Column(
    modifier = Modifier
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
      ErrorText(error)
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
private fun ReviewCard(state: CreateProductionState) {
  val cardShape = RoundedCornerShape(AppTheme.radiusSystem.radius16)
  val phaseColor = state.phase.dotColor()

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(cardShape)
      .border(1.dp, AppTheme.colorSystem.cardBorder, cardShape)
      .background(AppTheme.colorSystem.cardBackground)
  ) {
    // Gradient accent strip at top
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(6.dp)
        .background(
          Brush.horizontalGradient(
            listOf(phaseColor, AppTheme.colorSystem.accent)
          )
        )
    )

    Column(modifier = Modifier.padding(AppTheme.spacingSystem.space16)) {
      Text(
        text = state.title.ifBlank { stringResource(Res.string.create_review_untitled) },
        style = AppTheme.typographySystem.titleLarge,
        color = AppTheme.colorSystem.textPrimary
      )

      VerticalSpacer(AppTheme.spacingSystem.space8)

      Row(
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacingSystem.space8),
        verticalAlignment = Alignment.CenterVertically
      ) {
        GenreChip(
          label = state.genre.displayLabel(),
          isSelected = true,
          onClick = {}
        )
        Text(
          text = state.phase.label(),
          style = AppTheme.typographySystem.labelSmall,
          color = phaseColor
        )
      }

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
            text = state.wrapDate?.formatDisplay()
              ?: stringResource(Res.string.create_review_not_set),
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
            text = state.budgetCents?.let { formatBudget(it) }
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
            text = stringResource(Res.string.create_review_members_count, state.crewMembers.size + 1),
            style = AppTheme.typographySystem.bodyMedium,
            color = AppTheme.colorSystem.textPrimary
          )
        }
      }
    }
  }
}

// ── Phase selector ───────────────────────────────────────────────────

private val visiblePhases = listOf(
  ProductionPhase.DEVELOPMENT,
  ProductionPhase.PRE_PRODUCTION,
  ProductionPhase.PRODUCTION,
  ProductionPhase.POST_PRODUCTION
)

@Composable
private fun PhaseSelector(
  selected: ProductionPhase,
  onSelect: (ProductionPhase) -> Unit
) {
  Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacingSystem.space8)) {
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
            width = if (isSelected) 2.dp else 1.dp,
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
              .size(12.dp)
              .clip(CircleShape)
              .background(phase.dotColor())
          )
          HorizontalSpacer(AppTheme.spacingSystem.space8)
          Text(
            text = phase.label(),
            style = AppTheme.typographySystem.bodyMedium,
            color = if (isSelected) AppTheme.colorSystem.textPrimary
            else AppTheme.colorSystem.textMuted
          )
        }
        if (isSelected) {
          Box(
            modifier = Modifier
              .size(24.dp)
              .clip(RoundedCornerShape(AppTheme.radiusSystem.radius4))
              .background(phase.dotColor()),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "✓",
              style = AppTheme.typographySystem.labelSmall,
              color = AppTheme.colorSystem.textOnAccent
            )
          }
        }
      }
    }
  }
}

// ── Genre chip ───────────────────────────────────────────────────────

@Composable
private fun GenreChip(
  label: String,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  val shape = RoundedCornerShape(AppTheme.radiusSystem.radiusMax)
  Box(
    modifier = Modifier
      .clip(shape)
      .background(
        if (isSelected) AppTheme.colorSystem.accentSurface
        else AppTheme.colorSystem.cardBackground
      )
      .border(
        1.dp,
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
      color = if (isSelected) AppTheme.colorSystem.accentText
      else AppTheme.colorSystem.textSecondary
    )
  }
}

// ── Crew member row ──────────────────────────────────────────────────

@Composable
private fun CrewMemberRow(member: CrewMemberEntry, onRemove: () -> Unit) {
  val shape = RoundedCornerShape(AppTheme.radiusSystem.radius8)
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(shape)
      .border(1.dp, AppTheme.colorSystem.cardBorder, shape)
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
        .size(24.dp)
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

// ── Crew avatar ──────────────────────────────────────────────────────

@Composable
private fun CrewAvatar(member: CrewMemberEntry) {
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
    modifier = Modifier
      .size(36.dp)
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

// ── Role dropdown ────────────────────────────────────────────────────

@Composable
private fun RoleDropdown(
  selected: String,
  onSelect: (String) -> Unit
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

  Box {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clip(shape)
        .background(AppTheme.colorSystem.inputBackground)
        .border(1.dp, AppTheme.colorSystem.border, shape)
        .clickable { expanded = !expanded }
        .padding(
          horizontal = AppTheme.spacingSystem.space16,
          vertical = AppTheme.spacingSystem.space8
        )
        .height(32.dp),
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
          .padding(top = 52.dp)
          .clip(shape)
          .background(AppTheme.colorSystem.surfaceElevated)
          .border(1.dp, AppTheme.colorSystem.border, shape)
      ) {
        crewRoles.forEach { role ->
          Text(
            text = role,
            style = AppTheme.typographySystem.bodyMedium,
            color = if (role == selected) AppTheme.colorSystem.accentText
            else AppTheme.colorSystem.textPrimary,
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
private fun BudgetInputField(
  budgetCents: Long?,
  onBudgetChange: (Long?) -> Unit,
  enabled: Boolean
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
    placeholder = "500,000",
    enabled = enabled,
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

@Composable
private fun DateInputField(
  value: LocalDate?,
  placeholder: String,
  enabled: Boolean,
  onDateChange: (LocalDate) -> Unit
) {
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
    trailingContent = {
      Text(
        text = "📅",
        style = AppTheme.typographySystem.bodyMedium
      )
    }
  )
}

// ── Shared composables ───────────────────────────────────────────────

@Composable
private fun FieldLabel(text: String) {
  Text(
    text = text,
    style = AppTheme.typographySystem.labelSmall,
    color = AppTheme.colorSystem.textSecondary
  )
}

@Composable
private fun ErrorText(error: String) {
  Text(
    text = error,
    style = AppTheme.typographySystem.bodySmall,
    color = AppTheme.colorSystem.errorText
  )
}

// ── Utilities ────────────────────────────────────────────────────────

private fun Genre.displayLabel(): String =
  name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }

@Composable
private fun ProductionPhase.label(): String = when (this) {
  ProductionPhase.DEVELOPMENT -> stringResource(Res.string.phase_development)
  ProductionPhase.PRE_PRODUCTION -> stringResource(Res.string.phase_pre_production)
  ProductionPhase.PRODUCTION -> stringResource(Res.string.phase_production)
  ProductionPhase.POST_PRODUCTION -> stringResource(Res.string.phase_post_production)
  ProductionPhase.DISTRIBUTION -> stringResource(Res.string.phase_distribution)
}

@Composable
private fun ProductionPhase.dotColor(): Color =
  when (this) {
    ProductionPhase.DEVELOPMENT -> Color(0xFF5BC0EB)
    ProductionPhase.PRE_PRODUCTION -> AppTheme.colorSystem.warningText
    ProductionPhase.PRODUCTION -> AppTheme.colorSystem.successText
    ProductionPhase.POST_PRODUCTION -> AppTheme.colorSystem.accent
    ProductionPhase.DISTRIBUTION -> AppTheme.colorSystem.successText
  }

private fun LocalDate.formatDisplay(): String {
  val monthNames = listOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
  )
  return "${monthNames[month.ordinal]} $day, $year"
}

private fun formatBudget(cents: Long): String {
  val dollars = cents / 100
  return "$${dollars.toString().reversed().chunked(3).joinToString(",").reversed()}"
}

private fun parseDateInput(raw: String): LocalDate? {
  val parts = raw.split(".")
  if (parts.size == 3) {
    val day = parts[0].toIntOrNull() ?: return null
    val month = parts[1].toIntOrNull() ?: return null
    val year = parts[2].toIntOrNull() ?: return null
    return runCatching { LocalDate(year, month, day) }.getOrNull()
  }
  return runCatching { LocalDate.parse(raw) }.getOrNull()
}
