import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

val archs = listOf("mac", "linux", "win32", "win32_x64")
val playwrightVersion = "1.11.1-1621490832000"

tasks.register("downloadAndUnzip") {
    var baseUrl = "https://playwright.azureedge.net/builds/driver/"
    if (!playwrightVersion.matches(Regex("^[0-9]+\\.[0-9]+\\.[0-9]+\$"))) {
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
        return@register
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