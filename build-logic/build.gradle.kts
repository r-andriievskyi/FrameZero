plugins {
  `kotlin-dsl`
}

configurations.all {
  resolutionStrategy.eachDependency {
    if (requested.group == "org.jetbrains.kotlin") {
      useVersion(embeddedKotlinVersion)
    }
  }
}

dependencies {
  compileOnly(libs.gradle.plugin.android)
  compileOnly(libs.gradle.plugin.kotlin)
  compileOnly(libs.gradle.plugin.compose.compiler)
  compileOnly(libs.gradle.plugin.compose.multiplatform)
  compileOnly(libs.gradle.plugin.detekt)
  compileOnly(libs.gradle.plugin.kover)
  compileOnly(libs.gradle.plugin.ktlint)
  compileOnly(libs.gradle.plugin.roborazzi)
}

gradlePlugin {
  plugins {
    register("kmpLibrary") {
      id = "crossplatform.library"
      implementationClass = "KmpLibraryConventionPlugin"
    }
    register("kmpLibraryCompose") {
      id = "crossplatform.library.compose"
      implementationClass = "KmpLibraryComposeConventionPlugin"
    }
    register("codeQuality") {
      id = "crossplatform.code.quality"
      implementationClass = "CodeQualityConventionPlugin"
    }
    register("screenshotTest") {
      id = "crossplatform.screenshot"
      implementationClass = "ScreenshotTestConventionPlugin"
    }
  }
}
