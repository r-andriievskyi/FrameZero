plugins {
  id("crossplatform.library")
  id("crossplatform.di")
}

base { archivesName = "repository-force-update-impl" }

kotlin {
  android { namespace = "com.frame.zero.repository.force_update.impl" }

  applyDefaultHierarchyTemplate()

  sourceSets {
    commonMain.dependencies {
      api(projects.shared.repositories.forceUpdate.api)
      implementation(projects.shared.dto)
      implementation(projects.shared.repositories.deviceToken)
      implementation(libs.gitlive.firebase.config)
    }
    androidMain.dependencies {
      implementation(project.dependencies.platform(libs.firebase.bom))
    }
    commonTest.dependencies { implementation(libs.kotlin.test) }
  }
}

val skippedIosTestTasks = setOf(
  "linkDebugTestIosArm64",
  "linkDebugTestIosSimulatorArm64",
  "iosArm64Test",
  "iosSimulatorArm64Test"
)
tasks.matching { it.name in skippedIosTestTasks }.configureEach { enabled = false }
