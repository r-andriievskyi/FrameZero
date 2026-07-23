package com.frame.zero.feature.app_update

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.widgets.CtaButton
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import framezero.composeapp.generated.resources.Res
import framezero.composeapp.generated.resources.ic_update_warning
import framezero.composeapp.generated.resources.update_hard_action_button
import framezero.composeapp.generated.resources.update_hard_subtitle
import framezero.composeapp.generated.resources.update_hard_title
import framezero.composeapp.generated.resources.update_hard_warning
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun HardUpdateScreen(
  message: String?,
  onUpdate: () -> Unit,
  modifier: Modifier = Modifier
) {
  val colorSystem = AppTheme.colorSystem
  val spacingSystem = AppTheme.spacingSystem

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(colorSystem.background)
      .pointerInput(Unit) { /* swallow touches to the covered stack */ }
      .systemBarsPadding(),
    contentAlignment = Alignment.TopCenter
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .fillMaxSize()
        .padding(horizontal = AppTheme.spacingSystem.space32, vertical = spacingSystem.space24),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      HardUpdateContent(
        message = message,
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
      )
      HardUpdateWarningBanner(modifier = Modifier.fillMaxWidth())
      VerticalSpacer(spacingSystem.space32)
      CtaButton(
        text = stringResource(Res.string.update_hard_action_button),
        modifier = Modifier.fillMaxWidth(),
        onClick = onUpdate
      )
    }
  }
}

@Composable
private fun HardUpdateContent(
  message: String?,
  modifier: Modifier = Modifier
) {
  val colorSystem = AppTheme.colorSystem
  val typographySystem = AppTheme.typographySystem
  val spacingSystem = AppTheme.spacingSystem

  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    UpdateLogoBadge(
      badgeIcon = Res.drawable.ic_update_warning,
      badgeContainerColor = colorSystem.warningSurface,
      badgeIconTint = colorSystem.warningText
    )
    VerticalSpacer(spacingSystem.space24)
    Text(
      text = stringResource(Res.string.update_hard_title),
      style = typographySystem.displayMedium,
      color = colorSystem.textPrimary,
      textAlign = TextAlign.Center
    )
    VerticalSpacer(spacingSystem.space8)
    Text(
      text = message ?: stringResource(Res.string.update_hard_subtitle),
      style = typographySystem.bodyMedium,
      color = colorSystem.textMuted,
      textAlign = TextAlign.Center
    )
  }
}

@Composable
private fun HardUpdateWarningBanner(modifier: Modifier = Modifier) {
  val colorSystem = AppTheme.colorSystem
  val typographySystem = AppTheme.typographySystem
  val spacingSystem = AppTheme.spacingSystem

  Text(
    text = stringResource(Res.string.update_hard_warning),
    modifier = modifier
      .clip(RoundedCornerShape(AppTheme.radiusSystem.radius14))
      .background(colorSystem.warningSurface)
      .padding(spacingSystem.space16),
    style = typographySystem.bodySmall,
    color = colorSystem.warningText
  )
}

@LightDarkPreview
@Composable
private fun HardUpdateScreenPreview() {
  AppTheme {
    HardUpdateScreen(message = null, onUpdate = {})
  }
}
