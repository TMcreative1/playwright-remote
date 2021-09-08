import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.20-M1"
    application
    jacoco
    `maven-publish`
}

group = "io.github.tmcreative1"
version = "1.13-SNAPSHOT"

apply(from = "${rootProject.file("gradle/source-sets.gradle.kts")}")
apply(from = "${rootProject.file("gradle/dependencies.gradle.kts")}")
apply(from = "${rootProject.file("gradle/coverage.gradle.kts")}")
apply(from = "${rootProject.file("gradle/tests.gradle.kts")}")
apply(from = "${rootProject.file("gradle/tasks.gradle.kts")}")
apply(from = "${rootProject.file("gradle/publish.gradle.kts")}")

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}