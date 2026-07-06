plugins {
  id("crossplatform.library")
}

base { archivesName = "repository-chat-impl" }

kotlin {
  android { namespace = "com.frame.zero.repository.chat.impl" }

  applyDefaultHierarchyTemplate()

  sourceSets {
    commonMain.dependencies {
      api(projects.shared.repositories.chat.api)
      implementation(projects.shared)
      implementation(projects.shared.database)
      implementation(libs.bundles.koinRuntime)
      implementation(libs.bundles.ktorClient)
    }

    commonTest.dependencies {
      implementation(projects.shared.testFixtures)
      implementation(libs.bundles.commonTest)
      implementation(libs.androidx.room.runtime)
      implementation(libs.androidx.sqlite.bundled)
    }
  }
}
