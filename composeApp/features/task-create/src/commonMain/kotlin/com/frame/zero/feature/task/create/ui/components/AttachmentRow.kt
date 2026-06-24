package com.frame.zero.feature.task.create.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.modifier.clickableWithRipple
import framezero.composeapp.features.task_create.generated.resources.Res
import framezero.composeapp.features.task_create.generated.resources.attach_file
import framezero.composeapp.features.task_create.generated.resources.attachment_remove
import org.jetbrains.compose.resources.stringResource

/**
 * Minimal attach-file affordance: a tap-to-attach label, or the picked file's name with a
 * Remove action. Visual styling is intentionally plain pending design.
 */
@Composable
internal fun AttachmentRow(
  attachmentName: String?,
  onAttach: () -> Unit,
  onRemove: () -> Unit,
  modifier: Modifier = Modifier
) {
  val colors = AppTheme.colorSystem
  val spacing = AppTheme.spacingSystem
  val typography = AppTheme.typographySystem

  if (attachmentName == null) {
    Text(
      text = stringResource(Res.string.attach_file),
      style = typography.bodyLarge,
      color = colors.accent,
      modifier = modifier
        .clickableWithRipple(color = colors.accentDim, onClick = onAttach)
        .padding(vertical = spacing.space8)
    )
  } else {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = modifier.fillMaxWidth()
    ) {
      Text(
        text = attachmentName,
        style = typography.bodyLarge,
        color = colors.textPrimary,
        modifier = Modifier.weight(1f)
      )
      Text(
        text = stringResource(Res.string.attachment_remove),
        style = typography.bodyLarge,
        color = colors.errorText,
        modifier = Modifier
          .clickableWithRipple(color = colors.accentDim, onClick = onRemove)
          .padding(spacing.space8)
      )
    }
  }
}

@LightDarkPreview
@Composable
private fun AttachmentRowPreview() {
  AppTheme {
    AttachmentRow(attachmentName = "shot-list.pdf", onAttach = {}, onRemove = {})
  }
}
