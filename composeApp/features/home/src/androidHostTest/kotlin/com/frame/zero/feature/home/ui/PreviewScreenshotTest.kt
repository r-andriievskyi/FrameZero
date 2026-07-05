package com.frame.zero.feature.home.ui

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
    fun previews() = scanPreviews(
      packageTree = "com.frame.zero.feature.home.ui",
      excludedPreviews = setOf("ProductionsSkeletonPreview", "ProductionsContentPreview")
    )
  }
}
