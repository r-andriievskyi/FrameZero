import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidKmpLibrary)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
  alias(libs.plugins.kotlinSerialization)
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
  android {
    namespace = "com.frame.zero.composeapp"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    minSdk = libs.versions.android.minSdk.get().toInt()
    compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
    androidResources { enable = true }
    withHostTest { isIncludeAndroidResources = true }
  }

  listOf(iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
    iosTarget.binaries.framework {
      baseName = "ComposeApp"
      isStatic = true
    }
  }

  sourceSets {
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
      implementation(projects.shared.features.taskList)
      implementation(projects.shared.repositories.productions.impl)
      implementation(projects.shared.repositories.chat.impl)
      implementation(projects.shared.repositories.tasks.impl)
      implementation(projects.shared.repositories.appUpdate.impl)
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
      implementation(projects.composeApp.features.taskList)
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
    getByName("androidHostTest").dependencies {
      implementation(libs.kotlin.testJunit)
      implementation(libs.junit)
      implementation(libs.robolectric)
      implementation(libs.kotlinx.coroutines.test)
    }
  }
}

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
