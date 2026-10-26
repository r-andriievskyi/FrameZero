// iOS-only Obj-C framework of the shared Kotlin layer for the SwiftUI app
// (docs/ios-swiftui-migration.md, P1). SKIE turns its header into idiomatic Swift (sealed →
// enum, Flow → AsyncSequence, suspend → async). Not wired into Xcode until C2 — the app still
// links :composeApp's framework.
plugins {
  alias(libs.plugins.kotlinMultiplatform)
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
    }
  }
}

skie {
  analytics {
    // SKIE uploads anonymous build analytics by default.
    disableUpload.set(true)
  }
}

/** Modules whose state, intent and event types Swift uses by name. */
val exportedModules
  get() = listOf(
    projects.shared,
    projects.shared.domain,
    projects.shared.uiText,
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
