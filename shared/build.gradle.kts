import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidLibrary)
  alias(libs.plugins.kotlinSerialization)
  id("crossplatform.code.quality")
}

kotlin {
  androidTarget { compilerOptions { jvmTarget.set(JvmTarget.JVM_11) } }

  iosArm64()
  iosSimulatorArm64()

  jvm()

  sourceSets {
    commonMain.dependencies {
      api(projects.shared.features.auth)
      api(projects.shared.features.dashboard)
      api(projects.shared.repositories.auth)
      api(libs.kotlinx.serialization.json)
      implementation(libs.koin.core)
      implementation(libs.decompose)
      implementation(libs.kotlinx.coroutines.core)
    }
    commonTest.dependencies { implementation(libs.kotlin.test) }
  }
}

android {
  namespace = "com.frame.zero.shared"
  compileSdk = libs.versions.android.compileSdk.get().toInt()
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  defaultConfig { minSdk = libs.versions.android.minSdk.get().toInt() }
}
