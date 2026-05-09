package com.frame.zero.feature.home.ui.tab.productions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.discovery.playground.shared.design_system.AppTheme
import com.discovery.playground.shared.design_system.widgets.VerticalSpacer
import framezero.composeapp.features.home.generated.resources.Res
import framezero.composeapp.features.home.generated.resources.projects_create_button
import framezero.composeapp.features.home.generated.resources.projects_empty_description
import framezero.composeapp.features.home.generated.resources.projects_empty_invite
import framezero.composeapp.features.home.generated.resources.projects_empty_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun EmptyState(modifier: Modifier = Modifier, onCreateProductionClick: () -> Unit) {
  Column(
    modifier = modifier.fillMaxWidth().padding(vertical = AppTheme.spacingSystem.space24),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    EmptyStateIllustration()

    VerticalSpacer(AppTheme.spacingSystem.space24)

    Text(
      text = stringResource(Res.string.projects_empty_title),
      style = AppTheme.typographySystem.titleMedium,
      color = AppTheme.colorSystem.textPrimary,
      textAlign = TextAlign.Center
    )

    VerticalSpacer(AppTheme.spacingSystem.space8)

    Text(
      text = stringResource(Res.string.projects_empty_description),
      style = AppTheme.typographySystem.bodySmall,
      color = AppTheme.colorSystem.textMuted,
      textAlign = TextAlign.Center
    )

    VerticalSpacer(AppTheme.spacingSystem.space24)

    Box(
      modifier =
        Modifier
          .fillMaxWidth(0.7f)
          .clip(RoundedCornerShape(AppTheme.radiusSystem.radius16))
          .background(AppTheme.colorSystem.accent)
          .clickable(onClick = onCreateProductionClick)
          .padding(
            horizontal = AppTheme.spacingSystem.space24,
            vertical = AppTheme.spacingSystem.space16
          ),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = stringResource(Res.string.projects_create_button),
        style = AppTheme.typographySystem.labelLarge,
        color = AppTheme.colorSystem.textOnAccent
      )
    }

    VerticalSpacer(AppTheme.spacingSystem.space16)

    Text(
      text = stringResource(Res.string.projects_empty_invite),
      style = AppTheme.typographySystem.bodySmall,
      color = AppTheme.colorSystem.textMuted,
      textAlign = TextAlign.Center
    )
  }
}

@Composable
private fun EmptyStateIllustration() {
  val cardShape = RoundedCornerShape(AppTheme.radiusSystem.radius16)

  Box(
    modifier = Modifier.size(width = 220.dp, height = 160.dp),
    contentAlignment = Alignment.BottomCenter
  ) {
    // Back card (rotated left)
    Box(
      modifier = Modifier
        .size(width = 180.dp, height = 100.dp)
        .offset(y = (-40).dp)
        .rotate(-5f)
        .clip(cardShape)
        .background(AppTheme.colorSystem.cardBackground)
        .border(1.dp, AppTheme.colorSystem.cardBorder, cardShape)
    )

    // Middle card (rotated right)
    Box(
      modifier = Modifier
        .size(width = 180.dp, height = 100.dp)
        .offset(y = (-25).dp)
        .rotate(3f)
        .clip(cardShape)
        .background(AppTheme.colorSystem.cardBackground)
        .border(1.dp, AppTheme.colorSystem.cardBorder, cardShape)
    ) {
      // Colored progress bars at bottom of middle card
      Row(
        modifier = Modifier
          .align(Alignment.BottomStart)
          .padding(AppTheme.spacingSystem.space8)
          .fillMaxWidth()
      ) {
        Box(
          modifier = Modifier
            .weight(1f)
            .height(4.dp)
            .clip(RoundedCornerShape(AppTheme.radiusSystem.radius4))
            .background(AppTheme.colorSystem.warningText)
        )
        Spacer(modifier = Modifier.width(AppTheme.spacingSystem.space4))
        Box(
          modifier = Modifier
            .weight(1f)
            .height(4.dp)
            .clip(RoundedCornerShape(AppTheme.radiusSystem.radius4))
            .background(AppTheme.colorSystem.successText)
        )
        Spacer(modifier = Modifier.width(AppTheme.spacingSystem.space4))
        Box(
          modifier = Modifier
            .weight(0.5f)
            .height(4.dp)
            .clip(RoundedCornerShape(AppTheme.radiusSystem.radius4))
            .background(AppTheme.colorSystem.accent)
        )
      }
    }

    // Front card
    Box(
      modifier = Modifier
        .size(width = 200.dp, height = 70.dp)
        .clip(cardShape)
        .background(AppTheme.colorSystem.cardBackground)
        .border(1.dp, AppTheme.colorSystem.cardBorder, cardShape)
        .padding(AppTheme.spacingSystem.space8),
      contentAlignment = Alignment.CenterStart
    ) {
      Column {
        Box(
          modifier = Modifier
            .size(width = 80.dp, height = 8.dp)
            .clip(RoundedCornerShape(AppTheme.radiusSystem.radius4))
            .background(AppTheme.colorSystem.border)
        )
        VerticalSpacer(AppTheme.spacingSystem.space4)
        Box(
          modifier = Modifier
            .size(width = 50.dp, height = 6.dp)
            .clip(RoundedCornerShape(AppTheme.radiusSystem.radius4))
            .background(AppTheme.colorSystem.border)
        )
      }
    }
  }
}

@Preview
@Composable
private fun EmptyStatePreview() {
  AppTheme(darkTheme = true) {
    Column(
      modifier = Modifier
        .background(AppTheme.colorSystem.background)
        .padding(AppTheme.spacingSystem.space16)
    ) {
      EmptyState(onCreateProductionClick = {})
    }
  }
}

