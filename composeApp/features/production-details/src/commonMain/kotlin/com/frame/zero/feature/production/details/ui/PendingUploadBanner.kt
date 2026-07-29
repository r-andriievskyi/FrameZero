package com.frame.zero.feature.production.details.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.frame.zero.feature.production.details.PendingUploadUi
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.modifier.clickableWithRipple
import com.frame.zero.shared.design_system.widgets.HorizontalSpacer
import framezero.composeapp.features.production_details.generated.resources.Res
import framezero.composeapp.features.production_details.generated.resources.pending_upload_dismiss
import framezero.composeapp.features.production_details.generated.resources.pending_upload_failed_title
import framezero.composeapp.features.production_details.generated.resources.pending_upload_retry
import framezero.composeapp.features.production_details.generated.resources.pending_upload_uploading_title
import org.jetbrains.compose.resources.stringResource

/**
 * The one background task-create-with-attachment for this production, if any — either still
 * uploading (no action, it finishes on its own) or parked after exhausting its retry budget
 * (offers retry/dismiss). See `PendingUploadStore`/`UploadTaskUseCase`: this is the "somewhere
 * in the UI" the error-handling audit asked for so a parked upload is never silently lost.
 */
@Composable
internal fun PendingUploadBanner(
  upload: PendingUploadUi,
  onRetry: () -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  val colors = AppTheme.colorSystem
  val spacing = AppTheme.spacingSystem
  val backgroundColor = if (upload.isFailed) colors.errorSurface else colors.accentSurface
  val contentColor = if (upload.isFailed) colors.errorText else colors.accentText
  val shape = RoundedCornerShape(AppTheme.radiusSystem.radius14)

  Row(
    modifier = modifier
      .fillMaxWidth()
      .background(backgroundColor, shape)
      .padding(spacing.space16),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = if (upload.isFailed) {
        stringResource(Res.string.pending_upload_failed_title, upload.taskTitle)
      } else {
        stringResource(Res.string.pending_upload_uploading_title, upload.taskTitle)
      },
      style = AppTheme.typographySystem.bodyMedium,
      color = contentColor,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier.weight(1f)
    )
    if (upload.isFailed) {
      HorizontalSpacer(spacing.space12)
      Text(
        text = stringResource(Res.string.pending_upload_retry),
        style = AppTheme.typographySystem.bodyMedium.copy(fontWeight = FontWeight.Medium),
        color = contentColor,
        modifier = Modifier
          .clip(shape)
          .clickableWithRipple(color = colors.accentDim, onClick = onRetry)
          .padding(horizontal = spacing.space8, vertical = spacing.space4)
      )
      HorizontalSpacer(spacing.space4)
      Text(
        text = stringResource(Res.string.pending_upload_dismiss),
        style = AppTheme.typographySystem.bodyMedium,
        color = colors.textMuted,
        modifier = Modifier
          .clip(shape)
          .clickableWithRipple(color = colors.accentDim, onClick = onDismiss)
          .padding(horizontal = spacing.space8, vertical = spacing.space4)
      )
    }
  }
}

@LightDarkPreview
@Composable
private fun PendingUploadBannerUploadingPreview() {
  AppTheme {
    PendingUploadBanner(
      upload = PendingUploadUi(uploadId = "1", taskTitle = "Lock the schedule", isFailed = false),
      onRetry = {},
      onDismiss = {}
    )
  }
}

@LightDarkPreview
@Composable
private fun PendingUploadBannerFailedPreview() {
  AppTheme {
    PendingUploadBanner(
      upload = PendingUploadUi(uploadId = "1", taskTitle = "Lock the schedule", isFailed = true),
      onRetry = {},
      onDismiss = {}
    )
  }
}
