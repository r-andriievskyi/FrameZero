plugins { id("crossplatform.library") }

base { archivesName = "repository-app-update-impl" }

kotlin {
  android { namespace = "com.frame.zero.repository.app_update.impl" }

  applyDefaultHierarchyTemplate()

  sourceSets {
    commonMain.dependencies {
      api(projects.shared.repositories.appUpdate.api)
      implementation(projects.shared.dto)
      implementation(projects.shared.repositories.deviceToken)
      implementation(libs.koin.core)
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
