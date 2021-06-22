import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.*
import java.net.URL
import java.nio.file.*
import kotlin.collections.*

val archs = listOf("mac", "linux", "win32", "win32_x64")
val playwrightVersion = "1.11.1-1621490832000"

plugins {
    kotlin("jvm") version "1.5.20-M1"
    application
}

group = "com.playwright.remote"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    testImplementation(kotlin("test-junit5"))
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.0.0")
    implementation("com.squareup.okhttp3:okhttp:4.9.1")
    implementation("com.google.code.gson:gson:2.8.6")
}

tasks.test {
    useJUnitPlatform {
        include("**/Test*.class")
    }
    dependsOn("downloadAndUnzip")
    systemProperty("browser", System.getProperty("browser"))
    maxHeapSize = "2560m"
}

task<DefaultTask>("downloadAndUnzip") {
    var baseUrl = "https://playwright.azureedge.net/builds/driver/"
    if (playwrightVersion.contains("next", ignoreCase = true)) {
        baseUrl += "next/"
    }
    if (Files.exists(Paths.get("$projectDir/drivers"))) {
        println("Drivers have been already downloaded")
        archs.forEach { arch ->
            copy {
                from("$projectDir/src/main/resources/server/config.json")
                into("$projectDir/drivers/$arch")
            }
        }
        return@task
    }
    archs.forEach { arch ->
        val downloadFileName = "playwright-$playwrightVersion-$arch.zip"
        val downloadUrl = baseUrl + downloadFileName
        val downloadDirName = "$projectDir/drivers/$arch"
        val url = URL(downloadUrl)
        println("Downloading playwright driver for arch [ $arch ] using url - $url")
        val destDir = mkdir(downloadDirName)
        val destFilePath = destDir.resolve(downloadFileName).toPath()
        val destFile = Files.createFile(destFilePath)
        Files.copy(url.openStream(), destFile, StandardCopyOption.REPLACE_EXISTING)
        copy {
            from(zipTree(destFile))
            into(destDir)
            from("$projectDir/src/main/resources/server/config.json")
            into(destDir)
        }
        Files.deleteIfExists(destFile)
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}