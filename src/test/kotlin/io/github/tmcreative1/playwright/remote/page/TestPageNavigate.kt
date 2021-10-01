package io.github.tmcreative1.playwright.remote.page

import io.github.tmcreative1.playwright.remote.base.BaseTest
import io.github.tmcreative1.playwright.remote.core.enums.WaitUntilState
import io.github.tmcreative1.playwright.remote.core.exceptions.PlaywrightException
import io.github.tmcreative1.playwright.remote.engine.options.NavigateOptions
import org.junit.jupiter.api.Test
import java.nio.file.Paths
import kotlin.test.*

class TestPageNavigate : BaseTest() {

    @Test
    fun `check correct work`() {
        page.navigate(httpServer.emptyPage)
        assertEquals(httpServer.emptyPage, page.url())
    }

    @Test
    fun `check correct work with file url`() {
        val fileUrl = Paths.get("src/test/resources/frames/two-frames.html").toUri().toString()
        page.navigate(fileUrl)
        assertEquals(fileUrl.lowercase(), page.url().lowercase())
        assertEquals(3, page.frames().size)
    }

    @Test
    fun `check to use http for no protocol`() {
        page.navigate(httpServer.emptyPage.substring("http://".length))
        assertEquals(httpServer.emptyPage, page.url())
    }

    @Test
    fun `check correct work of cross process`() {
        page.navigate(httpServer.emptyPage)
        assertEquals(httpServer.emptyPage, page.url())

        val url = "${httpServer.prefixWithIP}/empty.html"
        val response = page.navigate(url)
        assertEquals(url, page.url())
        assertNotNull(response)
        assertEquals(page.mainFrame(), response.frame())
        assertEquals(page.mainFrame(), response.request().frame())
        assertEquals(url, response.url())
    }

    @Test
    fun `check to capture iframe navigation request`() {
        page.navigate(httpServer.emptyPage)
        assertEquals(httpServer.emptyPage, page.url())
        val request = page.waitForRequest("${httpServer.prefixWithDomain}/frames/frame.html") {
            val response = page.navigate("${httpServer.prefixWithDomain}/frames/one-frame.html")
            assertEquals("${httpServer.prefixWithDomain}/frames/one-frame.html", page.url())
            assertNotNull(response)
            assertEquals(page.mainFrame(), response.frame())
            assertEquals("${httpServer.prefixWithDomain}/frames/one-frame.html", response.url())

            assertEquals(2, page.frames().size)
        }
        assertNotNull(request)
        assertEquals(page.frames()[1], request.frame())
    }

    @Test
    fun `check correct work with anchor navigation`() {
        page.navigate(httpServer.emptyPage)
        assertEquals(httpServer.emptyPage, page.url())

        page.navigate("${httpServer.emptyPage}#foo")
        assertEquals("${httpServer.emptyPage}#foo", page.url())

        page.navigate("${httpServer.emptyPage}#bar")
        assertEquals("${httpServer.emptyPage}#bar", page.url())
    }

    @Test
    fun `check correct work with redirects`() {
        httpServer.setRedirect("/redirect/1.html", "/redirect/2.html")
        httpServer.setRedirect("/redirect/2.html", "/empty.html")
        val response = page.navigate("${httpServer.prefixWithDomain}/redirect/1.html")
        assertNotNull(response)
        assertEquals(200, response.status())
        assertEquals(httpServer.emptyPage, page.url())
    }

    @Test
    fun `check to navigate to about blank`() {
        val response = page.navigate("about:blank")
        assertNull(response)
    }

    @Test
    fun `check to return response when page changes its url after load`() {
        val response = page.navigate("${httpServer.prefixWithDomain}/history-api.html")
        assertNotNull(response)
        assertEquals(200, response.status())
    }

    @Test
    fun `check correct work with sub-frames return 204`() {
        httpServer.setRoute("/frames/frame.html") {
            it.sendResponseHeaders(204, 0)
            it.responseBody.close()
        }
        val response = page.navigate("${httpServer.prefixWithDomain}/frames/one-frame.html")
        assertNotNull(response)
        assertEquals(200, response.status())
    }

    @Test
    fun `check correct work with sub-frames return 204 with dom content loaded`() {
        httpServer.setRoute("/frames/frame.html") {
            it.sendResponseHeaders(204, 0)
            it.responseBody.close()
        }
        val response = page.navigate(
            "${httpServer.prefixWithDomain}/frames/one-frame.html",
            NavigateOptions { it.waitUntil = WaitUntilState.DOMCONTENTLOADED.value })
        assertNotNull(response)
        assertEquals(200, response.status())
    }

    @Test
    fun `check to fail when server returns 204`() {
        httpServer.setRoute("/empty.html") {
            it.sendResponseHeaders(204, 0)
            it.responseBody.close()
        }

        try {
            page.navigate(httpServer.emptyPage)
            fail("navigate should throw")
        } catch (e: PlaywrightException) {
            when {
                isChromium() ->
                    assertTrue(e.message!!.contains("net::ERR_ABORTED"))
                isWebkit() ->
                    assertTrue(e.message!!.contains("Aborted: 204 No Content"))
                else ->
                    assertTrue(e.message!!.contains("NS_BINDING_ABORTED"))
            }
        }
    }

    @Test
    fun `check to navigate to empty page with dom content loaded`() {
        val response = page.navigate(
            httpServer.emptyPage,
            NavigateOptions { it.waitUntil = WaitUntilState.DOMCONTENTLOADED.value })
        assertNotNull(response)
        assertEquals(200, response.status())
    }

    @Test
    fun `check correct work when page calls history api in before unload`() {
        page.navigate(httpServer.emptyPage)
        page.evaluate("window.addEventListener('beforeunload', () => history.replaceState(null, 'initial', window.location.href), false);")
        val response = page.navigate("${httpServer.prefixWithDomain}/grid.html")
        assertNotNull(response)
        assertEquals(200, response.status())
    }

    @Test
    fun `check to capture cross process iframe navigation request`() {
        page.navigate(httpServer.emptyPage)
        assertEquals(httpServer.emptyPage, page.url())

        val request = page.waitForRequest("${httpServer.prefixWithIP}/frames/frame.html") {
            val response = page.navigate("${httpServer.prefixWithIP}/frames/one-frame.html")
            assertNotNull(response)
            assertEquals("${httpServer.prefixWithIP}/frames/one-frame.html", page.url())
            assertEquals(page.mainFrame(), response.frame())
            assertEquals("${httpServer.prefixWithIP}/frames/one-frame.html", response.url())

            assertEquals(2, page.frames().size)
        }
        assertNotNull(request)
        assertEquals(page.frames()[1], request.frame())
    }

    @Test
    fun `check to send referer`() {
        val request1 = httpServer.futureRequest("/grid.html")
        val request2 = httpServer.futureRequest("/images/digits/1.png")

        page.navigate("${httpServer.prefixWithDomain}/grid.html", NavigateOptions {
            it.referer = "http://google.com/"
        })
        assertEquals(listOf("http://google.com/"), request1.get().headers["referer"])
        assertEquals(listOf("${httpServer.prefixWithDomain}/grid.html"), request2.get().headers["referer"])
        assertEquals("${httpServer.prefixWithDomain}/grid.html", page.url())
    }
}