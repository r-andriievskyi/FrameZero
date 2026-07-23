plugins { id("crossplatform.library") }

base { archivesName = "repository-force-update-api" }

kotlin {
  android { namespace = "com.frame.zero.repository.force_update.api" }

  sourceSets {
    commonMain.dependencies {
      api(projects.shared.domain)
    }
  }
}
