@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    js {
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                devServer = devServer?.copy(port = 3030)
            }
        }
        binaries.executable()
    }
    wasmJs {
        browser {
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                devServer = devServer?.copy(port = 3030)
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":core"))
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(libs.compose.material.icons.extended)
            implementation(libs.coil)
            implementation(libs.coil.network)
            implementation(libs.kotlinx.datetime)

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)

            implementation(libs.sentry.kotlin.multiplatform)
            implementation(libs.napier)
        }

        wasmJsMain.dependencies {
            implementation(project(":app:shared"))
            implementation(libs.ktor.client.js)
        }

        jsMain.dependencies {
            implementation(libs.ktor.client.js)
        }
    }
}
