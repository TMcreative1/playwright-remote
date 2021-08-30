import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val archs = listOf("mac", "linux", "win32", "win32_x64")
val playwrightVersion = "1.10.0"

plugins {
    kotlin("jvm") version "1.5.20-M1"
    application
    jacoco
}

jacoco {
    toolVersion = "0.8.7"
    reportsDirectory.set(layout.buildDirectory.dir("jacocoReportDir"))
}

group = "io.github.tmcreative1"
val artifactBaseName = "playwright.remote"
version = "1.0-SNAPSHOT"

apply(from = "${rootProject.file("gradle/dependencies.gradle.kts")}")
apply(from = "${rootProject.file("gradle/tests.gradle.kts")}")
apply(from = "${rootProject.file("gradle/tasks.gradle.kts")}")

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}