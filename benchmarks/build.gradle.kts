plugins {
  id("com.android.test")
  alias(libs.plugins.baselineprofile)
}

android {
  namespace = "com.frame.zero.benchmarks"
  compileSdk = libs.versions.android.compileSdk.get().toInt()

  defaultConfig {
    minSdk = libs.versions.android.minSdk.get().toInt()
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    testInstrumentationRunnerArguments["androidx.benchmark.suppressErrors"] = "EMULATOR"
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  flavorDimensions += "mode"
  productFlavors {
    create("demo") { dimension = "mode" }
    create("prod") { dimension = "mode" }
  }

  targetProjectPath = ":androidApp"
  experimentalProperties["android.experimental.self-instrumenting"] = true
}

androidComponents {
  beforeVariants(selector().withFlavor("mode" to "prod")) { it.enable = false }
}

baselineProfile {
  useConnectedDevices = true
}

dependencies {
  implementation(libs.androidx.benchmark.macroJunit4)
  implementation(libs.androidx.uiautomator)
  implementation(libs.androidx.testExt.junit)
  implementation(libs.junit)
}
