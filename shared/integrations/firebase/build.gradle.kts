plugins { id("crossplatform.library") }

base { archivesName = "integration-firebase" }

kotlin {
  android { namespace = "com.frame.zero.integrations.firebase" }

  applyDefaultHierarchyTemplate()

  sourceSets {
    commonMain.dependencies {
      api(projects.shared)
      implementation(libs.koin.core)
      implementation(libs.gitlive.firebase.app)
      implementation(libs.gitlive.firebase.analytics)
      implementation(libs.gitlive.firebase.crashlytics)
      implementation(libs.gitlive.firebase.perf)
    }
    androidMain.dependencies {
      implementation(project.dependencies.platform(libs.firebase.bom))
      implementation(libs.gitlive.firebase.messaging)
    }
    commonTest.dependencies { implementation(libs.kotlin.test) }
  }
}
// The GitLive Firebase SDK references native Firebase frameworks (FirebaseCore,
// FirebaseAnalytics, FirebaseCrashlytics) that are supplied to the final iOS app link
// via Xcode/SPM — never to Gradle. Linking a Kotlin/Native *test* executable for this
// module therefore fails with undefined symbols (`ld` errors). The module's only test
// is pure-Kotlin mapping logic that runs on the Android target, so disable the iOS test
// binaries and their run tasks. Remove this once the native frameworks are made
// available to the Gradle link (e.g. via the CocoaPods plugin).
val skippedIosTestTasks = setOf(
  "linkDebugTestIosArm64",
  "linkDebugTestIosSimulatorArm64",
  "iosArm64Test",
  "iosSimulatorArm64Test"
)
tasks.matching { it.name in skippedIosTestTasks }.configureEach { enabled = false }
