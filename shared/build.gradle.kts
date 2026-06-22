import com.codingfeline.buildkonfig.compiler.FieldSpec
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidLibrary)
  alias(libs.plugins.kotlinSerialization)
  alias(libs.plugins.buildKonfig)
  id("crossplatform.code.quality")
}

// BuildKonfig picks its flavor from the `buildkonfig.flavor` Gradle property,
// which nothing sets by default — a Play-store build would ship the dev config
// (DEBUG=true → full network logging incl. bearer tokens, localhost base URL).
// Infer the release flavor whenever a *Release task was requested on the
// command line; an explicit -Pbuildkonfig.flavor always wins.
if (!project.hasProperty("buildkonfig.flavor")) {
  val releaseTaskRequested = gradle.startParameter.taskNames.any { taskName ->
    taskName.substringAfterLast(":").contains("Release")
  }
  if (releaseTaskRequested) {
    project.extensions.extraProperties["buildkonfig.flavor"] = "release"
  }
}

val buildKonfigFlavor: String = project.findProperty("buildkonfig.flavor")?.toString().orEmpty()

// Release base URL. A checked-in localhost placeholder (gradle.properties)
// keeps release builds working before a real backend exists; the env var
// (how CI/staging/prod inject the real URL) and an explicit -P override it.
fun resolveReleaseBaseUrl(): String {
  if (buildKonfigFlavor != "release") return ""
  val raw = providers.environmentVariable("FRAMEZERO_API_BASE_URL").orNull
    ?: providers.gradleProperty("framezero.api.baseUrl").orNull
    ?: error(
      "Release builds need a backend base URL: set framezero.api.baseUrl " +
        "(gradle.properties / -P) or the FRAMEZERO_API_BASE_URL environment variable."
    )
  if (!raw.startsWith("https://")) {
    logger.warn(
      "Release BASE_URL '$raw' is not https — fine as a local placeholder, never for production."
    )
  }
  return raw.trimEnd('/')
}

kotlin {
  jvmToolchain(21)
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
      implementation(libs.androidx.biometric)
      implementation(libs.androidx.fragment)
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

  // empty BASE_URL → NetworkConfig falls back to the platform localhost dev server.
  defaultConfigs {
    buildConfigField(FieldSpec.Type.BOOLEAN, "DEBUG", "true")
    buildConfigField(FieldSpec.Type.STRING, "BASE_URL", "")
  }
  defaultConfigs("release") {
    buildConfigField(FieldSpec.Type.BOOLEAN, "DEBUG", "false")
    buildConfigField(FieldSpec.Type.STRING, "BASE_URL", resolveReleaseBaseUrl())
  }
}
