package com.frame.zero.feature.home.ui

import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import sergio.sastre.composable.preview.scanner.android.AndroidComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.android.AndroidPreviewInfo
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview

@OptIn(ExperimentalRoborazziApi::class)
@RunWith(ParameterizedRobolectricTestRunner::class)
class PreviewScreenshotTest(
  private val preview: ComposablePreview<AndroidPreviewInfo>
) {
  companion object {
    private const val GOLDENS_DIR = "src/androidUnitTest/screenshots"

    private val NON_IDLE_PREVIEWS = setOf("ProductionsSkeletonPreview", "ProductionsContentPreview")

    @JvmStatic
    @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
    fun previews(): List<ComposablePreview<AndroidPreviewInfo>> =
      AndroidComposablePreviewScanner()
        .scanPackageTrees("com.frame.zero.feature.home.ui")
        .includePrivatePreviews()
        .getPreviews()
        .filterNot { it.methodName in NON_IDLE_PREVIEWS }
  }

  @Test
  fun snapshot() {
    val variant = preview.previewInfo.name.ifBlank { "preview" }
    val name = "${preview.declaringClass}_${preview.methodName}_${variant}_${preview.previewIndex ?: 0}.png"
    preview.captureRoboImage(filePath = "$GOLDENS_DIR/$name")
  }
}
