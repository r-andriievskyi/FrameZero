package com.frame.zero.feature.gallery.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.frame.zero.shared.design_system.AppTheme
import com.frame.zero.shared.design_system.LightDarkPreview
import com.frame.zero.shared.design_system.widgets.SingleLineInputField

@Composable
internal fun InputSection() {
  var value by remember { mutableStateOf("") }
  GallerySection(title = "Input field") {
    SingleLineInputField(
      value = value,
      onValueChange = { value = it },
      placeholder = "Type something…"
    )
  }
}

@LightDarkPreview
@Composable
private fun InputSectionPreview() {
  AppTheme {
    Box(modifier = Modifier.background(AppTheme.colorSystem.background).padding(AppTheme.spacingSystem.space16)) {
      InputSection()
    }
  }
}
