import com.codingfeline.buildkonfig.compiler.FieldSpec
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidLibrary)
  alias(libs.plugins.kotlinSerialization)
  alias(libs.plugins.buildKonfig)
  id("crossplatform.code.quality")
}

kotlin {
  androidTarget { compilerOptions { jvmTarget.set(JvmTarget.JVM_11) } }

  iosArm64()
  iosSimulatorArm64()

  sourceSets {
    commonMain.dependencies {
      api(libs.kotlinx.serialization.json)
      api(libs.kotlinx.coroutines.core)
      api(libs.kotlinx.datetime)
      api(libs.kotlinx.collections.immutable)
      api(libs.koin.core)
      api(libs.ktor.clientCore)
      api(libs.multiplatformSettings)
      implementation(libs.ktor.clientAuth)
      implementation(libs.ktor.clientContentNegotiation)
      implementation(libs.ktor.clientLogging)
      implementation(libs.ktor.clientSerializationJson)
      implementation(libs.multiplatformSettings.coroutines)
    }
    androidMain.dependencies {
      implementation(libs.ktor.clientOkHttp)
    }
    iosMain.dependencies { implementation(libs.ktor.clientDarwin) }
    commonTest.dependencies {
      implementation(libs.kotlin.test)
      implementation(libs.kotlinx.coroutines.test)
      implementation(libs.ktor.clientMock)
      implementation(libs.multiplatformSettings.test)
    }
  }
}

dependencies {
  "debugImplementation"(libs.chucker.library)
  "releaseImplementation"(libs.chucker.libraryNoOp)
}

android {
  namespace = "com.frame.zero.shared"
  compileSdk = libs.versions.android.compileSdk
    .get()
    .toInt()
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  defaultConfig {
    minSdk = libs.versions.android.minSdk
      .get()
      .toInt()
  }
}

buildkonfig {
  packageName = "com.frame.zero.core.network"

  defaultConfigs {
    buildConfigField(FieldSpec.Type.BOOLEAN, "DEBUG", "true")
  }
  defaultConfigs("release") {
    buildConfigField(FieldSpec.Type.BOOLEAN, "DEBUG", "false")
  }
}
