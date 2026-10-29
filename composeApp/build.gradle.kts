import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidKmpLibrary)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
  alias(libs.plugins.kotlinSerialization)
  alias(libs.plugins.metro)
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
      implementation(projects.shared.appGraph)
      implementation(projects.shared.database)
      implementation(projects.shared.features.account)
      implementation(projects.shared.features.auth)
      implementation(projects.shared.features.home)
      implementation(projects.shared.features.production)
      implementation(projects.shared.features.productionDetails)
      implementation(projects.shared.features.taskDetails)
      implementation(projects.shared.features.taskCreate)
      implementation(projects.shared.features.taskList)
      implementation(projects.shared.features.forceUpdate)
      implementation(projects.shared.repositories.productions.impl)
      implementation(projects.shared.repositories.tasks.impl)
      implementation(projects.shared.repositories.forceUpdate.impl)
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
      implementation(projects.composeApp.features.taskList)
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

dependencies {
  add("androidRuntimeClasspath", libs.compose.uiTooling)
}
