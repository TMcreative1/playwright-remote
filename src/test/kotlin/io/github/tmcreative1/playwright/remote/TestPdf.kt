package io.github.tmcreative1.playwright.remote

import io.github.tmcreative1.playwright.remote.base.BaseTest
import io.github.tmcreative1.playwright.remote.domain.ServerConfig
import io.github.tmcreative1.playwright.remote.engine.options.PdfOptions
import io.github.tmcreative1.playwright.remote.engine.parser.IParser.Companion.fromJson
import jdk.jfr.Description
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledIfSystemProperty
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.test.assertTrue

class TestPdf : BaseTest() {

    private fun getServerConfig(): ServerConfig {
        val configFile = Path("src", "main", "resources", "server", "config.json").toFile()
        return fromJson(configFile.readText(), ServerConfig::class.java)
    }

    @Test
    @Description("Test only for Chromimum headless")
    @DisabledIfSystemProperty(named = "browser", matches = "^\$|webkit|firefox")
    fun `check to save file`(@TempDir tempDir: Path) {
        Assumptions.assumeTrue(getServerConfig().headless)
        val path = tempDir.resolve("output.pdf")
        page.pdf(PdfOptions { it.path = path })
        val size = Files.size(path)
        assertTrue(size > 0)
    }

    @Test
    @Description("Test only for Chromimum headless")
    @DisabledIfSystemProperty(named = "browser", matches = "^\$|webkit|firefox")
    fun `check to support fractional scale value`(@TempDir tempDir: Path) {
        Assumptions.assumeTrue(getServerConfig().headless)
        val path = tempDir.resolve("output.pdf")
        page.pdf(PdfOptions {
            it.path = path
            it.scale = 0.5
        })
        val size = Files.size(path)
        assertTrue(size > 0)
    }
}