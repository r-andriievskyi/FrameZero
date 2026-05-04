plugins { id("crossplatform.kmp.library") }

base { archivesName = "feature-home" }

kotlin {
  sourceSets {
    commonMain.dependencies {
      api(projects.shared)
      api(projects.shared.repositories.user)
      api(projects.shared.repositories.dashboard)
      api(projects.shared.repositories.productions)
      api(projects.shared.repositories.schedule)
      api(libs.decompose)
      implementation(libs.koin.core)
      implementation(libs.kotlinx.coroutines.core)
      implementation(libs.ktor.clientContentNegotiation)
      implementation(libs.ktor.clientSerializationJson)
    }
    commonTest.dependencies { implementation(libs.kotlin.test) }
  }
}

android { namespace = "com.frame.zero.feature.home" }
