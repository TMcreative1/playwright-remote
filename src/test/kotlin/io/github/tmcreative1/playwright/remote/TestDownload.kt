package io.github.tmcreative1.playwright.remote

import io.github.tmcreative1.playwright.remote.base.BaseTest
import io.github.tmcreative1.playwright.remote.engine.options.NewPageOptions
import io.github.tmcreative1.playwright.remote.core.exceptions.PlaywrightException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

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

    @Test
    fun `check to save to two different paths with multiple save as calls`() {
        val pg = browser.newPage(NewPageOptions { it.acceptDownloads = true })
        pg.setContent("<a href='${httpServer.prefixWithDomain}/download'>download</a>")
        val download = pg.waitForDownload { pg.click("a") }

        val userFile = Files.createTempFile("download-", ".txt")
        assertNotNull(download)
        download.saveAs(userFile)

        assertTrue(Files.exists(userFile))
        var bytes = Files.readAllBytes(userFile)
        assertEquals("Hello world", String(bytes, StandardCharsets.UTF_8))

        val anotherUserPath = Files.createTempFile("download-2-", ".txt")
        download.saveAs(anotherUserPath)
        assertTrue(Files.exists(anotherUserPath))
        bytes = Files.readAllBytes(anotherUserPath)
        assertEquals("Hello world", String(bytes, StandardCharsets.UTF_8))
        pg.close()
    }

    @Test
    fun `check to save to overwritten filepath`() {
        val pg = browser.newPage(NewPageOptions { it.acceptDownloads = true })
        pg.setContent("<a href='${httpServer.prefixWithDomain}/download'>download</a>")
        val download = pg.waitForDownload { pg.click("a") }
        val userFile = Files.createTempFile("download-", ".txt")

        assertNotNull(download)
        download.saveAs(userFile)
        assertTrue(Files.exists(userFile))
        var bytes = Files.readAllBytes(userFile)
        assertEquals("Hello world", String(bytes, StandardCharsets.UTF_8))

        download.saveAs(userFile)
        assertTrue(Files.exists(userFile))
        bytes = Files.readAllBytes(userFile)
        assertEquals("Hello world", String(bytes, StandardCharsets.UTF_8))
        pg.close()
    }

    @Test
    fun `check to create subdirectories when saving to non existent user specified path`() {
        val pg = browser.newPage(NewPageOptions { it.acceptDownloads = true })
        pg.setContent("<a href='${httpServer.prefixWithDomain}/download'>download</a>")
        val download = pg.waitForDownload { pg.click("a") }
        assertNotNull(download)

        val downloads = Files.createTempDirectory("downloads")
        val nestedPath = downloads.resolve(Paths.get("these", "are", "directories", "download.txt"))
        download.saveAs(nestedPath)
        assertTrue(Files.exists(nestedPath))

        val bytes = Files.readAllBytes(nestedPath)
        assertEquals("Hello world", String(bytes, StandardCharsets.UTF_8))
        pg.close()
    }

    @Test
    fun `check to throw error when saving with downloads disabled`() {
        val pg = browser.newPage(NewPageOptions { it.acceptDownloads = false })
        pg.setContent("<a href='${httpServer.prefixWithDomain}/download'>download</a>")
        val download = pg.waitForDownload { pg.click("a") }
        assertNotNull(download)

        val userPath = Files.createTempFile("download-", ".txt")
        try {
            download.saveAs(userPath)
            fail("saveAs should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Pass { acceptDownloads: true } when you are creating your browser context"))
        }
        pg.close()
    }

    @Test
    fun `check to throw error when saving after deletion`() {
        val pg = browser.newPage(NewPageOptions { it.acceptDownloads = true })
        pg.setContent("<a href='${httpServer.prefixWithDomain}/download'>download</a>")
        val download = pg.waitForDownload { pg.click("a") }
        assertNotNull(download)

        val userPath = Files.createTempFile("download-", ".txt")
        download.delete()

        try {
            download.saveAs(userPath)
            fail("saveAs should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Download already deleted. Save before deleting."))
        }
        pg.close()
    }

    @Test
    fun `check to report non navigation downloads`() {
        httpServer.setRoute("/download") {
            it.responseHeaders.add("Content-Type", "application/octet-stream")
            it.sendResponseHeaders(200, 0)
            OutputStreamWriter(it.responseBody).use { wr ->
                wr.write("Hello world")
            }
        }

        browser.newPage(NewPageOptions { it.acceptDownloads = true }).use {
            it.navigate(httpServer.emptyPage)
            it.setContent("<a download='file.txt' href='${httpServer.prefixWithDomain}/download'>download</a>")
            val download = it.waitForDownload { it.click("a") }
            assertNotNull(download)
            assertEquals("file.txt", download.suggestedFilename())

            val userPath = Files.createTempFile("download-", ".txt")
            download.saveAs(userPath)
            assertTrue(Files.exists(userPath))

            val bytes = Files.readAllBytes(userPath)
            assertEquals("Hello world", String(bytes, StandardCharsets.UTF_8))
        }
    }
}