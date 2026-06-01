plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.kover)
    alias(libs.plugins.ktlint) apply false
    id("crossplatform.kmp.library") apply false
    id("crossplatform.kmp.library.compose") apply false
    id("crossplatform.code.quality") apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.ktor) apply false
}

dependencies {
    kover(project(":shared"))
    kover(project(":shared:features:auth"))
    kover(project(":shared:features:home"))
    kover(project(":shared:repositories:auth"))
    kover(project(":shared:repositories:user"))
    kover(project(":server"))
    kover(project(":composeApp"))
    kover(project(":composeApp:features:auth"))
    kover(project(":composeApp:features:home"))
    kover(project(":composeApp:shared:design_system"))
}