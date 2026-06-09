package com.frame.zero.feature.home.ui

import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import sergio.sastre.composable.preview.scanner.android.AndroidComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.android.AndroidPreviewInfo
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview

/**
 * Golden-image coverage for every `@LightDarkPreview` (and any other `@Preview`) in the home
 * feature. The scanner discovers previews on the test classpath; the custom `@LightDarkPreview`
 * meta-annotation expands to Light + Dark `@Preview`s, so each composable yields two goldens.
 *
 * Record with `:composeApp:features:home:recordRoborazziDebug`, check drift with
 * `:composeApp:features:home:verifyRoborazziDebug`. Robolectric native graphics + SDK level come
 * from `androidUnitTest/resources/robolectric.properties`.
 */
@OptIn(ExperimentalRoborazziApi::class)
@RunWith(ParameterizedRobolectricTestRunner::class)
class PreviewScreenshotTest(
  private val preview: ComposablePreview<AndroidPreviewInfo>
) {
  companion object {
    private const val GOLDENS_DIR = "src/androidUnitTest/screenshots"

    // Previews containing an indeterminate/animated indicator never reach an idle frame, so the
    // capture lands on a different animation phase each run → flaky pixel diffs. Keep the suite
    // exact-match by excluding them. (ProductionsSkeletonPreview pulses via an infinite transition.)
    private val ANIMATED_PREVIEWS = setOf("ProductionsSkeletonPreview")

    @JvmStatic
    @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
    fun previews(): List<ComposablePreview<AndroidPreviewInfo>> =
      AndroidComposablePreviewScanner()
        .scanPackageTrees("com.frame.zero.feature.home.ui")
        .includePrivatePreviews()
        .getPreviews()
        .filterNot { it.methodName in ANIMATED_PREVIEWS }
  }

  @Test
  fun snapshot() {
    val variant = preview.previewInfo.name.ifBlank { "preview" }
    // Relative paths resolve against the module dir; commit goldens under src so they survive `clean`.
    val name = "${preview.declaringClass}_${preview.methodName}_${variant}_${preview.previewIndex ?: 0}.png"
    preview.captureRoboImage(filePath = "$GOLDENS_DIR/$name")
  }
}
