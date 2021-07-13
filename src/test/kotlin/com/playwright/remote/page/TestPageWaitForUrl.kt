package com.playwright.remote.page

import com.playwright.remote.base.BaseTest
import com.playwright.remote.core.enums.WaitUntilState.DOMCONTENTLOADED
import com.playwright.remote.core.enums.WaitUntilState.LOAD
import com.playwright.remote.core.exceptions.TimeoutException
import com.playwright.remote.engine.options.wait.WaitForURLOptions
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

class TestPageWaitForUrl : BaseTest() {

    @Test
    fun `check correct work`() {
        page.navigate(httpServer.emptyPage)
        page.evaluate("url => window.location.href = url", "${httpServer.prefixWithDomain}/grid.html")
        page.waitForURL("**/grid.html")
        assertEquals("${httpServer.prefixWithDomain}/grid.html", page.url())
    }

    @Test
    fun `check correct work with respect timeout`() {
        page.navigate(httpServer.emptyPage)
        try {
            page.waitForURL("**/frame.html", WaitForURLOptions { it.timeout = 2500.0 })
            fail("waitForURL should throw")
        } catch (e: TimeoutException) {
            assertTrue(e.message!!.contains("Timeout 2500 ms exceeded"))
        }
    }

    @Test
    fun `check correct work with dom content loaded and load events`() {
        page.navigate("${httpServer.prefixWithDomain}/page-with-one-style.html")
        page.waitForURL("**/page-with-one-style.html", WaitForURLOptions { it.waitUntil = DOMCONTENTLOADED })
        page.waitForURL("**/page-with-one-style.html", WaitForURLOptions { it.waitUntil = LOAD })
        assertEquals("${httpServer.prefixWithDomain}/page-with-one-style.html", page.url())
    }

    @Test
    fun `check correct work with clicking on anchor links`() {
        page.navigate(httpServer.emptyPage)
        page.setContent("<a href='#foobar'>foobar</a>")
        page.click("a")
        page.waitForURL("**/*#foobar")
        assertEquals("${httpServer.emptyPage}#foobar", page.url())
    }

    @Test
    fun `check correct work with history push state`() {
        page.navigate(httpServer.emptyPage)
        val content = """<a onclick='javascript:pushState()'>SPA</a>
            |<script>
            |   function pushState() { history.pushState({}, '', 'wow.html') }
            |</script>
        """.trimMargin()
        page.setContent(content)
        page.click("a")
        page.waitForURL("**/wow.html")
        assertEquals("${httpServer.prefixWithDomain}/wow.html", page.url())
    }

    @Test
    fun `check correct work with history replace state`() {
        page.navigate(httpServer.emptyPage)
        val content = """<a onclick='javascript:replaceState()'>SPA</a>
            |<script>
            |   function replaceState() { history.replaceState({}, '', '/replaced.html') }
            |</script>
        """.trimMargin()
        page.setContent(content)
        page.click("a")
        page.waitForURL("**/replaced.html")
        assertEquals("${httpServer.prefixWithDomain}/replaced.html", page.url())
    }

    @Test
    fun `check work with DOM history back and history forward`() {
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

        page.click("a#back")
        page.waitForURL("**/first.html")
        assertEquals("${httpServer.prefixWithDomain}/first.html", page.url())

        page.click("a#forward")
        page.waitForURL("**/second.html")
        assertEquals("${httpServer.prefixWithDomain}/second.html", page.url())
    }

    @Test
    fun `check correct work on frame`() {
        page.navigate("${httpServer.prefixWithDomain}/frames/one-frame.html")
        val frame = page.frames()[1]
        assertNotNull(frame)
        frame.evaluate("url => window.location.href = url", "${httpServer.prefixWithDomain}/grid.html")
        frame.waitForURL("**/grid.html")
        assertEquals("${httpServer.prefixWithDomain}/grid.html", frame.url())
    }
}