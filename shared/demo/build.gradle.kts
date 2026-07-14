plugins { id("crossplatform.library") }

base { archivesName = "shared-demo" }

kotlin {
  android { namespace = "com.frame.zero.demo" }

  sourceSets {
    commonMain.dependencies {
      implementation(projects.shared)
      implementation(projects.shared.domain)
      implementation(projects.shared.dto)
      implementation(projects.shared.repositories.auth)
      implementation(projects.shared.repositories.user)
      implementation(projects.shared.repositories.dashboard)
      implementation(projects.shared.repositories.schedule)
      implementation(projects.shared.repositories.tasks)
      implementation(projects.shared.repositories.productions.api)
      implementation(projects.shared.repositories.chat.api)
      implementation(projects.shared.repositories.deviceToken)
      implementation(libs.androidx.paging.common)
      implementation(libs.kotlinx.coroutines.core)
      implementation(libs.kotlinx.datetime)
      implementation(libs.kotlinx.collections.immutable)
      implementation(libs.koin.core)
    }
    commonTest.dependencies {
      implementation(libs.bundles.commonTest)
      implementation(libs.multiplatformSettings)
      implementation(libs.multiplatformSettings.test)
    }
  }
}
