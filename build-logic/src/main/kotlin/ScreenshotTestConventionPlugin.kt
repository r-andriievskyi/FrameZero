import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * Wires a module into the Roborazzi preview-screenshot suite: applies the Roborazzi plugin and
 * adds the shared screenshot-testing helper (which carries the full test stack via `api`) to
 * `androidHostTest`. Apply after `crossplatform.library.compose` so the android host-test source
 * set already exists. The module still supplies its own `PreviewScreenshotTest` subclass of
 * `BasePreviewScreenshotTest` and `androidHostTest/resources/robolectric.properties`.
 */
class ScreenshotTestConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      pluginManager.apply("io.github.takahirom.roborazzi")

      extensions.configure<KotlinMultiplatformExtension> {
        sourceSets.getByName("androidHostTest").dependencies {
          implementation(project(":composeApp:shared:screenshot-testing"))
        }
      }
    }
  }
}
