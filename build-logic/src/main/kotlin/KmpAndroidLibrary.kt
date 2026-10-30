import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * The KMP + Android-library setup every client library shares. `crossplatform.library` adds the
 * iOS targets on top; `crossplatform.library.compose` does not — Compose UI is Android-only since
 * iOS moved to SwiftUI (docs/ios-swiftui-migration.md, C2).
 */
internal fun Project.configureKmpAndroidLibrary() {
  pluginManager.apply("org.jetbrains.kotlin.multiplatform")
  pluginManager.apply("com.android.kotlin.multiplatform.library")
  pluginManager.apply("crossplatform.code.quality")

  val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

  extensions.configure<KotlinMultiplatformExtension> {
    jvmToolchain(21)

    (this as ExtensionAware).extensions
      .configure<KotlinMultiplatformAndroidLibraryTarget> {
        compileSdk = libs.findVersion("android-compileSdk").get().requiredVersion.toInt()
        minSdk = libs.findVersion("android-minSdk").get().requiredVersion.toInt()
        compilerOptions {
          jvmTarget.set(JvmTarget.JVM_17)
        }
        androidResources { enable = true }
        withHostTest { isIncludeAndroidResources = true }
      }
  }

  // Roborazzi captures every @Preview in one host-test JVM; the screenshot-heavy modules
  // (e.g. home) exhaust the default heap, so give the unit-test workers more headroom.
  tasks.withType<Test>().configureEach {
    maxHeapSize = "2g"
  }
}
