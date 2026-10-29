plugins {
  id("crossplatform.library")
  id("crossplatform.di")
}

base { archivesName = "feature-task-list" }

kotlin {
  android { namespace = "com.frame.zero.feature.task.list" }

  sourceSets {
    commonMain.dependencies {
      api(projects.shared)
      api(projects.shared.uiText)
      api(libs.essenty.instanceKeeper)
      api(libs.androidx.paging.common)
      implementation(projects.shared.repositories.tasks.api)
      implementation(libs.kotlinx.coroutines.core)
    }
    commonTest.dependencies {
      implementation(projects.shared.testFixtures)
      implementation(libs.bundles.commonTest)
    }
  }
}
