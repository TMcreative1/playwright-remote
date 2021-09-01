package com.playwright.remote.browser.context

import com.playwright.remote.base.BaseTest
import com.playwright.remote.core.exceptions.PlaywrightException
import com.playwright.remote.engine.options.NewContextOptions
import com.playwright.remote.engine.options.ViewportSize
import com.playwright.remote.engine.page.api.IPage
import org.junit.jupiter.api.Test
import java.io.OutputStreamWriter
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

class TestBrowserContextBasic : BaseTest() {
    @Test
    fun `check to create a new context`() {
        assertEquals(1, browser.contexts().size)
        val context = browser.newContext()
        assertEquals(2, browser.contexts().size)
        assertTrue(browser.contexts().indexOf(context) != -1)
        assertEquals(context.browser(), browser)
        context.close()
        assertEquals(1, browser.contexts().size)
        assertEquals(context.browser(), browser)
    }

    @Test
    fun `check window to open should use parent tab context`() {
        val context = browser.newContext()
        val pg = context.newPage()
        pg.navigate(httpServer.emptyPage)
        val popup = pg.waitForPopup { pg.evaluate("url => window.open(url)", httpServer.emptyPage) }
        assertNotNull(popup)
        assertEquals(context, popup.context())
        context.close()
    }

    @Test
    fun `check to isolate local storage and cookies`() {
        val context1 = browser.newContext()
        val context2 = browser.newContext()
        assertEquals(0, context1.pages().size)
        assertEquals(0, context2.pages().size)

        val pg1 = context1.newPage()
        pg1.navigate(httpServer.emptyPage)
        var jsScript = """() => {
            |   localStorage.setItem('name', 'page1');
            |   document.cookie = 'name=page1';
            |}
        """.trimMargin()
        pg1.evaluate(jsScript)

        assertEquals(1, context1.pages().size)
        assertEquals(0, context2.pages().size)

        val pg2 = context2.newPage()
        pg2.navigate(httpServer.emptyPage)
        jsScript = """() => {
            |   localStorage.setItem('name', 'page2');
            |   document.cookie = 'name=page2';
            |}
        """.trimMargin()
        pg2.evaluate(jsScript)

        assertEquals(1, context1.pages().size)
        assertEquals(1, context2.pages().size)
        assertEquals(pg1, context1.pages()[0])
        assertEquals(pg2, context2.pages()[0])


        assertEquals("page1", pg1.evaluate("() => localStorage.getItem('name')"))
        assertEquals("name=page1", pg1.evaluate("() => document.cookie"))
        assertEquals("page2", pg2.evaluate("() => localStorage.getItem('name')"))
        assertEquals("name=page2", pg2.evaluate("() => document.cookie"))

        context1.close()
        context2.close()

        assertEquals(1, browser.contexts().size)
    }

    @Test
    fun `check to propagate default viewport to the page`() {
        browser.newContext(NewContextOptions {
            it.viewportSize = ViewportSize { view ->
                view.width = 456
                view.height = 789
            }
        }).use {
            val pg = it.newPage()
            verifyViewport(pg, 456, 789)
        }
    }

    @Test
    fun `check to respect device scale factor`() {
        browser.newContext(NewContextOptions {
            it.deviceScaleFactor = 3.0
        }).use {
            val pg = it.newPage()
            assertEquals(3, pg.evaluate("window.devicePixelRatio"))
        }
    }

    @Test
    fun `check to not allow device scale factor with null viewport`() {
        try {
            browser.newContext(NewContextOptions {
                it.deviceScaleFactor = 1.0
                it.viewportSize = null
            })
            fail("newContext should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("\"deviceScaleFactor\" option is not supported with null \"viewport\""))
        }
    }

    @Test
    fun `check to not allow is mobile with null viewport`() {
        try {
            browser.newContext(NewContextOptions {
                it.viewportSize = null
                it.isMobile = true
            })
            fail("newContext should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("\"isMobile\" option is not supported with null \"viewport\""))
        }
    }

    @Test
    fun `check to close empty context`() {
        val context = browser.newContext()
        context.close()
    }

    @Test
    fun `check to abort future event when context closed`() {
        val context = browser.newContext()
        try {
            context.waitForPage { context.close() }
            fail("waitForPage should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Context closed"))
        }
    }

    @Test
    fun `check correct work to call close twice`() {
        val context = browser.newContext()
        context.close()
        context.close()
        context.close()
    }

    @Test
    fun `check to not report frameless pages on error`() {
        val context = browser.newContext()
        val pg = context.newPage()
        val url = httpServer.emptyPage
        httpServer.setRoute("/empty.html") {
            it.sendResponseHeaders(200, 0)
            OutputStreamWriter(it.responseBody).use { wr ->
                wr.write("<a href='$url' target='_blank'>Click me</a>")
            }
        }
        val popup = arrayListOf<IPage?>(null)
        context.onPage { popup[0] = it }
        pg.navigate(httpServer.emptyPage)
        pg.click("'Click me'")
        context.close()
        if (popup[0] != null) {
            assertTrue(popup[0]!!.isClosed())
            assertNotNull(popup[0]!!.mainFrame())
        }
    }

    @Test
    fun `check to return all of the pages`() {
        val context = browser.newContext()
        val pg = context.newPage()
        val second = context.newPage()
        val allPages = context.pages()
        assertEquals(2, allPages.size)
        assertTrue(allPages.contains(pg))
        assertTrue(allPages.contains(second))
        context.close()
    }

    @Test
    fun `check to close all belonging pages once closing context`() {
        val context = browser.newContext()
        context.newPage()
        assertEquals(1, context.pages().size)
        context.close()
        assertEquals(0, context.pages().size)
    }

    @Test
    fun `check to disable javascript`() {
        browser.newContext().use {
            val pg = it.newPage()
            pg.navigate("data:text/html, <script>var something = 'forbidden'</script>")
            try {
                page.evaluate("something")
                fail("evaluate should throw")
            } catch (e: PlaywrightException) {
                if (isWebkit()) {
                    assertTrue(e.message!!.contains("Can't find variable: something"))
                } else {
                    assertTrue(e.message!!.contains("something is not defined"))
                }
            }
        }
        browser.newContext().use {
            val pg = it.newPage()
            pg.navigate("data:text/html, <script>var something = 'forbidden'</script>")
            assertEquals("forbidden", pg.evaluate("something"))
        }
    }

    @Test
    fun `check to be able to navigate after disabling javascript`() {
        browser.newContext(NewContextOptions { it.javaScriptEnabled = false }).use {
            val pg = it.newPage()
            pg.navigate(httpServer.emptyPage)
        }
    }

    @Test
    fun `check correct work with offline option`() {
        browser.newContext(NewContextOptions { it.offline = true }).use {
            val pg = it.newPage()
            try {
                pg.navigate(httpServer.emptyPage)
                fail("navigate should throw")
            } catch (e: PlaywrightException) {
                when {
                    isWebkit() -> assertTrue(e.message!!.contains("WebKit encountered an internal error"))
                    isChromium() -> assertTrue(e.message!!.contains("net::ERR_INTERNET_DISCONNECTED"))
                    isFirefox() -> assertTrue(e.message!!.contains("NS_ERROR_FAILURE"))
                }
            }
            it.setOffline(false)
            val response = pg.navigate(httpServer.emptyPage)
            assertNotNull(response)
            assertEquals(200, response.status())
        }
    }

    @Test
    fun `check to emulate navigator online`() {
        browser.newContext().use {
            val pg = it.newPage()
            assertEquals(true, pg.evaluate("() => window.navigator.onLine"))
            it.setOffline(true)
            assertEquals(false, pg.evaluate("() => window.navigator.onLine"))
            it.setOffline(false)
            assertEquals(true, pg.evaluate("() => window.navigator.onLine"))
        }
    }
}