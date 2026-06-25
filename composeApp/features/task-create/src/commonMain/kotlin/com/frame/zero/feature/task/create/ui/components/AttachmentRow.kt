package com.frame.zero.feature.task.create.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.modifier.clickableWithRipple
import com.frame.zero.shared.design_system.widgets.HorizontalSpacer
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import framezero.composeapp.features.task_create.generated.resources.Res
import framezero.composeapp.features.task_create.generated.resources.attach_file_subtitle
import framezero.composeapp.features.task_create.generated.resources.attach_file_title
import framezero.composeapp.features.task_create.generated.resources.attachment_remove
import framezero.composeapp.features.task_create.generated.resources.ic_paperclip
import framezero.composeapp.features.task_create.generated.resources.ic_plus
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private val IconSize = 20.dp
private val DashStrokeWidth = 1.5.dp
private val DashOn = 6.dp
private val DashGap = 4.dp

@Composable
internal fun AttachmentRow(
  attachmentName: String?,
  onAttach: () -> Unit,
  onRemove: () -> Unit,
  modifier: Modifier = Modifier
) {
  if (attachmentName == null) {
    AttachFilePrompt(onAttach = onAttach, modifier = modifier)
  } else {
    AttachedFileRow(attachmentName = attachmentName, onRemove = onRemove, modifier = modifier)
  }
}

@Composable
private fun AttachFilePrompt(
  onAttach: () -> Unit,
  modifier: Modifier = Modifier
) {
  val colors = AppTheme.colorSystem
  val spacing = AppTheme.spacingSystem
  val typography = AppTheme.typographySystem
  val shape = RoundedCornerShape(AppTheme.radiusSystem.radius14)

  Row(
    modifier = modifier
      .fillMaxWidth()
      .clip(shape)
      .dashedBorder(color = colors.border, cornerRadius = AppTheme.radiusSystem.radius14)
      .clickableWithRipple(color = colors.accentDim, onClick = onAttach)
      .padding(spacing.space16),
    verticalAlignment = Alignment.CenterVertically
  ) {
    LeadingIcon(iconColor = colors.textMuted, backgroundColor = colors.inputBackground)
    HorizontalSpacer(spacing.space12)
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = stringResource(Res.string.attach_file_title),
        style = typography.titleSmall,
        color = colors.textPrimary
      )
      VerticalSpacer(spacing.space4)
      Text(
        text = stringResource(Res.string.attach_file_subtitle),
        style = typography.bodySmall,
        color = colors.textMuted
      )
    }
    HorizontalSpacer(spacing.space12)
    Image(
      painter = painterResource(Res.drawable.ic_plus),
      contentDescription = null,
      colorFilter = ColorFilter.tint(colors.textMuted)
    )
  }
}

@Composable
private fun AttachedFileRow(
  attachmentName: String,
  onRemove: () -> Unit,
  modifier: Modifier = Modifier
) {
  val colors = AppTheme.colorSystem
  val spacing = AppTheme.spacingSystem
  val typography = AppTheme.typographySystem
  val shape = RoundedCornerShape(AppTheme.radiusSystem.radius14)

  Row(
    modifier = modifier
      .fillMaxWidth()
      .clip(shape)
      .background(colors.inputBackground)
      .padding(spacing.space16),
    verticalAlignment = Alignment.CenterVertically
  ) {
    LeadingIcon(iconColor = colors.accent, backgroundColor = colors.accentSurface)
    HorizontalSpacer(spacing.space12)
    Text(
      text = attachmentName,
      style = typography.titleSmall,
      color = colors.textPrimary,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier.weight(1f)
    )
    HorizontalSpacer(spacing.space8)
    Text(
      text = stringResource(Res.string.attachment_remove),
      style = typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
      color = colors.errorText,
      modifier = Modifier
        .clip(RoundedCornerShape(AppTheme.radiusSystem.radius8))
        .clickableWithRipple(color = colors.accentDim, onClick = onRemove)
        .padding(horizontal = spacing.space8, vertical = spacing.space4)
    )
  }
}

@Composable
private fun LeadingIcon(
  iconColor: Color,
  backgroundColor: Color,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(AppTheme.radiusSystem.radius8))
      .background(backgroundColor)
      .padding(AppTheme.spacingSystem.space8),
    contentAlignment = Alignment.Center
  ) {
    Image(
      painter = painterResource(Res.drawable.ic_paperclip),
      contentDescription = null,
      colorFilter = ColorFilter.tint(iconColor)
    )
  }
}

private fun Modifier.dashedBorder(
  color: Color,
  cornerRadius: Dp
): Modifier =
  drawWithCache {
    val stroke = Stroke(
      width = DashStrokeWidth.toPx(),
      pathEffect = PathEffect.dashPathEffect(floatArrayOf(DashOn.toPx(), DashGap.toPx()))
    )
    val radius = CornerRadius(cornerRadius.toPx())
    val inset = DashStrokeWidth.toPx() / 2f
    onDrawBehind {
      drawRoundRect(
        color = color,
        topLeft = Offset(inset, inset),
        size = Size(size.width - inset * 2, size.height - inset * 2),
        cornerRadius = radius,
        style = stroke
      )
    }
  }

@LightDarkPreview
@Composable
private fun AttachmentRowEmptyPreview() {
  AppTheme {
    AttachmentRow(attachmentName = null, onAttach = {}, onRemove = {})
  }
}

@LightDarkPreview
@Composable
private fun AttachmentRowAttachedPreview() {
  AppTheme {
    AttachmentRow(attachmentName = "shot-list.pdf", onAttach = {}, onRemove = {})
  }
}
