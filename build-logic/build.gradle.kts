plugins {
  `kotlin-dsl`
}

// kotlin-gradle-plugin 2.3.x ships kotlin-stdlib with metadata format 2.3.0,
// but Gradle's embedded Kotlin compiler only understands 2.0.0.
// Force all org.jetbrains.kotlin artifacts to the embedded version so compiler
// and classpath stay in sync. These are compileOnly, so the main build still
// uses Kotlin 2.3.x at runtime.
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
  compileOnly(libs.gradle.plugin.compose.multiplatform)
  compileOnly(libs.gradle.plugin.detekt)
  compileOnly(libs.gradle.plugin.ktfmt)
}

gradlePlugin {
  plugins {
    register("kmpLibrary") {
      id = "crossplatform.kmp.library"
      implementationClass = "KmpLibraryConventionPlugin"
    }
    register("kmpLibraryCompose") {
      id = "crossplatform.kmp.library.compose"
      implementationClass = "KmpLibraryComposeConventionPlugin"
    }
    register("codeQuality") {
      id = "crossplatform.code.quality"
      implementationClass = "CodeQualityConventionPlugin"
    }
  }
}
