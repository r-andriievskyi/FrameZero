import java.util.Properties

val releaseKeystoreProps = Properties().apply {
  val propsFile = rootProject.file("key.properties")
  if (propsFile.exists()) propsFile.inputStream().use { load(it) }
}

val requestedTaskNames = gradle.startParameter.taskNames.map { it.substringAfterLast(":") }
val demoFlavorRequested = requestedTaskNames.any { it.contains("Demo") } ||
  providers.gradleProperty("buildkonfig.flavor").orNull?.startsWith("demo") == true

// Only packaging is flavor-sensitive; compiling or unit-testing both variants together is fine.
fun isPackagingTask(name: String) = listOf("assemble", "install", "bundle", "package").any { name.startsWith(it) }

if (requestedTaskNames.any { it.contains("Demo") && isPackagingTask(it) } &&
  requestedTaskNames.any { it.contains("Prod") && isPackagingTask(it) }
) {
  error(
    "Demo and prod variant tasks cannot run in the same Gradle invocation — :shared compiles " +
      "for a single BuildKonfig flavor. Build them in separate commands."
  )
}

tasks.matching { it.name == "packageDemoDebug" || it.name == "packageDemoRelease" }.configureEach {
  // Local copy: the doFirst lambda must not capture the build script (configuration cache).
  val demoRequested = demoFlavorRequested
  doFirst {
    check(demoRequested) {
      "Packaging a demo APK via an aggregate task (assembleDebug, build, assembleRelease) " +
        "compiles :shared without the demo BuildKonfig flavor — the APK would NOT run on demo " +
        "data. Invoke a Demo-qualified task instead, e.g. :androidApp:assembleDemoDebug or " +
        ":androidApp:assembleProdRelease for the real app."
    }
  }
}

plugins {
  alias(libs.plugins.androidApplication)
  alias(libs.plugins.composeCompiler)
  alias(libs.plugins.googleServices)
  alias(libs.plugins.firebaseCrashlytics)
  alias(libs.plugins.firebasePerf)
  alias(libs.plugins.baselineprofile)
  id("crossplatform.code.quality")
}

android {
  namespace = "com.frame.zero"
  compileSdk = libs.versions.android.compileSdk.get().toInt()

  defaultConfig {
    applicationId = "com.frame.zero"
    minSdk = libs.versions.android.minSdk.get().toInt()
    targetSdk = libs.versions.android.targetSdk.get().toInt()
    versionCode = providers.gradleProperty("framezero.versionCode").get().toInt()
    versionName = providers.gradleProperty("framezero.versionName").get()
  }
  packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
  flavorDimensions += "mode"
  productFlavors {
    create("demo") { dimension = "mode" }
    create("prod") { dimension = "mode" }
  }
  signingConfigs {
    getByName("debug") {
      storeFile = rootProject.file("keystores/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
    if (releaseKeystoreProps.isNotEmpty()) {
      create("release") {
        storeFile = rootProject.file(releaseKeystoreProps.getProperty("storeFile"))
        storePassword = releaseKeystoreProps.getProperty("storePassword")
        keyAlias = releaseKeystoreProps.getProperty("keyAlias")
        keyPassword = releaseKeystoreProps.getProperty("keyPassword")
      }
    }
  }
  buildTypes {
    getByName("debug") {
      manifestPlaceholders["firebasePerformanceEnabled"] = false
    }
    getByName("release") {
      if (releaseKeystoreProps.isNotEmpty()) {
        signingConfig = signingConfigs.getByName("release")
      }
      isMinifyEnabled = true
      isShrinkResources = true
      manifestPlaceholders["firebasePerformanceEnabled"] = true
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  sourceSets {
    getByName("prod") {
      baselineProfiles.srcDir("src/demoRelease/generated/baselineProfiles")
    }
  }
}

androidComponents {
  onVariants { variant ->
    if (variant.buildType in setOf("nonMinifiedRelease", "benchmarkRelease")) {
      variant.manifestPlaceholders.put("firebasePerformanceEnabled", "false")
    }
  }
}

baselineProfile {
  // Committed at src/demoRelease/generated/baselineProfiles; regenerate with
  // :androidApp:generateDemoReleaseBaselineProfile (docs/performance.md).
  // mergeIntoMain would collapse generation into a single task spanning both flavors,
  // breaking the one-BuildKonfig-flavor-per-invocation rule.
  mergeIntoMain = false
  saveInSrc = true
  automaticGenerationDuringBuild = false
}

dependencies {
  implementation(projects.composeApp)
  implementation(projects.shared)
  implementation(projects.shared.repositories.deviceToken)
  implementation(projects.shared.features.account)
  implementation(projects.shared.features.auth)
  implementation(projects.shared.features.home)
  implementation(projects.shared.features.production)
  implementation(projects.shared.features.productionDetails)
  implementation(projects.shared.features.taskDetails)
  implementation(projects.shared.features.taskCreate)
  implementation(projects.shared.features.chat)
  implementation(projects.shared.features.taskList)
  implementation(projects.shared.features.forceUpdate)
  implementation(libs.decompose)
  implementation(libs.koin.core)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.compose.runtime)
  implementation(libs.compose.ui)
  implementation(libs.compose.foundation)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.core.splashscreen)
  implementation(libs.androidx.lifecycle.runtimeCompose)
  implementation(libs.androidx.profileinstaller)
  baselineProfile(projects.benchmarks)
  implementation(libs.androidx.fragment)
  implementation(project.dependencies.platform(libs.firebase.bom))
  implementation(libs.firebase.messaging)
  debugImplementation(libs.compose.uiTooling)
}
