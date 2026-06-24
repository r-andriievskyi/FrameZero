import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

val releaseKeystoreProps = Properties().apply {
  val propsFile = rootProject.file("key.properties")
  if (propsFile.exists()) propsFile.inputStream().use { load(it) }
}

plugins {
  alias(libs.plugins.kotlinMultiplatform)
  alias(libs.plugins.androidApplication)
  alias(libs.plugins.composeMultiplatform)
  alias(libs.plugins.composeCompiler)
  alias(libs.plugins.kotlinSerialization)
  alias(libs.plugins.googleServices)
  alias(libs.plugins.firebaseCrashlytics)
  id("crossplatform.code.quality")
}

kotlin {
  jvmToolchain(21)
  androidTarget { compilerOptions { jvmTarget.set(JvmTarget.JVM_11) } }

  listOf(iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
    iosTarget.binaries.framework {
      baseName = "ComposeApp"
      isStatic = true
    }
  }

  sourceSets {
    androidMain.dependencies {
      implementation(libs.compose.uiToolingPreview)
      implementation(libs.androidx.activity.compose)
      implementation(project.dependencies.platform(libs.firebase.bom))
      implementation(libs.firebase.messaging)
      implementation(libs.androidx.core.ktx)
      implementation(libs.androidx.biometric)
      implementation(libs.androidx.fragment)
      implementation(projects.shared.repositories.deviceToken)
    }
    commonMain.dependencies {
      implementation(libs.compose.runtime)
      implementation(libs.compose.foundation)
      implementation(libs.compose.material3)
      implementation(libs.compose.ui)
      implementation(libs.compose.components.resources)
      implementation(libs.compose.uiToolingPreview)
      implementation(libs.androidx.lifecycle.viewmodelCompose)
      implementation(libs.androidx.lifecycle.runtimeCompose)
      implementation(projects.shared)
      implementation(projects.shared.database)
      implementation(projects.shared.features.account)
      implementation(projects.shared.features.auth)
      implementation(projects.shared.features.home)
      implementation(projects.shared.features.production)
      implementation(projects.shared.features.productionDetails)
      implementation(projects.shared.features.taskDetails)
      implementation(projects.shared.features.taskCreate)
      implementation(projects.shared.repositories.productions)
      implementation(projects.shared.repositories.deviceToken)
      implementation(projects.shared.integrations.firebase)
      implementation(projects.composeApp.features.account)
      implementation(projects.composeApp.features.auth)
      implementation(projects.composeApp.features.home)
      implementation(projects.composeApp.features.production)
      implementation(projects.composeApp.features.productionDetails)
      implementation(projects.composeApp.features.taskDetails)
      implementation(projects.composeApp.features.taskCreate)
      implementation(libs.koin.core)
      implementation(libs.kotlinx.serialization.json)
      implementation(libs.decompose)
      implementation(libs.decompose.extensionsCompose)
      implementation(projects.composeApp.shared.designSystem)
    }
    commonTest.dependencies { implementation(libs.kotlin.test) }
    androidUnitTest.dependencies {
      implementation(libs.kotlin.testJunit)
      implementation(libs.junit)
      implementation(libs.robolectric)
      implementation(libs.kotlinx.coroutines.test)
    }
  }
}

android {
  namespace = "com.frame.zero"
  compileSdk = libs.versions.android.compileSdk.get().toInt()

  defaultConfig {
    applicationId = "com.frame.zero"
    minSdk = libs.versions.android.minSdk.get().toInt()
    targetSdk = libs.versions.android.targetSdk.get().toInt()
    versionCode = 1
    versionName = "1.0"
  }
  packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
  testOptions { unitTests.isIncludeAndroidResources = true }
  signingConfigs {
    getByName("debug") {
      storeFile = rootProject.file("keystores/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
    if (releaseKeystoreProps.isNotEmpty()) {
      create("release") {
        storeFile = file(releaseKeystoreProps.getProperty("storeFile"))
        storePassword = releaseKeystoreProps.getProperty("storePassword")
        keyAlias = releaseKeystoreProps.getProperty("keyAlias")
        keyPassword = releaseKeystoreProps.getProperty("keyPassword")
      }
    }
  }
  buildTypes {
    getByName("release") {
      if (releaseKeystoreProps.isNotEmpty()) {
        signingConfig = signingConfigs.getByName("release")
      }
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
      signingConfig = signingConfigs.getByName("debug")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
}

dependencies { debugImplementation(libs.compose.uiTooling) }
