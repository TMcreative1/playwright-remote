package io.github.tmcreative1.playwright.remote

import io.github.tmcreative1.playwright.remote.base.BaseTest
import io.github.tmcreative1.playwright.remote.core.exceptions.PlaywrightException
import io.github.tmcreative1.playwright.remote.engine.options.NewPageOptions
import io.github.tmcreative1.playwright.remote.engine.route.response.api.IResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Files
import java.nio.file.Files.readAllBytes
import java.nio.file.Paths
import kotlin.test.*

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
        httpServer.setRoute("/downloadWithDelay") {
            it.responseHeaders.add("Content-Type", "application/octet-stream")
            it.responseHeaders.add("Content-Disposition", "attachment; filename=file.txt")
            it.sendResponseHeaders(200, 0)
            OutputStreamWriter(it.responseBody).use { wr ->
                wr.write(Array(100 * 1024 * 1024) { "a" }.joinToString(separator = ""))
                wr.write("foo")
                wr.flush()
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
        val bytes = readAllBytes(userFile)
        assertEquals("Hello world", String(bytes, UTF_8))
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
        var bytes = readAllBytes(userFile)
        assertEquals("Hello world", String(bytes, UTF_8))

        val anotherUserPath = Files.createTempFile("download-2-", ".txt")
        download.saveAs(anotherUserPath)
        assertTrue(Files.exists(anotherUserPath))
        bytes = readAllBytes(anotherUserPath)
        assertEquals("Hello world", String(bytes, UTF_8))
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
        var bytes = readAllBytes(userFile)
        assertEquals("Hello world", String(bytes, UTF_8))

        download.saveAs(userFile)
        assertTrue(Files.exists(userFile))
        bytes = readAllBytes(userFile)
        assertEquals("Hello world", String(bytes, UTF_8))
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

        val bytes = readAllBytes(nestedPath)
        assertEquals("Hello world", String(bytes, UTF_8))
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
            assertTrue(e.message!!.contains("Target page, context or browser has been closed"))
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

            val bytes = readAllBytes(userPath)
            assertEquals("Hello world", String(bytes, UTF_8))
        }
    }

    @Test
    fun `check to be able to cancel pending downloads`() {
        browser.newPage(NewPageOptions { it.acceptDownloads = true }).use { pg ->
            pg.setContent("<a href='${httpServer.prefixWithDomain}/downloadWithDelay'>download</a>")
            val download = pg.waitForDownload { pg.click("a") }
            assertNotNull(download)
            download.cancel()
            assertEquals("canceled", download.failure())
        }
    }

    @Test
    fun `check to not fail explicitly to cancel a download even of that is already finished`() {
        browser.newPage(NewPageOptions { it.acceptDownloads = true }).use {
            it.setContent("<a href='${httpServer.prefixWithDomain}/download'>download</a>")
            val download = it.waitForDownload { it.click("a") }
            assertNotNull(download)

            val userPath = Files.createTempFile("download-", ".txt")
            download.saveAs(userPath)
            assertTrue(Files.exists(userPath))
            val bytes = readAllBytes(userPath)
            assertEquals("Hello world", String(bytes, UTF_8))
            download.cancel()
            assertNull(download.failure())
        }
    }

    @Test
    fun `check to support stream zero size read`() {
        browser.newPage(NewPageOptions { it.acceptDownloads = true }).use {
            it.setContent("<a href='${httpServer.prefixWithDomain}/download'>download</a>")
            val download = it.waitForDownload { it.click("a") }
            assertNotNull(download)

            val stream = download.createReadStream()
            assertNotNull(stream)
            val b = ByteArray(1)
            val read = stream.read(b, 0, 0)
            assertEquals(0, read)
        }
    }

    @Test
    fun `check to report download when navigation turns into download`() {
        browser.newPage(NewPageOptions { it.acceptDownloads = true }).use {
            val response = arrayListOf<IResponse?>(null)
            val error = arrayListOf<PlaywrightException?>(null)

            val download = it.waitForDownload {
                try {
                    response[0] = it.navigate("${httpServer.prefixWithDomain}/download")
                } catch (e: PlaywrightException) {
                    error[0] = e
                }
            }

            assertNotNull(download)
            assertEquals(it, download.page())
            assertEquals("${httpServer.prefixWithDomain}/download", download.url())
            val userPath = Files.createTempFile("download-", ".txt")
            download.saveAs(userPath)
            assertTrue(Files.exists(userPath))
            val bytes = readAllBytes(userPath)
            assertEquals("Hello world", String(bytes, UTF_8))

            when {
                isChromium() -> {
                    assertNotNull(error[0])
                    assertTrue(error[0]!!.message!!.contains("net::ERR_ABORTED"))
                    assertEquals("about:blank", it.url())
                }
                isWebkit() -> {
                    assertNotNull(error[0])
                    assertTrue(error[0]!!.message!!.contains("Download is starting"))
                    assertEquals("about:blank", it.url())
                }
                else -> {
                    assertNotNull(response[0])
                    assertEquals(200, response[0]!!.status())
                    assertEquals("${httpServer.prefixWithDomain}/download", it.url())
                }
            }
        }
    }
}