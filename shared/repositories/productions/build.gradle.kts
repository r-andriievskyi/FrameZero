plugins {
  id("crossplatform.kmp.library")
  alias(libs.plugins.ksp)
  alias(libs.plugins.androidxRoom)
}

base { archivesName = "repository-productions" }

kotlin {
  applyDefaultHierarchyTemplate()

  sourceSets {
    commonMain.dependencies {
      api(projects.shared)
      api(libs.androidx.paging.common)
      implementation(libs.koin.core)
      implementation(libs.kotlinx.coroutines.core)
      implementation(libs.ktor.clientCore)
      implementation(libs.ktor.clientContentNegotiation)
      implementation(libs.androidx.room.runtime)
      implementation(libs.androidx.room.paging)
      implementation(libs.androidx.sqlite.bundled)
    }

    commonTest.dependencies {
      implementation(libs.kotlin.test)
      implementation(libs.kotlinx.coroutines.test)
    }
  }
}

android { namespace = "com.frame.zero.repository.productions" }

room {
  schemaDirectory("$projectDir/build/schemas")
}

dependencies {
  add("kspAndroid", libs.androidx.room.compiler)
  add("kspIosArm64", libs.androidx.room.compiler)
  add("kspIosSimulatorArm64", libs.androidx.room.compiler)
}
