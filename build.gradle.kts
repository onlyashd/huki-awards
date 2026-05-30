plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.ktor) apply false
    alias(libs.plugins.buildkonfig) apply false
}

tasks.register("cleanAll", Delete::class) {
    group = "build"
    description = "Forcefully cleans all build directories, including the root."

    delete(rootProject.layout.buildDirectory)

    subprojects {
        val cleanTask = tasks.findByName("clean")
        if (cleanTask != null) {
            dependsOn(cleanTask)
        }
    }
}
