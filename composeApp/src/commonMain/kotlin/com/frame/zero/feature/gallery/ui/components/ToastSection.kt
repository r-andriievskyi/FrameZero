package com.frame.zero.feature.gallery.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.widgets.VerticalSpacer
import com.frame.zero.shared.design_system.widgets.toast.AppToast
import com.frame.zero.shared.design_system.widgets.toast.ToastSeverity

@Composable
internal fun ToastSection() {
  val spacingSystem = AppTheme.spacingSystem
  GallerySection(title = "Toasts") {
    Column {
      AppToast(title = "Saved successfully", severity = ToastSeverity.Success)
      VerticalSpacer(spacingSystem.space12)
      AppToast(
        title = "Check your connection",
        severity = ToastSeverity.Warning,
        description = "You appear to be offline."
      )
      VerticalSpacer(spacingSystem.space12)
      AppToast(
        title = "Something went wrong",
        severity = ToastSeverity.Error,
        actionLabel = "Retry",
        onAction = {}
      )
    }
  }
}

@LightDarkPreview
@Composable
private fun ToastSectionPreview() {
  AppTheme {
    Box(modifier = Modifier.background(AppTheme.colorSystem.background).padding(AppTheme.spacingSystem.space16)) {
      ToastSection()
    }
  }
}
