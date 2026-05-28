package com.frame.zero.feature.task.details.ui

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
import com.frame.zero.feature.task.details.TaskAttachment
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.widgets.HorizontalSpacer
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import framezero.composeapp.features.task_details.generated.resources.Res
import framezero.composeapp.features.task_details.generated.resources.task_details_attachments
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun AttachmentsCard(
  attachments: List<TaskAttachment>,
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
    SectionLabel(text = stringResource(Res.string.task_details_attachments))
    VerticalSpacer(AppTheme.spacingSystem.space16)
    attachments.forEachIndexed { index, attachment ->
      AttachmentRow(attachment = attachment)
      if (index < attachments.lastIndex) {
        VerticalSpacer(AppTheme.spacingSystem.space8)
      }
    }
  }
}

@Composable
private fun AttachmentRow(
  attachment: TaskAttachment,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(AppTheme.radiusSystem.radius8))
      .background(AppTheme.colorSystem.inputBackground)
      .padding(AppTheme.spacingSystem.space16),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(AttachmentIconSize)
        .clip(RoundedCornerShape(AppTheme.radiusSystem.radius8))
        .background(AppTheme.colorSystem.accentSurface),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = "\uD83D\uDCC4",
        style = AppTheme.typographySystem.bodyLarge
      )
    }
    HorizontalSpacer(AppTheme.spacingSystem.space16)
    Column {
      Text(
        text = attachment.fileName,
        style = AppTheme.typographySystem.titleSmall,
        color = AppTheme.colorSystem.textPrimary
      )
      VerticalSpacer(AppTheme.spacingSystem.space2)
      Text(
        text = "${attachment.fileType}  ·  ${attachment.fileSize}",
        style = AppTheme.typographySystem.bodySmall,
        color = AppTheme.colorSystem.textMuted
      )
    }
  }
}

@LightDarkPreview
@Composable
private fun AttachmentsCardPreview() {
  AppTheme {
    AttachmentsCard(
      attachments = listOf(
        TaskAttachment(
          id = "a1",
          fileName = "Scene12_rev4.pdf",
          fileType = "PDF",
          fileSize = "1.2 MB"
        ),
        TaskAttachment(
          id = "a2",
          fileName = "Continuity_notes.fdx",
          fileType = "Final Draft",
          fileSize = "84 KB"
        )
      )
    )
  }
}

@LightDarkPreview
@Composable
private fun AttachmentsCardSinglePreview() {
  AppTheme {
    AttachmentsCard(
      attachments = listOf(
        TaskAttachment(
          id = "a1",
          fileName = "Location_scout_photos.zip",
          fileType = "ZIP",
          fileSize = "24.5 MB"
        )
      )
    )
  }
}

