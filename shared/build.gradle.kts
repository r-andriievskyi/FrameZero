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
      api(libs.kotlinx.serialization.json)
      api(libs.kotlinx.coroutines.core)
      api(libs.kotlinx.datetime)
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
      implementation(libs.androidx.security.crypto)
    }
    iosMain.dependencies { implementation(libs.ktor.clientDarwin) }
    jvmMain.dependencies { implementation(libs.ktor.clientOkHttp) }
    commonTest.dependencies {
      implementation(libs.kotlin.test)
      implementation(libs.kotlinx.coroutines.test)
      implementation(libs.ktor.clientMock)
      implementation(libs.multiplatformSettings.test)
    }
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
