package com.playwright.remote

import com.playwright.remote.base.BaseTest
import com.playwright.remote.engine.options.NewPageOptions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestDownload : BaseTest() {

    @BeforeEach
    fun addRoutes() {
        httpServer.setRoute("/download") {
            it.responseHeaders.add("Content-Type", "application/octet-stream")
            it.responseHeaders.add("Content-Disposition", "attachment")
            it.sendResponseHeaders(200, 0)
            OutputStreamWriter(it.responseBody).use { wr ->
                wr.write("Hello world")
            }
        }
        httpServer.setRoute("/downloadWithFilename") {
            it.responseHeaders.add("Content-Type", "application/octet-stream")
            it.responseHeaders.add("Content-Disposition", "attachment; filename=file.txt")
            it.sendResponseHeaders(200, 0)
            OutputStreamWriter(it.responseBody).use { wr ->
                wr.write("Hello world")
            }
        }
    }

    @Test
    fun `check to save to user specified path`() {
        val pg = browser.newPage(NewPageOptions { it.acceptDownloads = true })
        pg.setContent("<a href='${httpServer.prefixWithDomain}/download'>download</a>")
        val download = pg.waitForDownload { pg.click("a") }

        val userFile = Files.createTempFile("download-", ".txt")
        assertNotNull(download)
        download.saveAs(userFile)
        assertTrue(Files.exists(userFile))
        val bytes = Files.readAllBytes(userFile)
        assertEquals("Hello world", String(bytes, StandardCharsets.UTF_8))
        pg.close()
    }
}