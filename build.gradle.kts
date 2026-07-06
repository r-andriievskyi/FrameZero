plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidKmpLibrary) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.kover)
    alias(libs.plugins.ktlint) apply false
    id("crossplatform.library") apply false
    id("crossplatform.library.compose") apply false
    id("crossplatform.code.quality") apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.ktor) apply false
    alias(libs.plugins.roborazzi) apply false
    alias(libs.plugins.googleServices) apply false
    alias(libs.plugins.firebaseCrashlytics) apply false
    alias(libs.plugins.firebasePerf) apply false
    alias(libs.plugins.moduleGraph)
}

subprojects {
    val module = this
    plugins.withId("org.jetbrains.kotlinx.kover") {
        rootProject.dependencies.add("kover", module)
    }
}

// Generates a Mermaid module dependency graph into docs/module-graph.md.
// Regenerate with: ./gradlew createModuleGraph
moduleGraphConfig {
    readmePath.set("${rootDir}/docs/module-graph.md")
    heading.set("## Module Graph")
    // Hide cross-cutting noise: the detekt ruleset every module wires in, and
    // the root-project kover aggregation edges that fan out to every module.
    excludedModulesRegex.set(".*:detekt-rules")
    excludedConfigurationsRegex.set("kover|detektPlugins")
}


