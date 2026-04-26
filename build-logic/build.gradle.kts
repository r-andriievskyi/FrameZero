plugins {
    `kotlin-dsl`
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