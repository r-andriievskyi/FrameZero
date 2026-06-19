plugins { id("crossplatform.library") }

base { archivesName = "integration-firebase" }

kotlin {
  applyDefaultHierarchyTemplate()

  sourceSets {
    commonMain.dependencies {
      api(projects.shared)
      implementation(libs.koin.core)
      implementation(libs.gitlive.firebase.app)
      implementation(libs.gitlive.firebase.analytics)
      implementation(libs.gitlive.firebase.crashlytics)
    }
    androidMain.dependencies {
      implementation(project.dependencies.platform(libs.firebase.bom))
      implementation(libs.gitlive.firebase.messaging)
    }
    commonTest.dependencies { implementation(libs.kotlin.test) }
  }
}

android { namespace = "com.frame.zero.integrations.firebase" }
