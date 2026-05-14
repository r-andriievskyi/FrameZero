package com.frame.zero.feature.production.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.discovery.playground.shared.design_system.AppTheme
import com.discovery.playground.shared.design_system.widgets.TopToolbar
import com.discovery.playground.shared.design_system.widgets.VerticalSpacer
import com.frame.zero.domain.production.Genre
import com.frame.zero.feature.production.CreateProductionComponent
import com.frame.zero.feature.production.CreateProductionIntent
import com.frame.zero.feature.production.CreateProductionState
import framezero.composeapp.features.production.generated.resources.Res
import framezero.composeapp.features.production.generated.resources.create_step_indicator
import framezero.composeapp.features.production.generated.resources.create_title
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
internal fun CreateProductionScreen(
  state: CreateProductionState,
  onIntent: (CreateProductionIntent) -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .background(AppTheme.colorSystem.background)
      .systemBarsPadding()
  ) {
    TopToolbar(
      title = stringResource(Res.string.create_title),
      onBack = onBack
    )

    Text(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = AppTheme.spacingSystem.space16),
      text = stringResource(Res.string.create_step_indicator, state.currentStep, state.totalSteps),
      style = AppTheme.typographySystem.caption,
      color = AppTheme.colorSystem.textMuted,
      textAlign = TextAlign.Center
    )

    VerticalSpacer(AppTheme.spacingSystem.space8)

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
        2 -> Step3Content(state = state, onIntent = onIntent)
        3 -> Step4Content(state = state, onIntent = onIntent)
      }
    }
  }
}

// ── Previews ─────────────────────────────────────────────────────────

@Preview
@Composable
private fun CreateProductionStep1Preview() {
  AppTheme(darkTheme = true) {
    CreateProductionScreen(
      state = CreateProductionState(currentStep = 1, title = "Echoes of Silence", genre = Genre.DRAMA),
      onIntent = {},
      onBack = {}
    )
  }
}

@Preview
@Composable
private fun CreateProductionStep2Preview() {
  AppTheme(darkTheme = true) {
    CreateProductionScreen(
      state = CreateProductionState(currentStep = 2),
      onIntent = {},
      onBack = {}
    )
  }
}

@Preview
@Composable
private fun CreateProductionStep3Preview() {
  AppTheme(darkTheme = true) {
    CreateProductionScreen(
      state = CreateProductionState(currentStep = 3, title = "Echoes of Silence", genre = Genre.DRAMA),
      onIntent = {},
      onBack = {}
    )
  }
}
