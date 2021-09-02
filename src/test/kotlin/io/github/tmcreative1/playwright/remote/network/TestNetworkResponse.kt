package io.github.tmcreative1.playwright.remote.network

import io.github.tmcreative1.playwright.remote.base.BaseTest
import io.github.tmcreative1.playwright.remote.core.exceptions.PlaywrightException
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import java.io.OutputStreamWriter
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.Semaphore
import kotlin.test.*

class TestNetworkResponse : BaseTest() {

    @Test
    fun `check correct work`() {
        httpServer.setRoute("/empty.html") {
            it.responseHeaders.add("foo", "bar")
            it.responseHeaders.add("BaZ", "bAz")
            it.sendResponseHeaders(200, 0)
            it.responseBody.close()
        }
        val response = page.navigate(httpServer.emptyPage)
        assertNotNull(response)
        assertEquals("bar", response.headers()["foo"])
        assertEquals("bAz", response.headers()["baz"])
        assertNull(response.headers()["BaZ"])
    }

    @Test
    fun `check to return text`() {
        val response = page.navigate("${httpServer.prefixWithDomain}/simple.json")
        assertNotNull(response)
        assertEquals("{\"foo\": \"bar\"}", response.text())
    }

    @Test
    fun `check to return uncompressed text`() {
        httpServer.enableGzip("/simple.json")
        val response = page.navigate("${httpServer.prefixWithDomain}/simple.json")
        assertNotNull(response)
        assertEquals("gzip", response.headers()["content-encoding"])
        assertEquals("{\"foo\": \"bar\"}", response.text())
    }

    @Test
    fun `check to throw error when requesting body of redirected response`() {
        httpServer.setRedirect("/foo.html", "/empty.html")
        val response = page.navigate("${httpServer.prefixWithDomain}/foo.html")
        assertNotNull(response)
        val redirectedFrom = response.request().redirectedFrom()
        assertNotNull(redirectedFrom)
        val redirected = redirectedFrom.response()
        assertNotNull(redirected)
        assertEquals(302, redirected.status())
        try {
            redirected.text()
            fail("text should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Response body is unavailable for redirect responses"))
        }
    }

    @Test
    fun `check to wait until response completes`() {
        page.navigate(httpServer.emptyPage)
        val responseWritten = Semaphore(0)
        val responseRead = Semaphore(0)
        httpServer.setRoute("/get") {
            it.responseHeaders.add("Content-Type", "text/plain; charset=utf-8")
            it.sendResponseHeaders(200, 0)
            OutputStreamWriter(it.responseBody).use { wr ->
                wr.write("Hello, ")
                wr.flush()
                responseWritten.release()
                responseRead.acquire()
                wr.write("Wor")
                wr.flush()
                wr.write("ld!")
            }
            responseWritten.release()
        }
        val requestFinished = arrayListOf(false)
        page.onRequestFinished { requestFinished[0] = it.url().contains("/get") }

        val pageResponse = page.waitForResponse("**") { page.evaluate("() => fetch('./get', { method: 'GET'})") }
        assertNotNull(pageResponse)
        responseWritten.acquire()
        assertEquals(200, pageResponse.status())
        assertFalse(requestFinished[0])
        responseRead.release()
        responseWritten.acquire()
        assertEquals("Hello, World!", pageResponse.text())
        assertTrue(requestFinished[0])
    }

    @Test
    fun `check to return body`() {
        val response = page.navigate("${httpServer.prefixWithDomain}/playwright.png")
        val expected = Files.readAllBytes(Paths.get("src/test/resources/playwright.png"))
        assertNotNull(response)
        assertArrayEquals(expected, response.body())
    }

    @Test
    fun `check to return body with compression`() {
        httpServer.enableGzip("/playwright.png")
        val response = page.navigate("${httpServer.prefixWithDomain}/playwright.png")
        val expected = Files.readAllBytes(Paths.get("src/test/resources/playwright.png"))
        assertNotNull(response)
        assertArrayEquals(expected, response.body())
    }

    @Test
    fun `check to return status text`() {
        httpServer.setRoute("/cool") {
            it.sendResponseHeaders(200, 0)
            it.responseBody.close()
        }
        val response = page.navigate("${httpServer.prefixWithDomain}/cool")
        assertNotNull(response)
        assertEquals("OK", response.statusText())
    }
}