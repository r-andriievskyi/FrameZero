plugins { id("crossplatform.library") }

base { archivesName = "integration-firebase" }

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(projects.shared)
      implementation(libs.koin.core)
      implementation(libs.gitlive.firebase.app)
      implementation(libs.gitlive.firebase.analytics)
      implementation(libs.gitlive.firebase.crashlytics)
    }
    // GitLive's Android artifacts pull the native com.google.firebase:* libraries but
    // leave their versions to the Firebase BOM — supply it here so they resolve.
    androidMain.dependencies {
      implementation(project.dependencies.platform(libs.firebase.bom))
    }
    commonTest.dependencies { implementation(libs.kotlin.test) }
  }
}

android { namespace = "com.frame.zero.integrations.firebase" }
