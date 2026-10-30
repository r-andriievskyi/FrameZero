plugins {
  id("crossplatform.library")
  id("crossplatform.di")
}

base { archivesName = "feature-task-create" }

kotlin {
  android { namespace = "com.frame.zero.feature.task.create" }

  sourceSets {
    commonMain.dependencies {
      api(projects.shared)
      api(projects.shared.uiText)
      api(libs.essenty.instanceKeeper)
      implementation(projects.shared.repositories.tasks.api)
      implementation(projects.shared.repositories.productions.api)
      implementation(libs.kotlinx.coroutines.core)
      implementation(libs.kotlinx.datetime)
    }
    commonTest.dependencies {
      implementation(projects.shared.testFixtures)
      implementation(libs.bundles.commonTest)
    }
  }
}
