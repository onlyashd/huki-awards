plugins {
    alias(libs.plugins.ktor)
    kotlin("jvm")
    kotlin("plugin.serialization")
}

group = "io.github.onlyashd"

version = "1.0.0"

application {
    mainClass.set("io.github.onlyashd.hukiawards.ApplicationKt")
}

dependencies {
    implementation(project(":core"))

    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)

    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)

    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.jvm)
    implementation(libs.ktor.cors)

    implementation(libs.postgresql)
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.java.time)
    implementation(libs.hikaricp)

    implementation(libs.sentry.core)
    implementation(libs.logback)
    implementation(libs.kotlin.logging)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}