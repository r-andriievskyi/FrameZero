package com.frame.zero.feature.task.details.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import framezero.composeapp.features.task_details.generated.resources.Res
import framezero.composeapp.features.task_details.generated.resources.task_details_description
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DescriptionCard(
  description: String,
  tags: List<String>,
  modifier: Modifier = Modifier
) {
  val shape = RoundedCornerShape(AppTheme.radiusSystem.radius16)
  Column(
    modifier = modifier
      .fillMaxWidth()
      .clip(shape)
      .background(AppTheme.colorSystem.cardBackground)
      .padding(AppTheme.spacingSystem.space16)
  ) {
    SectionLabel(text = stringResource(Res.string.task_details_description))
    VerticalSpacer(AppTheme.spacingSystem.space8)
    Text(
      text = description,
      style = AppTheme.typographySystem.bodyMedium,
      color = AppTheme.colorSystem.textPrimary
    )
    if (tags.isNotEmpty()) {
      VerticalSpacer(AppTheme.spacingSystem.space16)
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacingSystem.space8),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacingSystem.space8)
      ) {
        tags.forEach { tag ->
          TagChip(tag = tag)
        }
      }
    }
  }
}

@Composable
internal fun TagChip(
  tag: String,
  modifier: Modifier = Modifier
) {
  Text(
    text = tag,
    style = AppTheme.typographySystem.labelMedium.copy(fontWeight = FontWeight.Medium),
    color = AppTheme.colorSystem.accentText,
    modifier = modifier
      .clip(RoundedCornerShape(AppTheme.radiusSystem.radius4))
      .background(AppTheme.colorSystem.accentSurface)
      .padding(horizontal = TagPaddingHorizontal, vertical = TagPaddingVertical)
  )
}

@LightDarkPreview
@Composable
private fun DescriptionCardWithTagsPreview() {
  AppTheme {
    DescriptionCard(
      description = "Writer turned in revised pages for the confrontation in Scene 12. " +
        "Review the new dialogue against the shooting schedule and flag any continuity " +
        "issues with the Act II callbacks before the table read.",
      tags = listOf("Script", "Act II", "Review")
    )
  }
}

@LightDarkPreview
@Composable
private fun DescriptionCardNoTagsPreview() {
  AppTheme {
    DescriptionCard(
      description = "Quick sync with the DP about the new lens kit arriving tomorrow.",
      tags = emptyList()
    )
  }
}

