package com.frame.zero.shared.design_system

import com.frame.zero.shared.screenshot.BasePreviewScreenshotTest
import org.robolectric.ParameterizedRobolectricTestRunner
import sergio.sastre.composable.preview.scanner.android.AndroidPreviewInfo
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview

class PreviewScreenshotTest(
  preview: ComposablePreview<AndroidPreviewInfo>
) : BasePreviewScreenshotTest(preview) {
  companion object {
    @JvmStatic
    @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
    fun previews() =
      scanPreviews(
        packageTree = "com.frame.zero.shared.design_system",
        // Both render Material3's morphing LoadingIndicator, which never reaches an idle frame.
        excludedPreviews = setOf("CtaButtonPreview", "FullScreenProgressPreview")
      )
  }
}
