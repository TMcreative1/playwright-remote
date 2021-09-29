package io.github.tmcreative1.playwright.remote

import com.google.gson.JsonObject
import io.github.tmcreative1.playwright.remote.base.BaseTest
import io.github.tmcreative1.playwright.remote.core.exceptions.PlaywrightException
import io.github.tmcreative1.playwright.remote.engine.options.StartTracingOptions
import io.github.tmcreative1.playwright.remote.engine.parser.IParser
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfSystemProperty
import org.junit.jupiter.api.io.TempDir
import java.io.FileReader
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

@EnabledIfSystemProperty(named = "browser", matches = "chromium")
class TestChromiumTracing : BaseTest() {

    @Test
    fun `check output a trace`(@TempDir tempDir: Path) {
        browser.newPage().use {
            val outputTraceFile = tempDir.resolve("trace.json")
            browser.startTracing(it, StartTracingOptions { opt ->
                opt.screenshots = true
                opt.path = outputTraceFile
            })
            it.navigate("${httpServer.prefixWithDomain}/grid.html")
            browser.stopTracing()
            assertTrue(Files.exists(outputTraceFile))
        }
    }

    @Test
    fun `check to create directories as needed`(@TempDir tempDir: Path) {
        browser.newPage().use {
            val filePath = tempDir.resolve("these/are/directories/trace.json")
            browser.startTracing(page, StartTracingOptions { opt ->
                opt.screenshots = true
                opt.path = filePath
            })
            it.navigate("${httpServer.prefixWithDomain}/grid.html")
            browser.stopTracing()
            assertTrue(Files.exists(filePath))
        }
    }

    @Test
    fun `check to run with custom categories if provided`(@TempDir tempDir: Path) {
        browser.newPage().use {
            val outputTraceFile = tempDir.resolve("trace.json")
            browser.startTracing(it, StartTracingOptions { opt ->
                opt.path = outputTraceFile
                opt.categories = arrayListOf("disabled-by-default-v8.cpu_profiler.hires")
            })
            browser.stopTracing()
            FileReader(outputTraceFile.toFile()).use { file ->
                val traceJson = IParser.fromJson(file, JsonObject::class.java)
                assertTrue(traceJson["metadata"].asJsonObject["trace-config"].asString.contains("disabled-by-default-v8.cpu_profiler.hires"))
            }
        }
    }

    @Test
    fun `check to throw if tracing on two pages`(@TempDir tempDir: Path) {
        browser.newPage().use {
            val outputTraceFile = tempDir.resolve("trace.json")
            browser.startTracing(it, StartTracingOptions { opt ->
                opt.path = outputTraceFile
            })
            val pg = browser.newPage()
            try {
                browser.startTracing(pg, StartTracingOptions { opt -> opt.path = outputTraceFile })
                fail("startTracing should throw")
            } catch (e: PlaywrightException) {
                assertTrue(e.message!!.contains("Cannot start recording trace while already recording trace"))
            }
            pg.close()
            browser.stopTracing()
        }
    }

    @Test
    fun `check to return a buffer`(@TempDir tempDir: Path) {
        browser.newPage().use {
            val outputTraceFile = tempDir.resolve("trace.json")
            browser.startTracing(it, StartTracingOptions { opt ->
                opt.screenshots = true
                opt.path = outputTraceFile
            })
            it.navigate("${httpServer.prefixWithDomain}/grid.html")
            val trace = browser.stopTracing()
            val buf = Files.readAllBytes(outputTraceFile)
            assertArrayEquals(buf, trace)
        }
    }

    @Test
    fun `check correct work without options`() {
        browser.newPage().use {
            browser.startTracing(it)
            it.navigate("${httpServer.prefixWithDomain}/grid.html")
            val trace = browser.stopTracing()
            assertNotNull(trace)
        }
    }

    @Test
    fun `check to support a buffer without a path`() {
        browser.newPage().use {
            browser.startTracing(it, StartTracingOptions { opt -> opt.screenshots = true })
            it.navigate("${httpServer.prefixWithDomain}/grid.html")
            val trace = browser.stopTracing()
            assertTrue(String(trace, StandardCharsets.UTF_8).contains("screenshot"))
        }
    }
}