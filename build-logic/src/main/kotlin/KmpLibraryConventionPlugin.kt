import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KmpLibraryConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      configureKmpAndroidLibrary()

      extensions.configure<KotlinMultiplatformExtension> {
        iosArm64()
        iosSimulatorArm64()
      }
    }
  }
}
