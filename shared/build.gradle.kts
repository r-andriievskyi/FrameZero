import com.codingfeline.buildkonfig.compiler.FieldSpec
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidKmpLibrary)
  alias(libs.plugins.kotlinSerialization)
  alias(libs.plugins.buildKonfig)
  id("crossplatform.code.quality")
}

val requestedTasks = gradle.startParameter.taskNames.map { it.substringAfterLast(":") }
val demoTaskRequested = requestedTasks.any { it.contains("Demo") }
val releaseTaskRequested = requestedTasks.any { it.contains("Release") }

// A single Gradle invocation compiles :shared for one BuildKonfig flavor, so `demo` + `release`
// requested together (assembleDemoRelease) is its own flavor: minified, but still demo data and
// no backend base URL. Never invoke demo and prod variant tasks in the same command.
val inferredFlavor: String? = when {
  demoTaskRequested && releaseTaskRequested -> "demoRelease"
  demoTaskRequested -> "demo"
  releaseTaskRequested -> "release"
  else -> null
}

if (!project.hasProperty("buildkonfig.flavor") && inferredFlavor != null) {
  project.extensions.extraProperties["buildkonfig.flavor"] = inferredFlavor
}

val buildKonfigFlavor: String = project.findProperty("buildkonfig.flavor")?.toString().orEmpty()

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
  android {
    namespace = "com.frame.zero.shared"
    compileSdk = libs.versions.android.compileSdk
      .get()
      .toInt()
    minSdk = libs.versions.android.minSdk
      .get()
      .toInt()
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_17)
    }
    withHostTest { isIncludeAndroidResources = true }
  }

  iosArm64()
  iosSimulatorArm64()

  sourceSets {
    commonMain.dependencies {
      api(projects.shared.domain)
      api(projects.shared.dto)
      implementation(projects.shared.database)
      api(projects.shared.uiText)
      implementation(libs.kotlinx.serialization.json)
      api(libs.kotlinx.coroutines.core)
      api(libs.kotlinx.datetime)
      api(libs.kotlinx.collections.immutable)
      api(libs.koin.core)
      implementation(libs.ktor.clientCore)
      implementation(libs.ktor.clientWebsockets)
      implementation(libs.multiplatformSettings)
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
      implementation(libs.androidx.work.runtime)
      implementation(if (releaseTaskRequested) libs.chucker.libraryNoOp else libs.chucker.library)
    }
    iosMain.dependencies { implementation(libs.ktor.clientDarwin) }
    commonTest.dependencies {
      implementation(libs.bundles.commonTest)
      implementation(libs.ktor.clientMock)
      implementation(libs.multiplatformSettings.test)
    }
    getByName("androidHostTest").dependencies {
      implementation(libs.kotlin.testJunit)
      implementation(libs.junit)
      implementation(libs.robolectric)
      implementation(libs.androidx.work.testing)
    }
  }
}

buildkonfig {
  packageName = "com.frame.zero.core.network"

  // empty BASE_URL → NetworkConfig falls back to the platform localhost dev server.
  // DEMO is const so it constant-folds and R8 strips the demo wiring from prod builds.
  defaultConfigs {
    buildConfigField(FieldSpec.Type.BOOLEAN, "DEBUG", "true")
    buildConfigField(FieldSpec.Type.STRING, "BASE_URL", "")
    buildConfigField(FieldSpec.Type.BOOLEAN, "DEMO", "false", const = true)
  }
  defaultConfigs("release") {
    buildConfigField(FieldSpec.Type.BOOLEAN, "DEBUG", "false")
    buildConfigField(FieldSpec.Type.STRING, "BASE_URL", resolveReleaseBaseUrl())
    buildConfigField(FieldSpec.Type.BOOLEAN, "DEMO", "false", const = true)
  }
  // Demo builds run entirely on local fake data — no backend, so BASE_URL stays empty.
  defaultConfigs("demo") {
    buildConfigField(FieldSpec.Type.BOOLEAN, "DEBUG", "true")
    buildConfigField(FieldSpec.Type.STRING, "BASE_URL", "")
    buildConfigField(FieldSpec.Type.BOOLEAN, "DEMO", "true", const = true)
  }
  defaultConfigs("demoRelease") {
    buildConfigField(FieldSpec.Type.BOOLEAN, "DEBUG", "false")
    buildConfigField(FieldSpec.Type.STRING, "BASE_URL", "")
    buildConfigField(FieldSpec.Type.BOOLEAN, "DEMO", "true", const = true)
  }
}
