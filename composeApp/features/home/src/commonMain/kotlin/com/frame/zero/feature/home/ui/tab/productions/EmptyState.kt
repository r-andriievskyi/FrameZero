package com.frame.zero.feature.home.ui.tab.productions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.widgets.CtaButton
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import framezero.composeapp.features.home.generated.resources.Res
import framezero.composeapp.features.home.generated.resources.projects_create_button
import framezero.composeapp.features.home.generated.resources.projects_empty_description
import framezero.composeapp.features.home.generated.resources.projects_empty_invite
import framezero.composeapp.features.home.generated.resources.projects_empty_title
import org.jetbrains.compose.resources.stringResource

private const val IllustrationWidthFraction = 0.7f
private const val ContainerAspectRatio = 1.375f
private const val StackedCardWidthFraction = 0.82f
private const val StackedCardHeightFraction = 0.625f
private const val BackCardOffsetFraction = 0.25f
private const val MiddleCardOffsetFraction = 0.156f
private const val FrontCardHeightFraction = 0.44f
private val IllustrationBarHeight = 4.dp
private val TitleBarHeight = 8.dp
private val SubtitleBarHeight = 6.dp

@Composable
internal fun EmptyState(
  modifier: Modifier = Modifier,
  onCreateProductionClick: () -> Unit
) {
  val spacingSystem = AppTheme.spacingSystem
  val typographySystem = AppTheme.typographySystem
  val colorSystem = AppTheme.colorSystem
  Column(
    modifier = modifier.fillMaxWidth().padding(vertical = spacingSystem.space24),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    EmptyStateIllustration()

    VerticalSpacer(spacingSystem.space24)

    Text(
      text = stringResource(Res.string.projects_empty_title),
      style = typographySystem.titleMedium,
      color = colorSystem.textPrimary,
      textAlign = TextAlign.Center
    )

    VerticalSpacer(spacingSystem.space8)

    Text(
      text = stringResource(Res.string.projects_empty_description),
      style = typographySystem.bodySmall,
      color = colorSystem.textMuted,
      textAlign = TextAlign.Center
    )

    VerticalSpacer(spacingSystem.space24)

    CtaButton(
      modifier = Modifier.fillMaxWidth(0.7f),
      text = stringResource(Res.string.projects_create_button),
      onClick = onCreateProductionClick
    )

    VerticalSpacer(spacingSystem.space16)

    Text(
      text = stringResource(Res.string.projects_empty_invite),
      style = typographySystem.bodySmall,
      color = colorSystem.textMuted,
      textAlign = TextAlign.Center
    )
  }
}

@Composable
private fun EmptyStateIllustration() {
  val cardShape = RoundedCornerShape(AppTheme.radiusSystem.radius16)

  BoxWithConstraints(
    modifier = Modifier
      .fillMaxWidth(IllustrationWidthFraction)
      .aspectRatio(ContainerAspectRatio),
    contentAlignment = Alignment.BottomCenter
  ) {
    val containerWidth = maxWidth
    val containerHeight = maxHeight
    val stackedCardWidth = containerWidth * StackedCardWidthFraction
    val stackedCardHeight = containerHeight * StackedCardHeightFraction
    val frontCardHeight = containerHeight * FrontCardHeightFraction

    // back card
    Box(
      modifier = Modifier
        .size(width = stackedCardWidth, height = stackedCardHeight)
        .offset(y = -(containerHeight * BackCardOffsetFraction))
        .rotate(-5f)
        .clip(cardShape)
        .background(AppTheme.colorSystem.cardBackground)
        .border(AppTheme.borderSystem.hairline, AppTheme.colorSystem.cardBorder, cardShape)
    )

    // middle card
    Box(
      modifier = Modifier
        .size(width = stackedCardWidth, height = stackedCardHeight)
        .offset(y = -(containerHeight * MiddleCardOffsetFraction))
        .rotate(3f)
        .clip(cardShape)
        .background(AppTheme.colorSystem.cardBackground)
        .border(AppTheme.borderSystem.hairline, AppTheme.colorSystem.cardBorder, cardShape)
    ) {
      Row(
        modifier = Modifier
          .align(Alignment.BottomStart)
          .padding(AppTheme.spacingSystem.space8)
          .fillMaxWidth()
      ) {
        Box(
          modifier = Modifier
            .weight(1f)
            .height(IllustrationBarHeight)
            .clip(RoundedCornerShape(AppTheme.radiusSystem.radius4))
            .background(AppTheme.colorSystem.warningText)
        )
        Spacer(modifier = Modifier.width(AppTheme.spacingSystem.space4))
        Box(
          modifier = Modifier
            .weight(1f)
            .height(IllustrationBarHeight)
            .clip(RoundedCornerShape(AppTheme.radiusSystem.radius4))
            .background(AppTheme.colorSystem.successText)
        )
        Spacer(modifier = Modifier.width(AppTheme.spacingSystem.space4))
        Box(
          modifier = Modifier
            .weight(0.5f)
            .height(IllustrationBarHeight)
            .clip(RoundedCornerShape(AppTheme.radiusSystem.radius4))
            .background(AppTheme.colorSystem.accent)
        )
      }
    }

    Box(
      modifier = Modifier
        .fillMaxWidth(0.9f)
        .height(frontCardHeight)
        .clip(cardShape)
        .background(AppTheme.colorSystem.cardBackground)
        .border(AppTheme.borderSystem.hairline, AppTheme.colorSystem.cardBorder, cardShape)
        .padding(AppTheme.spacingSystem.space8),
      contentAlignment = Alignment.CenterStart
    ) {
      Column {
        Box(
          modifier = Modifier
            .fillMaxWidth(0.4f)
            .height(TitleBarHeight)
            .clip(RoundedCornerShape(AppTheme.radiusSystem.radius4))
            .background(AppTheme.colorSystem.border)
        )
        VerticalSpacer(AppTheme.spacingSystem.space4)
        Box(
          modifier = Modifier
            .fillMaxWidth(0.25f)
            .height(SubtitleBarHeight)
            .clip(RoundedCornerShape(AppTheme.radiusSystem.radius4))
            .background(AppTheme.colorSystem.border)
        )
      }
    }
  }
}

@LightDarkPreview
@Composable
private fun EmptyStatePreview() {
  AppTheme {
    EmptyState(
      modifier = Modifier.padding(AppTheme.spacingSystem.space16),
      onCreateProductionClick = {}
    )
  }
}
