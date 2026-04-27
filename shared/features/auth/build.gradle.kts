plugins { id("crossplatform.kmp.library") }

base { archivesName = "feature-auth" }

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(projects.shared)
      api(projects.shared.repositories.auth)
      api(libs.decompose)
      implementation(libs.koin.core)
      implementation(libs.kotlinx.coroutines.core)
      implementation(libs.ktor.clientContentNegotiation)
      implementation(libs.ktor.clientSerializationJson)
    }
    commonTest.dependencies { implementation(libs.kotlin.test) }
  }
}

android { namespace = "com.frame.zero.feature.auth" }
