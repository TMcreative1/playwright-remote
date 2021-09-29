package io.github.tmcreative1.playwright.remote

import io.github.tmcreative1.playwright.remote.base.BaseTest
import io.github.tmcreative1.playwright.remote.engine.options.tracing.StartTracingOptions
import io.github.tmcreative1.playwright.remote.engine.options.tracing.StopTracingOptions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertTrue

class TestTracing : BaseTest() {

    @Test
    fun `check to collect trace`(@TempDir tempDir: Path) {
        browserContext.tracing().start(StartTracingOptions {
            it.name = "test"
            it.screenshots = true
            it.snapshots = true
        })
        page.navigate(httpServer.emptyPage)
        page.setContent("<button>Click</button>")
        page.click("'Click'")
        page.close()
        val traceFile = tempDir.resolve("trace.zip")
        browserContext.tracing().stop(StopTracingOptions { it.path = traceFile })
        assertTrue(Files.exists(traceFile))
    }

    @Test
    fun `check to collect two traces`(@TempDir tempDir: Path) {
        browserContext.tracing().start(StartTracingOptions {
            it.name = "test1"
            it.screenshots = true
            it.snapshots = true
        })
        page.navigate(httpServer.emptyPage)
        page.setContent("<button>Click</button>")
        page.click("'Click'")
        val traceFile1 = tempDir.resolve("trace1.zip")
        browserContext.tracing().stop(StopTracingOptions { it.path = traceFile1 })

        browserContext.tracing().start(StartTracingOptions {
            it.name = "test2"
            it.screenshots = true
            it.snapshots = true
        })
        page.doubleClick("'Click'")
        page.close()
        val traceFile2 = tempDir.resolve("trace2.zip")
        browserContext.tracing().stop(StopTracingOptions { it.path = traceFile2 })

        assertTrue(Files.exists(traceFile1))
        assertTrue(Files.exists(traceFile2))
    }
}