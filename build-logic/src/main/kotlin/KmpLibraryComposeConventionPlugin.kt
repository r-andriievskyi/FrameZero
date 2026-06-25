import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KmpLibraryComposeConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      pluginManager.apply("crossplatform.library")
      pluginManager.apply("org.jetbrains.compose")
      pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

      extensions.configure<ComposeCompilerGradlePluginExtension> {
        stabilityConfigurationFiles.add(
          rootProject.layout.projectDirectory.file("stability_config.conf")
        )
      }

      val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

      extensions.configure<KotlinMultiplatformExtension> {
        sourceSets.getByName("commonMain").dependencies {
          implementation(libs.findLibrary("compose-runtime").get())
          implementation(libs.findLibrary("compose-foundation").get())
          implementation(libs.findLibrary("compose-material3").get())
          implementation(libs.findLibrary("compose-ui").get())
          implementation(libs.findLibrary("compose-uiToolingPreview").get())
        }
      }

      dependencies {
        add("androidRuntimeClasspath", libs.findLibrary("compose-uiTooling").get())
      }
    }
  }
}
