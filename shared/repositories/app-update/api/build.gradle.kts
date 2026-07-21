plugins { id("crossplatform.library") }

base { archivesName = "repository-app-update-api" }

kotlin {
  android { namespace = "com.frame.zero.repository.app_update.api" }

  sourceSets {
    commonMain.dependencies {
      api(projects.shared.domain)
    }
  }
}
