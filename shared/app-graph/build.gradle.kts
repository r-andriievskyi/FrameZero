// Holds `AppGraph`, the accessor surface both platforms' Metro graphs implement: the Android
// graphs in :composeApp and the iOS graphs in :iosFramework. Compose-free so the iOS framework
// can depend on it.
plugins { id("crossplatform.library") }

base { archivesName = "app-graph" }

kotlin {
  android { namespace = "com.frame.zero.app_graph" }

  sourceSets {
    commonMain.dependencies {
      implementation(projects.shared)
      implementation(projects.shared.features.account)
      implementation(projects.shared.features.auth)
      implementation(projects.shared.features.home)
      implementation(projects.shared.features.production)
      implementation(projects.shared.features.productionDetails)
      implementation(projects.shared.features.taskDetails)
      implementation(projects.shared.features.taskCreate)
      implementation(projects.shared.features.taskList)
      implementation(projects.shared.features.forceUpdate)
      implementation(projects.shared.repositories.deviceToken)
      implementation(projects.shared.repositories.tasks.api)
    }
  }
}
