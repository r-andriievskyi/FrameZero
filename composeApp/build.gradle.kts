import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

val releaseKeystoreProps = Properties().apply {
  val propsFile = rootProject.file("key.properties")
  if (propsFile.exists()) propsFile.inputStream().use { load(it) }
}

// :shared compiles for exactly one BuildKonfig flavor per Gradle invocation, inferred from the
// requested task names (see shared/build.gradle.kts). Two guards keep flavored APKs honest:
// building demo and prod variants together would silently give one of them the other's flavor,
// and packaging demo through an aggregate task (assembleDebug, build) would produce a "demo"
// APK compiled with the default (non-demo) BuildKonfig. Only packaging is guarded — compiling
// and unit-testing both variants in one invocation (CI's `check`) is flavor-independent.
val requestedTaskNames = gradle.startParameter.taskNames.map { it.substringAfterLast(":") }
val demoFlavorRequested = requestedTaskNames.any { it.contains("Demo") } ||
  providers.gradleProperty("buildkonfig.flavor").orNull?.startsWith("demo") == true

// Only packaging is flavor-sensitive; compiling or unit-testing both variants together is fine.
fun isPackagingTask(name: String) = listOf("assemble", "install", "bundle", "package").any { name.startsWith(it) }

if (requestedTaskNames.any { it.contains("Demo") && isPackagingTask(it) } &&
  requestedTaskNames.any { it.contains("Prod") && isPackagingTask(it) }
) {
  error(
    "Demo and prod variant tasks cannot run in the same Gradle invocation — :shared compiles " +
      "for a single BuildKonfig flavor. Build them in separate commands."
  )
}

tasks.matching { it.name == "packageDemoDebug" || it.name == "packageDemoRelease" }.configureEach {
  // Local copy: the doFirst lambda must not capture the build script (configuration cache).
  val demoRequested = demoFlavorRequested
  doFirst {
    check(demoRequested) {
      "Packaging a demo APK via an aggregate task (assembleDebug, build, assembleRelease) " +
        "compiles :shared without the demo BuildKonfig flavor — the APK would NOT run on demo " +
        "data. Invoke a Demo-qualified task instead, e.g. :composeApp:assembleDemoDebug or " +
        ":composeApp:assembleProdRelease for the real app."
    }
  }
}

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidApplication)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
  alias(libs.plugins.kotlinSerialization)
  alias(libs.plugins.googleServices)
  alias(libs.plugins.firebaseCrashlytics)
  alias(libs.plugins.firebasePerf)
  id("crossplatform.code.quality")
}

composeCompiler {
  stabilityConfigurationFiles.add(
    rootProject.layout.projectDirectory.file("stability_config.conf")
  )
  if (providers.gradleProperty("enableComposeCompilerReports").orNull == "true") {
    val out = layout.buildDirectory.dir("compose_reports")
    reportsDestination.set(out)
    metricsDestination.set(out)
  }
}

kotlin {
  jvmToolchain(21)
  androidTarget { compilerOptions { jvmTarget.set(JvmTarget.JVM_11) } }

  listOf(iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
    iosTarget.binaries.framework {
      baseName = "ComposeApp"
      isStatic = true
    }
  }

  sourceSets {
    androidMain.dependencies {
      implementation(libs.compose.uiToolingPreview)
      implementation(libs.androidx.activity.compose)
      implementation(project.dependencies.platform(libs.firebase.bom))
      implementation(libs.firebase.messaging)
      implementation(libs.androidx.core.ktx)
      implementation(libs.androidx.core.splashscreen)
      implementation(libs.androidx.biometric)
      implementation(libs.androidx.fragment)
      implementation(projects.shared.repositories.deviceToken)
    }
    commonMain.dependencies {
      implementation(libs.compose.runtime)
      implementation(libs.compose.foundation)
      implementation(libs.compose.material3)
      implementation(libs.compose.ui)
      implementation(libs.compose.components.resources)
      implementation(libs.compose.uiToolingPreview)
      implementation(libs.androidx.lifecycle.viewmodelCompose)
      implementation(libs.androidx.lifecycle.runtimeCompose)
      implementation(projects.shared)
      implementation(projects.shared.database)
      implementation(projects.shared.features.account)
      implementation(projects.shared.features.auth)
      implementation(projects.shared.features.home)
      implementation(projects.shared.features.production)
      implementation(projects.shared.features.productionDetails)
      implementation(projects.shared.features.taskDetails)
      implementation(projects.shared.features.taskCreate)
      implementation(projects.shared.features.chat)
      implementation(projects.shared.repositories.productions.impl)
      implementation(projects.shared.repositories.chat.impl)
      implementation(projects.shared.repositories.deviceToken)
      implementation(projects.shared.integrations.firebase)
      implementation(projects.shared.demo)
      implementation(projects.composeApp.features.account)
      implementation(projects.composeApp.features.auth)
      implementation(projects.composeApp.features.home)
      implementation(projects.composeApp.features.production)
      implementation(projects.composeApp.features.productionDetails)
      implementation(projects.composeApp.features.taskDetails)
      implementation(projects.composeApp.features.taskCreate)
      implementation(projects.composeApp.features.chat)
      implementation(libs.koin.core)
      implementation(libs.kotlinx.serialization.json)
      implementation(libs.decompose)
      implementation(libs.decompose.extensionsCompose)
      implementation(projects.composeApp.shared.designSystem)
    }
    commonTest.dependencies {
      implementation(libs.bundles.commonTest)
      implementation(projects.shared.testFixtures)
      implementation(libs.multiplatformSettings)
      implementation(libs.multiplatformSettings.test)
    }
    androidUnitTest.dependencies {
      implementation(libs.kotlin.testJunit)
      implementation(libs.junit)
      implementation(libs.robolectric)
      implementation(libs.kotlinx.coroutines.test)
    }
  }
}

android {
  namespace = "com.frame.zero"
  compileSdk = libs.versions.android.compileSdk.get().toInt()

  defaultConfig {
    applicationId = "com.frame.zero"
    minSdk = libs.versions.android.minSdk.get().toInt()
    targetSdk = libs.versions.android.targetSdk.get().toInt()
    versionCode = providers.gradleProperty("framezero.versionCode").get().toInt()
    versionName = providers.gradleProperty("framezero.versionName").get()
  }
  packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
  testOptions { unitTests.isIncludeAndroidResources = true }
  // `demo` runs on local fake data (no backend); `prod` is the real network build. Same
  // applicationId — the demo build replaces the normal install rather than sitting beside it.
  // The DEMO/prod split reaches shared code via BuildKonfig, inferred from the task name in
  // shared/build.gradle.kts (assembleDemo* → buildkonfig.flavor=demo).
  flavorDimensions += "mode"
  productFlavors {
    create("demo") { dimension = "mode" }
    create("prod") { dimension = "mode" }
  }
  signingConfigs {
    getByName("debug") {
      storeFile = rootProject.file("keystores/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
    if (releaseKeystoreProps.isNotEmpty()) {
      create("release") {
        storeFile = rootProject.file(releaseKeystoreProps.getProperty("storeFile"))
        storePassword = releaseKeystoreProps.getProperty("storePassword")
        keyAlias = releaseKeystoreProps.getProperty("keyAlias")
        keyPassword = releaseKeystoreProps.getProperty("keyPassword")
      }
    }
  }
  buildTypes {
    getByName("debug") {
      // Gate Firebase Performance from process start (before any Kotlin runs), so the SDK's
      // automatic app-start/network/screen traces never reach the production dashboard.
      manifestPlaceholders["firebasePerformanceEnabled"] = false
    }
    getByName("release") {
      if (releaseKeystoreProps.isNotEmpty()) {
        signingConfig = signingConfigs.getByName("release")
      }
      isMinifyEnabled = true
      isShrinkResources = true
      manifestPlaceholders["firebasePerformanceEnabled"] = true
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
}

dependencies { debugImplementation(libs.compose.uiTooling) }

// Same workaround as :shared:integrations:firebase — this module links the GitLive Firebase
// klib, whose native frameworks (FirebaseCore, …) are supplied via Xcode/SPM only to the app
// link, never to Gradle. Kotlin/Native *test* executables therefore fail at `ld` with
// "framework 'FirebaseCore' not found". Tests in commonTest still run on the Android host.
// Remove once the native frameworks are made available to the Gradle link.
val skippedIosTestTasks = setOf(
  "linkDebugTestIosArm64",
  "linkDebugTestIosSimulatorArm64",
  "iosArm64Test",
  "iosSimulatorArm64Test"
)
tasks.matching { it.name in skippedIosTestTasks }.configureEach { enabled = false }
