package com.frame.zero.feature.task.details.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.frame.zero.feature.task.details.TaskAttachment
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.modifier.clickableWithRipple
import com.frame.zero.shared.design_system.widgets.HorizontalSpacer
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import framezero.composeapp.features.task_details.generated.resources.Res
import framezero.composeapp.features.task_details.generated.resources.ic_file
import framezero.composeapp.features.task_details.generated.resources.task_details_attachment
import framezero.composeapp.features.task_details.generated.resources.task_details_attachment_downloading
import framezero.composeapp.features.task_details.generated.resources.task_details_attachment_subtitle
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private val FileIconContainerSize = 44.dp

@Composable
internal fun AttachmentCard(
  attachment: TaskAttachment,
  isDownloading: Boolean,
  errorMessage: String?,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val colors = AppTheme.colorSystem
  val typography = AppTheme.typographySystem
  val spacing = AppTheme.spacingSystem

  Column(modifier = modifier.fillMaxWidth()) {
    Text(
      text = stringResource(Res.string.task_details_attachment),
      style = typography.labelLarge,
      color = colors.textMuted
    )
    VerticalSpacer(spacing.space8)
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(AppTheme.radiusSystem.radius14))
        .background(colors.inputBackground)
        .clickableWithRipple(color = colors.accentDim, onClick = onClick)
        .padding(spacing.space12),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(FileIconContainerSize)
          .clip(RoundedCornerShape(AppTheme.radiusSystem.radius8))
          .background(colors.accentSurface),
        contentAlignment = Alignment.Center
      ) {
        Image(
          painter = painterResource(Res.drawable.ic_file),
          contentDescription = null,
          colorFilter = ColorFilter.tint(colors.accent)
        )
      }
      HorizontalSpacer(spacing.space12)
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = attachment.fileName,
          style = typography.titleSmall,
          color = colors.textPrimary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        VerticalSpacer(spacing.space2)
        val subtitle = when {
          errorMessage != null -> errorMessage
          isDownloading -> stringResource(Res.string.task_details_attachment_downloading)
          else -> stringResource(
            Res.string.task_details_attachment_subtitle,
            attachment.typeLabel,
            attachment.sizeLabel
          )
        }
        Text(
          text = subtitle,
          style = typography.bodySmall,
          color = when {
            errorMessage != null -> colors.errorText
            isDownloading -> colors.accent
            else -> colors.textMuted
          }
        )
      }
    }
  }
}

@LightDarkPreview
@Composable
private fun AttachmentCardPreview() {
  AppTheme {
    AttachmentCard(
      attachment = TaskAttachment(
        fileName = "Scene12_rev4.pdf",
        typeLabel = "PDF",
        sizeLabel = "1.2 MB",
        contentType = "application/pdf",
        sizeBytes = 1_258_291
      ),
      isDownloading = false,
      errorMessage = null,
      onClick = {},
      modifier = Modifier
        .background(AppTheme.colorSystem.background)
        .padding(AppTheme.spacingSystem.space16)
    )
  }
}
