package com.frame.zero.feature.gallery.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.widgets.VerticalSpacer

/**
 * Titled container shared by every gallery section: a section heading above the supplied
 * [content].
 */
@Composable
internal fun GallerySection(
  title: String,
  content: @Composable () -> Unit
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Text(
      text = title,
      style = AppTheme.typographySystem.titleSmall,
      color = AppTheme.colorSystem.textPrimary,
      fontWeight = FontWeight.SemiBold
    )
    VerticalSpacer(AppTheme.spacingSystem.space12)
    content()
  }
}

@LightDarkPreview
@Composable
private fun GallerySectionPreview() {
  AppTheme {
    GallerySection(title = "Section") {
      Text(
        text = "Content",
        style = AppTheme.typographySystem.bodyMedium,
        color = AppTheme.colorSystem.textPrimary
      )
    }
  }
}
