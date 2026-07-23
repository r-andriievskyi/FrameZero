package com.frame.zero.feature.app_update

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.modifier.clickableWithRipple
import com.frame.zero.shared.design_system.widgets.CtaButton
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import framezero.composeapp.generated.resources.Res
import framezero.composeapp.generated.resources.ic_app_logo
import framezero.composeapp.generated.resources.ic_update_available
import framezero.composeapp.generated.resources.update_action_button
import framezero.composeapp.generated.resources.update_later_button
import framezero.composeapp.generated.resources.update_soft_subtitle
import framezero.composeapp.generated.resources.update_soft_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private val ContentHorizontalPadding = 32.dp
private val IconContainerSize = 80.dp
private val IconBadgeSize = 36.dp
private val BadgeOverhang = IconBadgeSize / 3
private val LogoSize = IconContainerSize / 2

@Composable
internal fun SoftUpdateScreen(
  message: String?,
  onUpdate: () -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  val colorSystem = AppTheme.colorSystem
  val typographySystem = AppTheme.typographySystem
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
        .padding(horizontal = ContentHorizontalPadding, vertical = spacingSystem.space24),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      SoftUpdateContent(
        message = message,
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
      )
      CtaButton(
        text = stringResource(Res.string.update_action_button),
        modifier = Modifier.fillMaxWidth(),
        onClick = onUpdate
      )
      VerticalSpacer(spacingSystem.space8)
      Text(
        text = stringResource(Res.string.update_later_button),
        style = typographySystem.labelLarge,
        color = colorSystem.textMuted,
        modifier = Modifier
          .clip(RoundedCornerShape(AppTheme.radiusSystem.radius8))
          .clickableWithRipple(color = colorSystem.accentDim, onClick = onDismiss)
          .padding(horizontal = spacingSystem.space16, vertical = spacingSystem.space8)
      )
    }
  }
}

@Composable
private fun SoftUpdateContent(message: String?, modifier: Modifier = Modifier) {
  val colorSystem = AppTheme.colorSystem
  val typographySystem = AppTheme.typographySystem
  val spacingSystem = AppTheme.spacingSystem
  val cardShape = RoundedCornerShape(AppTheme.radiusSystem.radius16)

  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Box {
      Box(
        modifier = Modifier
          .padding(end = BadgeOverhang, bottom = BadgeOverhang)
          .size(IconContainerSize)
          .clip(cardShape)
          .background(colorSystem.cardBackground)
          .border(AppTheme.borderSystem.hairline, colorSystem.border, cardShape),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          painter = painterResource(Res.drawable.ic_app_logo),
          contentDescription = null,
          tint = colorSystem.textPrimary,
          modifier = Modifier.size(LogoSize)
        )
      }
      Box(
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .size(IconBadgeSize)
          .clip(CircleShape)
          .background(colorSystem.accentSurface),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          painter = painterResource(Res.drawable.ic_update_available),
          contentDescription = null,
          tint = colorSystem.accentText,
          modifier = Modifier.padding(AppTheme.spacingSystem.space8)
        )
      }
    }
    VerticalSpacer(spacingSystem.space24)
    Text(
      text = stringResource(Res.string.update_soft_title),
      style = typographySystem.displayMedium,
      color = colorSystem.textPrimary,
      textAlign = TextAlign.Center
    )
    VerticalSpacer(spacingSystem.space8)
    Text(
      text = message ?: stringResource(Res.string.update_soft_subtitle),
      style = typographySystem.bodyMedium,
      color = colorSystem.textMuted,
      textAlign = TextAlign.Center
    )
  }
}

@LightDarkPreview
@Composable
private fun SoftUpdateScreenPreview() {
  AppTheme {
    SoftUpdateScreen(message = null, onUpdate = {}, onDismiss = {})
  }
}
