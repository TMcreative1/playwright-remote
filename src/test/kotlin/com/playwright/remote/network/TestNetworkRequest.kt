package com.playwright.remote.network

import com.playwright.remote.base.BaseTest
import com.playwright.remote.engine.route.request.api.IRequest
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.Test
import java.io.OutputStreamWriter
import kotlin.test.*

class TestNetworkRequest : BaseTest() {

    @Test
    fun `check correct work of main frame navigation request`() {
        val requests = arrayListOf<IRequest>()
        page.onRequest { requests.add(it) }
        page.navigate(httpServer.emptyPage)
        assertEquals(1, requests.size)
        assertEquals(page.mainFrame(), requests[0].frame())
    }

    @Test
    fun `check correct work of sub frame navigation request`() {
        page.navigate(httpServer.emptyPage)
        val requests = arrayListOf<IRequest>()
        page.onRequest { requests.add(it) }
        attachFrame(page, "frame1", httpServer.emptyPage)
        assertEquals(1, requests.size)
        assertEquals(page.frames()[1], requests[0].frame())
    }

    @Test
    fun `check correct work of fetch requests`() {
        page.navigate(httpServer.emptyPage)
        val requests = arrayListOf<IRequest>()
        page.onRequest { requests.add(it) }
        page.evaluate("() => fetch('/images/digits/1.png')")
        assertEquals(1, requests.size)
        assertEquals(page.mainFrame(), requests[0].frame())
    }

    @Test
    fun `check correct work of redirect`() {
        httpServer.setRedirect("/foo.html", "/empty.html")
        val requests = arrayListOf<IRequest>()
        page.onRequest { requests.add(it) }
        page.navigate("${httpServer.prefixWithDomain}/foo.html")

        assertEquals(2, requests.size)
        assertEquals("${httpServer.prefixWithDomain}/foo.html", requests[0].url())
        assertEquals("${httpServer.prefixWithDomain}/empty.html", requests[1].url())
    }

    @Test
    fun `check to not work for a redirect and interception`() {
        httpServer.setRedirect("/foo.html", "/empty.html")
        val requests = arrayListOf<IRequest>()
        page.route("**") {
            requests.add(it.request())
            it.resume()
        }
        page.navigate("${httpServer.prefixWithDomain}/foo.html")

        assertEquals("${httpServer.prefixWithDomain}/empty.html", page.url())
        assertEquals(1, requests.size)
        assertEquals("${httpServer.prefixWithDomain}/foo.html", requests[0].url())
    }

    @Test
    fun `check to return headers`() {
        val response = page.navigate(httpServer.emptyPage)
        val expectedHeader = when {
            isChromium() -> "Chrome"
            isFirefox() -> "Firefox"
            isWebkit() -> "WebKit"
            else -> ""
        }
        assertNotNull(response)
        assertTrue(response.request().headers()["user-agent"]!!.contains(expectedHeader))
    }

    @Test
    fun `check to get the same headers as the server`() {
        Assumptions.assumeFalse((isWindows() && isWebkit()) || isChromium())
        val serverRequest = httpServer.futureRequest("/empty.html")
        httpServer.setRoute("/empty.html") {
            it.sendResponseHeaders(200, 0)
            OutputStreamWriter(it.responseBody).use { wr ->
                wr.write("done")
            }
        }
        val response = page.navigate(httpServer.emptyPage)
        val expectedHeaders: Map<String, String> =
            serverRequest.get().headers.entries.associateBy({ it.key.lowercase() }, { it.value[0] })
        assertNotNull(response)
        assertEquals(expectedHeaders, response.request().headers())
    }

    @Test
    fun `check to get the same headers as the server corp`() {
        Assumptions.assumeFalse(isWindows() && isWebkit())
        page.navigate(httpServer.emptyPage)
        val serverRequest = httpServer.futureRequest("/something")
        httpServer.setRoute("/something") {
            it.responseHeaders.add("Access-Control-Allow-Origin", "*")
            it.sendResponseHeaders(200, 0)
            OutputStreamWriter(it.responseBody).use { wr ->
                wr.write("done")
            }
        }
        val response = page.waitForResponse("**") {
            val jsScript = """async url => {
                |   const data = await fetch(url);
                |   return data.text();
                |}
            """.trimMargin()
            val text = page.evaluate(jsScript, "${httpServer.prefixWithDomain}/something")
            assertEquals("done", text)
        }
        val expectedHeaders =
            serverRequest.get().headers.entries.associateBy({ it.key.lowercase() }, { it.value[0] })
        assertNotNull(response)
        assertEquals(expectedHeaders, response.request().headers())
    }

    @Test
    fun `check to return post data`() {
        page.navigate(httpServer.emptyPage)
        httpServer.setRoute("/post") {
            it.sendResponseHeaders(200, 0)
            it.responseBody.close()
        }
        val request = arrayListOf<IRequest?>(null)
        page.onRequest { request[0] = it }
        page.evaluate("() => fetch('./post', { method: 'POST', body: JSON.stringify({foo: 'bar'})})")
        assertNotNull(request[0])
        assertEquals("{\"foo\":\"bar\"}", request[0]!!.postData())
    }

    @Test
    fun `check correct work with binary post data`() {
        page.navigate(httpServer.emptyPage)
        httpServer.setRoute("/post") {
            it.sendResponseHeaders(200, 0)
            it.responseBody.close()
        }
        val request = arrayListOf<IRequest?>(null)
        page.onRequest { request[0] = it }
        val jsScript = """async () => {
            |   await fetch('./post', { method: 'POST', body: new Uint8Array(Array.from(Array(256).keys())) });
            |}
        """.trimMargin()
        page.evaluate(jsScript)
        assertNotNull(request[0])
        val buffer = request[0]!!.postDataBuffer()
        assertNotNull(buffer)
        assertEquals(256, buffer.size)
        for (index in 0..255) {
            assertEquals(index.toByte(), buffer[index])
        }
    }

    @Test
    fun `check correct work with binary post data and interception`() {
        page.navigate(httpServer.emptyPage)
        httpServer.setRoute("/post") {
            it.sendResponseHeaders(200, 0)
            it.responseBody.close()
        }
        val request = arrayListOf<IRequest?>(null)
        page.onRequest { request[0] = it }
        page.route("/post") {
            it.resume()
        }
        val jsScript = """async () => {
            |   await fetch('./post', { method: 'POST', body: new Uint8Array(Array.from(Array(256).keys())) });
            |}
        """.trimMargin()
        page.evaluate(jsScript)
        assertNotNull(request[0])
        val buffer = request[0]!!.postDataBuffer()
        assertNotNull(buffer)
        assertEquals(256, buffer.size)
        for (index in 0..255) {
            assertEquals(index.toByte(), buffer[index])
        }
    }

    @Test
    fun `check to be undefined when there is not post data`() {
        val response = page.navigate(httpServer.emptyPage)
        assertNotNull(response)
        assertNull(response.request().postData())
    }

    @Test
    fun `check to return event source`() {
        httpServer.setRoute("/sse") {
            it.responseHeaders.add("Content-Type", "text/event-stream")
            it.responseHeaders.add("Connection", "keep-alive")
            it.responseHeaders.add("Cache-Control", "no-cache")
            it.sendResponseHeaders(200, 0)
            OutputStreamWriter(it.responseBody).use { wr ->
                wr.write("data: {\"foo\":\"bar\"}\n\n")
            }
        }
        page.navigate(httpServer.emptyPage)
        val requests = arrayListOf<IRequest>()
        page.onRequest { requests.add(it) }
        val jsScript = """() => {
            |   const eventSource = new EventSource('/sse');
            |   return new Promise(resolve => {
            |       eventSource.onmessage = e => resolve(JSON.parse(e.data));
            |   });
            |}
        """.trimMargin()
        val result = page.evaluate(jsScript)
        assertEquals(mapOf("foo" to "bar"), result)
        assertEquals("eventsource", requests[0].resourceType())
    }

    @Test
    fun `check to return navigation bit`() {
        val requests = mutableMapOf<String, IRequest>()
        page.onRequest {
            var name = it.url()
            val lastSlash = name.lastIndexOf('/')
            if (lastSlash != -1) {
                name = name.substring(lastSlash + 1)
            }
            requests[name] = it
        }
        httpServer.setRedirect("/rrredirect", "/frames/one-frame.html")
        page.navigate("${httpServer.prefixWithDomain}/rrredirect")

        assertTrue(requests["rrredirect"]!!.isNavigationRequest())
        assertTrue(requests["one-frame.html"]!!.isNavigationRequest())
        assertTrue(requests["frame.html"]!!.isNavigationRequest())
        assertFalse(requests["frame.js"]!!.isNavigationRequest())
        assertFalse(requests["frame.css"]!!.isNavigationRequest())
    }

    @Test
    fun `check to return navigation bit when navigating to image`() {
        val requests = arrayListOf<IRequest>()
        page.onRequest { requests.add(it) }
        page.navigate("${httpServer.prefixWithDomain}/playwright.png")
        assertTrue(requests[0].isNavigationRequest())
    }
}