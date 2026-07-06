package com.frame.zero.shared.screenshot

import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import sergio.sastre.composable.preview.scanner.android.AndroidComposablePreviewScanner
import sergio.sastre.composable.preview.scanner.android.AndroidPreviewInfo
import sergio.sastre.composable.preview.scanner.core.preview.ComposablePreview

/**
 * Golden-image coverage for every `@LightDarkPreview` (and any other `@Preview`) in a module. The
 * scanner discovers previews on the test classpath; the custom `@LightDarkPreview` meta-annotation
 * expands to Light + Dark `@Preview`s, so each composable yields two goldens.
 *
 * Each module applying `crossplatform.screenshot` declares a `PreviewScreenshotTest` subclass whose
 * companion provides the parameters via [scanPreviews]:
 *
 * ```
 * class PreviewScreenshotTest(
 *   preview: ComposablePreview<AndroidPreviewInfo>
 * ) : BasePreviewScreenshotTest(preview) {
 *   companion object {
 *     @JvmStatic
 *     @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
 *     fun previews() = scanPreviews("com.frame.zero.feature.<name>.ui")
 *   }
 * }
 * ```
 *
 * Record with `:<module>:recordRoborazziAndroidHostTest`, check drift with
 * `:<module>:verifyRoborazziAndroidHostTest`. Robolectric native graphics + SDK level come from the
 * module's `androidHostTest/resources/robolectric.properties`.
 */
@OptIn(ExperimentalRoborazziApi::class)
@RunWith(ParameterizedRobolectricTestRunner::class)
abstract class BasePreviewScreenshotTest(
  private val preview: ComposablePreview<AndroidPreviewInfo>
) {
  @Test
  fun snapshot() {
    val variant = preview.previewInfo.name.ifBlank { "preview" }
    // Relative paths resolve against the module dir; commit goldens under src so they survive `clean`.
    val name = "${preview.declaringClass}_${preview.methodName}_${variant}_${preview.previewIndex ?: 0}.png"
    preview.captureRoboImage(filePath = "$GOLDENS_DIR/$name")
  }

  companion object {
    private const val GOLDENS_DIR = "src/androidUnitTest/screenshots"

    /**
     * [excludedPreviews] lists preview method names containing an indeterminate/animated indicator
     * that never reaches an idle frame — the capture lands on a different animation phase each run,
     * producing flaky pixel diffs. Excluding them keeps the suite exact-match.
     */
    fun scanPreviews(
      packageTree: String,
      excludedPreviews: Set<String> = emptySet()
    ): List<ComposablePreview<AndroidPreviewInfo>> =
      AndroidComposablePreviewScanner()
        .scanPackageTrees(packageTree)
        .includePrivatePreviews()
        .getPreviews()
        .filterNot { it.methodName in excludedPreviews }
  }
}
