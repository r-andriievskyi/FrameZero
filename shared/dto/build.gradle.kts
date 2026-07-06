plugins {
  id("crossplatform.library")
  alias(libs.plugins.kotlinSerialization)
}

base { archivesName = "shared-dto" }

kotlin {
  android { namespace = "com.frame.zero.shared.dto" }

  sourceSets {
    commonMain.dependencies {
      api(projects.shared.domain)
    }

    commonTest.dependencies {
      implementation(libs.kotlin.test)
    }
  }
}
