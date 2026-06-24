plugins {
  id("crossplatform.library")
  alias(libs.plugins.ksp)
  alias(libs.plugins.androidxRoom)
}

base { archivesName = "database" }

kotlin {
  applyDefaultHierarchyTemplate()

  sourceSets {
    commonMain.dependencies {
      // Leaf persistence module: owns the single Room database shared across features, so it
      // deliberately depends on no app modules (not even :shared) to stay reusable.
      api(libs.androidx.paging.common)
      api(libs.androidx.room.runtime)
      implementation(libs.androidx.room.paging)
      implementation(libs.androidx.sqlite.bundled)
      implementation(libs.koin.core)
      implementation(libs.kotlinx.coroutines.core)
      implementation(libs.kotlinx.serialization.json)
    }

    commonTest.dependencies {
      implementation(libs.kotlin.test)
      implementation(libs.kotlinx.coroutines.test)
    }
  }
}

android { namespace = "com.frame.zero.database" }

room {
  schemaDirectory("$projectDir/build/schemas")
}

dependencies {
  add("kspAndroid", libs.androidx.room.compiler)
  add("kspIosArm64", libs.androidx.room.compiler)
  add("kspIosSimulatorArm64", libs.androidx.room.compiler)
}
