import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.register
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
          exclude { entry -> entry.file.absolutePath.contains("/build/") }
          include("**/*.kt")
        }
      }

      extensions.configure<DetektExtension> {
        buildUponDefaultConfig = true
        config.setFrom(rootProject.files("config/detekt/detekt.yml"))
        // Per-module baseline grandfathers pre-existing findings so only new
        // ones fail the build. Regenerate with `./gradlew <module>:detektBaseline`.
        baseline = file("detekt-baseline.xml")
      }

      registerDesignSystemDetekt()
    }
  }

  private fun Project.registerDesignSystemDetekt() {
    val commonMain = layout.projectDirectory.dir("src/commonMain/kotlin")
    if (path == ":detekt-rules" || !commonMain.asFile.exists()) return

    val rulesClasspath = configurations.create("designSystemDetektRules")
    dependencies { add(rulesClasspath.name, project(":detekt-rules")) }

    val designSystemDetekt = tasks.register<Detekt>("detektDesignSystem") {
      description = "Enforces the design-system ruleset over commonMain."
      setSource(commonMain)
      include("**/*.kt")
      config.setFrom(rootProject.files("config/detekt/detekt-design-system.yml"))
      buildUponDefaultConfig = false
      disableDefaultRuleSets = true
      pluginClasspath.from(rulesClasspath)
      baseline.convention(null as org.gradle.api.file.RegularFile?)
      reports {
        html.required.set(false)
        xml.required.set(false)
        txt.required.set(false)
        sarif.required.set(false)
        md.required.set(false)
      }
    }

    tasks.named("detekt").configure { dependsOn(designSystemDetekt) }
  }
}
