import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jlleitschuh.gradle.ktlint.KtlintExtension

class CodeQualityConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      pluginManager.apply("org.jlleitschuh.gradle.ktlint")
      pluginManager.apply("io.gitlab.arturbosch.detekt")
      pluginManager.apply("org.jetbrains.kotlinx.kover")

      extensions.configure<KtlintExtension> {
        android.set(false)
        outputToConsole.set(true)
        ignoreFailures.set(false)
        filter {
          exclude("**/generated/**")
          include("**/*.kt")
        }
      }

      extensions.configure<DetektExtension> {
        buildUponDefaultConfig = true
        config.setFrom(rootProject.files("config/detekt/detekt.yml"))
      }
    }
  }
}
