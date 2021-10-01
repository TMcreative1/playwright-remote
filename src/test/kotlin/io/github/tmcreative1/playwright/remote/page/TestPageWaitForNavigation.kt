package io.github.tmcreative1.playwright.remote.page

import io.github.tmcreative1.playwright.remote.base.BaseTest
import io.github.tmcreative1.playwright.remote.core.enums.WaitUntilState
import io.github.tmcreative1.playwright.remote.core.exceptions.PlaywrightException
import io.github.tmcreative1.playwright.remote.core.exceptions.TimeoutException
import io.github.tmcreative1.playwright.remote.engine.options.wait.WaitForNavigationOptions
import org.junit.jupiter.api.Test
import java.net.MalformedURLException
import java.net.URL
import java.util.regex.Pattern
import kotlin.test.*

class TestPageWaitForNavigation : BaseTest() {

    @Test
    fun `check correct work of wait for navigation`() {
        page.navigate(httpServer.emptyPage)
        val response = page.waitForNavigation {
            page.evaluate("url => window.location.href = url", "${httpServer.prefixWithDomain}/grid.html")
        }
        assertNotNull(response)
        assertTrue(response.ok())
        assertTrue(response.url().contains("grid.html"))
    }

    @Test
    fun `check correct work of wait for navigation with timeout`() {
        try {
            page.waitForNavigation(WaitForNavigationOptions {
                it.url = "**/frame.html"
                it.timeout = 5000.0
            }) { page.navigate(httpServer.emptyPage) }
            fail("waitForNavigation should throw")
        } catch (e: TimeoutException) {
            assertTrue(e.message!!.contains("Timeout 5000 ms exceeded"))
        }
    }

    @Test
    fun `check correct work with clicking on anchor links`() {
        page.navigate(httpServer.emptyPage)
        page.setContent("<a href='#foobar'>foobar</a>")
        val response = page.waitForNavigation { page.click("a") }
        assertNull(response)
        assertEquals("${httpServer.emptyPage}#foobar", page.url())
    }

    @Test
    fun `check correct work with clicking on links which do not commit navigation`() {
        page.navigate(httpServer.emptyPage)
        page.setContent("<a href='${httpsServer.emptyPage}'>foobar</a>")
        try {
            page.waitForNavigation { page.click("a") }
            fail("waitForNavigation should throw")
        } catch (e: PlaywrightException) {
            val possibleErrorMessages = when {
                isChromium() -> {
                    if (isMac()) {
                        listOf("net::ERR_CERT_INVALID")
                    } else {
                        listOf("net::ERR_CERT_AUTHORITY_INVALID")
                    }
                }
                isWebkit() -> {
                    when {
                        isMac() -> listOf("The certificate for this server is invalid")
                        isWindows() -> listOf("SSL peer certificate or SSH remote key was not OK", "SSL connect error")
                        else -> listOf("Unacceptable TLS certificate", "Server required TLS certificate")
                    }
                }
                else -> listOf("SSL_ERROR_UNKNOWN")
            }
            assertTrue(
                possibleErrorMessages.stream().anyMatch { e.message!!.contains(it) },
                "Unexpected exception: '${e.message}' check message(s): ${possibleErrorMessages.joinToString(separator = ",")}"
            )
        }
    }

    @Test
    fun `check correct with history push state`() {
        page.navigate(httpServer.emptyPage)
        val content = """<a onclick='javascript:pushState()'>SPA</a>
            |<script>
            |   function pushState() { history.pushState({}, '', 'wow.html') }
            |</script>
        """.trimMargin()
        page.setContent(content)
        val response = page.waitForNavigation { page.click("a") }
        assertNull(response)
        assertEquals("${httpServer.prefixWithDomain}/wow.html", page.url())
    }

    @Test
    fun `check correct work with replace state`() {
        page.navigate(httpServer.emptyPage)
        val content = """<a onclick='javascript:replaceState()'>SPA</a>
            |<script>
            |   function replaceState() { history.replaceState({}, '', '/replaced.html') }
            |</script>
        """.trimMargin()
        page.setContent(content)
        val response = page.waitForNavigation { page.click("a") }
        assertNull(response)
        assertEquals("${httpServer.prefixWithDomain}/replaced.html", page.url())
    }

    @Test
    fun `check correct work with DOM history back and history forward`() {
        page.navigate(httpServer.emptyPage)
        val content = """<a id=back onclick='javascript:goBack()'>back</a>
            |<a id=forward onclick='javascript:goForward()'>forward</a>
            |<script>
            |   function goBack() { history.back(); }
            |   function goForward() { history.forward(); }
            |   history.pushState({}, '', '/first.html');
            |   history.pushState({}, '', '/second.html');
            |</script>
        """.trimMargin()
        page.setContent(content)
        assertEquals("${httpServer.prefixWithDomain}/second.html", page.url())

        val backResponse = page.waitForNavigation { page.click("a#back") }
        assertNull(backResponse)
        assertEquals("${httpServer.prefixWithDomain}/first.html", page.url())

        val forwardResponse = page.waitForNavigation { page.click("a#forward") }
        page.click("a#forward")
        assertNull(forwardResponse)
        assertEquals("${httpServer.prefixWithDomain}/second.html", page.url())
    }

    @Test
    fun `check correct work when subframe issues window stop`() {
        httpServer.setRoute("/styles/frame.css") {}
        val frameWindowStopCalled = arrayListOf(false)
        page.onFrameAttached { frameAttach ->
            page.onFrameNavigated { frameNavigate ->
                if (frameAttach == frameNavigate) {
                    frameAttach.evaluate("window.stop()")
                    frameWindowStopCalled[0] = true
                }
            }
        }
        page.navigate("${httpServer.prefixWithDomain}/frames/one-frame.html")
        assertTrue(frameWindowStopCalled[0])
    }

    @Test
    fun `check correct work with url match`() {
        page.navigate(httpServer.emptyPage)

        val response1 = page.waitForNavigation(WaitForNavigationOptions {
            it.url = "**/page-with-one-style.html"
        }) { page.navigate("${httpServer.prefixWithDomain}/page-with-one-style.html") }
        assertNotNull(response1)
        assertEquals("${httpServer.prefixWithDomain}/page-with-one-style.html", response1.url())

        val response2 = page.waitForNavigation(WaitForNavigationOptions { it.url = Pattern.compile("frame.html$") })
        { page.navigate("${httpServer.prefixWithDomain}/frame.html") }
        assertNotNull(response2)
        assertEquals("${httpServer.prefixWithDomain}/frame.html", response2.url())

        val predicate: (String) -> Boolean = { url ->
            try {
                URL(url).query.contains("foo=bar")
            } catch (e: MalformedURLException) {
                throw RuntimeException(e)
            }
        }

        val response3 = page.waitForNavigation(WaitForNavigationOptions { it.url = predicate })
        { page.navigate("${httpServer.prefixWithDomain}/frame.html?foo=bar") }
        assertNotNull(response3)
        assertEquals("${httpServer.prefixWithDomain}/frame.html?foo=bar", response3.url())
    }

    @Test
    fun `check correct work with url match for same document navigations`() {
        page.navigate(httpServer.emptyPage)
        val response = page.waitForNavigation(WaitForNavigationOptions { it.url = "**/third.html" })
        {
            page.evaluate(
                """() => {
                |   history.pushState({}, '', '/first.html');
                |}
            """.trimMargin()
            )
            page.evaluate(
                """() => {
                |   history.pushState({}, '', '/second.html');
                |}
            """.trimMargin()
            )
            page.evaluate(
                """() => {
                |   history.pushState({}, '', '/third.html');
                |}
            """.trimMargin()
            )
        }
        assertNull(response)
    }

    @Test
    fun `check correct work for cross process navigations`() {
        page.navigate(httpServer.emptyPage)
        val url = "${httpServer.prefixWithIP}/empty.html"
        val response =
            page.waitForNavigation(WaitForNavigationOptions { it.waitUntil = WaitUntilState.DOMCONTENTLOADED })
            { page.navigate(url) }
        assertNotNull(response)
        assertEquals(url, response.url())
        assertEquals(url, page.url())
        assertEquals(url, page.evaluate("document.location.href"))
    }

    @Test
    fun `check correct work on frame`() {
        page.navigate("${httpServer.prefixWithDomain}/frames/one-frame.html")
        val frame = page.frames()[1]
        val response = frame.waitForNavigation {
            frame.evaluate(
                "url => window.location.href = url",
                "${httpServer.prefixWithDomain}/grid.html"
            )
        }
        assertNotNull(response)
        assertTrue(response.ok())
        assertTrue(response.url().contains("grid.html"))
        assertEquals(frame, response.frame())
        assertTrue(page.url().contains("/frames/one-frame.html"))
    }

    @Test
    fun `check to fail when frame detaches`() {
        page.navigate("${httpServer.prefixWithDomain}/frames/one-frame.html")
        val frame = page.frames()[1]
        httpServer.setRoute("/empty.html") {}
        try {
            frame.waitForNavigation {
                val jsScript = """() => {
                    |   frames[0].location.href = '/empty.html';
                    |   setTimeout(() => document.querySelector('iframe').remove());
                    |}
                """.trimMargin()
                page.evaluate(jsScript)
            }
            fail("waitForNavigation should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("frame was detached"))
        }
    }

    @Test
    fun `check to throw on invalid url matcher type in page`() {
        try {
            page.waitForNavigation(WaitForNavigationOptions { it.url = Any() }) {}
            fail("waitForNavigation should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Url must be String, Pattern or Predicate"))
        }
    }

    @Test
    fun `check to throw on invalid url mathcer type in frame`() {
        page.navigate("${httpServer.prefixWithDomain}/frames/one-frame.html")
        val frame = page.frames()[1]
        try {
            frame.waitForNavigation(WaitForNavigationOptions { it.url = Any() }) {}
            fail("waitForNavigation should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Url must be String, Pattern or Predicate"))
        }
    }
}