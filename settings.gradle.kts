rootProject.name = "FrameZero"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
  includeBuild("build-logic")
  repositories {
    google {
      mavenContent {
        includeGroupAndSubgroups("androidx")
        includeGroupAndSubgroups("com.android")
        includeGroupAndSubgroups("com.google")
      }
    }
    mavenCentral()
    gradlePluginPortal()
  }
}

dependencyResolutionManagement {
  repositories {
    google {
      mavenContent {
        includeGroupAndSubgroups("androidx")
        includeGroupAndSubgroups("com.android")
        includeGroupAndSubgroups("com.google")
      }
    }
    mavenCentral()
  }
}

plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

include(":detekt-rules")
include(":androidApp")
include(":benchmarks")
include(":composeApp")
include(":composeApp:features:auth")
include(":composeApp:features:home")
include(":composeApp:features:production")
include(":composeApp:features:account")
include(":composeApp:features:production-details")
include(":composeApp:features:task-details")
include(":composeApp:features:task-create")
include(":composeApp:features:task-list")
include(":composeApp:features:chat")
include(":server")
include(":shared")
include(":shared:domain")
include(":shared:dto")
include(":shared:database")
include(":shared:test-fixtures")
include(":shared:demo")
include(":shared:ui_text")
include(":shared:features:account")
include(":shared:features:auth")
include(":shared:features:home")
include(":shared:features:production")
include(":shared:features:production-details")
include(":shared:features:task-details")
include(":shared:features:task-create")
include(":shared:features:task-list")
include(":shared:features:chat")
include(":shared:repositories:auth")
include(":shared:repositories:user")
include(":shared:repositories:dashboard")
include(":shared:repositories:productions:api")
include(":shared:repositories:productions:impl")
include(":shared:repositories:schedule")
include(":shared:repositories:tasks")
include(":shared:repositories:chat:api")
include(":shared:repositories:chat:impl")
include(":shared:repositories:device-token")
include(":shared:integrations:firebase")
include(":composeApp:shared:design_system")
include(":composeApp:shared:screenshot-testing")
include(":composeApp:shared:ui_text")
