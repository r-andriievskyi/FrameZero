// iOS-only Obj-C framework for the SwiftUI app (docs/ios-swiftui-migration.md). Owns the iOS
// Metro graphs and the Swift entry points; SKIE turns its header into idiomatic Swift (sealed →
// enum, Flow → AsyncSequence, suspend → async). Xcode links it via embedAndSignAppleFrameworkForXcode.
plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.metro)
  alias(libs.plugins.skie)
}

kotlin {
  jvmToolchain(21)

  listOf(iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
    iosTarget.binaries.framework {
      baseName = "FrameZero"
      isStatic = true
      // Only `api` dependencies can be exported (Kotlin/Native rule). This leaf module is the
      // one place `api` is allowed: nothing depends on it, so nothing leaks transitively.
      exportedModules.forEach { export(it) }
    }
  }

  sourceSets {
    iosMain.dependencies {
      exportedModules.forEach { api(it) }
      // Graph-only: implementations the iOS graphs aggregate, never named from Swift.
      implementation(projects.shared.database)
      implementation(projects.shared.demo)
      implementation(projects.shared.repositories.productions.impl)
      implementation(projects.shared.repositories.tasks.impl)
      implementation(projects.shared.repositories.forceUpdate.impl)
      implementation(projects.shared.repositories.deviceToken)
      implementation(projects.shared.integrations.firebase)
    }
  }
}

skie {
  analytics {
    // SKIE uploads anonymous build analytics by default.
    disableUpload.set(true)
  }
}

/** Modules whose types Swift uses by name (graph, state/intent/event types, design tokens). */
val exportedModules
  get() = listOf(
    projects.shared,
    projects.shared.appGraph,
    projects.shared.domain,
    projects.shared.uiText,
    projects.shared.designTokens,
    projects.shared.features.account,
    projects.shared.features.auth,
    projects.shared.features.home,
    projects.shared.features.production,
    projects.shared.features.productionDetails,
    projects.shared.features.taskDetails,
    projects.shared.features.taskCreate,
    projects.shared.features.taskList,
    projects.shared.features.forceUpdate
  )

// Same workaround as :shared:integrations:firebase — this module links the GitLive Firebase
// klib, whose native frameworks (FirebaseCore, …) are supplied via Xcode/SPM only to the app
// link, never to Gradle. Kotlin/Native *test* executables therefore fail at `ld` with
// "framework 'FirebaseCore' not found". Remove once the native frameworks reach the Gradle link.
val skippedIosTestTasks = setOf(
  "linkDebugTestIosArm64",
  "linkDebugTestIosSimulatorArm64",
  "iosArm64Test",
  "iosSimulatorArm64Test"
)
tasks.matching { it.name in skippedIosTestTasks }.configureEach { enabled = false }
