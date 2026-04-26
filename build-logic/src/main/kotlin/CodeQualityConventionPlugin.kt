import com.ncorti.ktfmt.gradle.KtfmtExtension
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class CodeQualityConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      pluginManager.apply("com.ncorti.ktfmt.gradle")
      pluginManager.apply("io.gitlab.arturbosch.detekt")

      extensions.configure<KtfmtExtension> {
        googleStyle() // 2-space indent, 4-space continuation, max line 100
      }

      extensions.configure<DetektExtension> {
        buildUponDefaultConfig = true
        config.setFrom(rootProject.files("config/detekt/detekt.yml"))
      }
    }
  }
}
