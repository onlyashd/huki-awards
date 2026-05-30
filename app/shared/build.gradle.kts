@file:OptIn(ExperimentalWasmDsl::class, ExperimentalJsExport::class)

import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import java.util.*
import kotlin.js.ExperimentalJsExport

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.buildkonfig)
}

val localProperties = Properties().apply {
    val propertiesFile = rootProject.file("local.properties")
    if (propertiesFile.exists()) {
        propertiesFile.inputStream().use { load(it) }
    }
}

val discordId = localProperties.getProperty("DISCORD_CLIENT_ID") ?: ""
val sentryDsn = localProperties.getProperty("SENTRY_DSN_SERVER") ?: ""

buildkonfig {
    packageName = "io.github.onlyashd.hukiawards.shared"
    objectName = "AppConfig"

    defaultConfigs {
        buildConfigField(STRING, "DISCORD_CLIENT_ID", discordId)
        buildConfigField(STRING, "SENTRY_DSN", sentryDsn)
    }
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
            implementation(libs.ktor.client.js)
        }

        jsMain.dependencies {
            implementation(libs.ktor.client.js)
        }
    }
}
