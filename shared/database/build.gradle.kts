plugins {
  id("crossplatform.library")
  alias(libs.plugins.ksp)
  alias(libs.plugins.androidxRoom)
}

base { archivesName = "database" }

kotlin {
  android { namespace = "com.frame.zero.database" }

  applyDefaultHierarchyTemplate()

  sourceSets {
    commonMain.dependencies {
      // Leaf persistence module: owns the single Room database shared across features, so it
      // deliberately depends on no app modules (not even :shared) to stay reusable.
      api(libs.androidx.paging.common)
      api(libs.androidx.room.runtime)
      implementation(libs.androidx.room.paging)
      implementation(libs.androidx.sqlite.bundled)
      implementation(libs.bundles.koinRuntime)
      implementation(libs.kotlinx.serialization.json)
    }

    commonTest.dependencies {
      implementation(libs.bundles.commonTest)
    }
  }
}
room {
  schemaDirectory("$projectDir/build/schemas")
}

dependencies {
  add("kspAndroid", libs.androidx.room.compiler)
  add("kspIosArm64", libs.androidx.room.compiler)
  add("kspIosSimulatorArm64", libs.androidx.room.compiler)
}
