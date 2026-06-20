import com.android.build.gradle.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KmpLibraryConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {
    with(target) {
      pluginManager.apply("com.android.library")
      pluginManager.apply("org.jetbrains.kotlin.multiplatform")
      pluginManager.apply("crossplatform.code.quality")

      val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

      extensions.configure<KotlinMultiplatformExtension> {
        jvmToolchain(21)
        androidTarget {
          compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
          }
        }
        iosArm64()
        iosSimulatorArm64()
      }

      extensions.configure<LibraryExtension> {
        compileSdk = libs.findVersion("android-compileSdk").get().requiredVersion.toInt()
        defaultConfig {
          minSdk = libs.findVersion("android-minSdk").get().requiredVersion.toInt()
        }
        compileOptions {
          sourceCompatibility = JavaVersion.VERSION_11
          targetCompatibility = JavaVersion.VERSION_11
        }
      }
    }
  }
}
