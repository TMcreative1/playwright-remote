package io.github.tmcreative1.playwright.remote.page

import io.github.tmcreative1.playwright.remote.base.BaseTest
import org.junit.jupiter.api.Test
import java.io.OutputStreamWriter
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestPageNetworkSizes : BaseTest() {

    @Test
    fun `check to set body size and headers size`() {
        page.navigate(httpServer.emptyPage)
        val request = page.waitForRequest("**/*") {
            page.evaluate("() => fetch('./get', { method: 'POST', body: '12345' }).then(r => r.text())")
        }
        assertNotNull(request)
        val sizes = request.sizes()
        assertEquals(5, sizes.requestBodySize)
        assertTrue(sizes.requestHeadersSize >= 250)
    }

    @Test
    fun `check to set body size to zero if there was no body`() {
        page.navigate(httpServer.emptyPage)
        val request = page.waitForRequest("**/*") {
            page.evaluate("() => fetch('./get').then(r => r.text())")
        }
        assertNotNull(request)
        val sizes = request.sizes()
        assertEquals(0, sizes.requestBodySize)
        assertTrue(sizes.requestHeadersSize >= 200)
    }

    @Test
    fun `check to set body size headers and transfer size`() {
        httpServer.setRoute("/get") {
            it.responseHeaders.add("Content-Type", "text/plain; charset=utf-8")
            it.sendResponseHeaders(200, 6)
            OutputStreamWriter(it.responseBody).use { wr ->
                wr.write("abc134")
            }
        }
        val request = httpServer.futureRequest("/get")
        page.navigate(httpServer.emptyPage)
        val response = page.waitForResponse("**/*") {
            page.evaluate("async () => fetch('./get').then(r => r.text())")
        }
        request.get()
        assertNotNull(response)
        val sizes = response.request().sizes()
        assertEquals(6, sizes.responseBodySize)
        assertTrue(sizes.responseHeadersSize > 0)
    }

    @Test
    fun `check to set body size to zero when there was no response body`() {
        val response = page.navigate("${httpServer.prefixWithDomain}/blank.html")
        assertNotNull(response)
        val sizes = response.request().sizes()
        assertEquals(0, sizes.responseBodySize)
        assertTrue(sizes.responseHeadersSize > 0)
    }

    @Test
    fun `check to have the correct response body size`() {
        val response = page.navigate("${httpServer.prefixWithDomain}/simple-zip.json")
        assertNotNull(response)
        val sizes = response.request().sizes()
        assertEquals(Files.size(Paths.get("src/test/resources/simple-zip.json")).toInt(), sizes.responseBodySize)
    }
}