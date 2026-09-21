import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Applies Metro, the compile-time DI compiler plugin, to a module that declares or consumes
 * bindings. Opt-in per module (like `crossplatform.screenshot`) rather than folded into
 * `crossplatform.library`: the pure-UI modules under `composeApp/features` and the pure-model
 * ones (`shared/domain`, `shared/dto`, `shared/ui_text`) have no DI and should not carry a
 * compiler plugin and a runtime dependency for nothing.
 *
 * The Metro Gradle plugin adds `dev.zacsweers.metro:runtime` to the shared source sets itself.
 */
class MetroConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    target.pluginManager.apply("dev.zacsweers.metro")
  }
}
