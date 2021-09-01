package io.github.tmcreative1.playwright.remote.page


import io.github.tmcreative1.playwright.remote.base.BaseTest
import io.github.tmcreative1.playwright.remote.core.exceptions.PlaywrightException
import io.github.tmcreative1.playwright.remote.engine.console.api.IConsoleMessage
import io.github.tmcreative1.playwright.remote.engine.frame.impl.Frame
import io.github.tmcreative1.playwright.remote.engine.options.CloseOptions
import io.github.tmcreative1.playwright.remote.engine.options.NewPageOptions
import io.github.tmcreative1.playwright.remote.engine.options.ScreenshotOptions

import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.util.regex.Pattern
import kotlin.io.path.Path
import kotlin.test.*

class TestPageBasic : BaseTest() {

    private fun createPageForHttps(
        options: NewPageOptions? = NewPageOptions { it.ignoreHTTPSErrors = true }
    ) = browser.newPage(options)

    @Test
    fun `check all promises are rejected after close page via browser`() {
        val page = createPageForHttps()
        page.close()
        try {
            page.evaluate("() => new Promise(r => {})")
            fail("evaluate should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Target page, context or browser has been closed"))
        }
    }

    @Test
    fun `check all promises are rejected after close page via browser context`() {
        page.close()
        try {
            page.evaluate("() => new Promise(r => {})")
            fail("evaluate should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Protocol error"))
        }
    }

    @Test
    fun `closed page should not be visible in context`() {
        assertTrue(browserContext.pages().contains(page))
        page.close()
        assertFalse(browserContext.pages().contains(page))
    }

    @Test
    fun `check run beforeunload if asked for`() {
        page.navigate("${httpServer.prefixWithDomain}/beforeunload.html")

        page.click("body")
        val didShowDialog = arrayOf(false)
        page.onDialog {
            didShowDialog[0] = true
            assertEquals("beforeunload", it.type())
            assertEquals("", it.defaultValue())
            when {
                isChromium() -> {
                    assertEquals("", it.message())
                }
                isWebkit() -> {
                    assertEquals("Leave?", it.message())
                }
                isFirefox() -> {
                    assertEquals(
                        "This page is asking you to confirm that you want to leave - data you have entered may not be saved.",
                        it.message()
                    )
                }
                else -> {
                    assertEquals(
                        "This page is asking you to confirm that you want to leave — information you’ve entered may not be saved.",
                        it.message()
                    )
                }
            }
            it.accept()
        }
        page.close(CloseOptions { it.runBeforeUnload = true })
        for (index in 0..300) {
            if (didShowDialog[0]) {
                break
            }
            page.waitForTimeout(100.0)
        }
        assertTrue(didShowDialog[0])
    }

    @Test
    fun `should not run beforeunload by default`() {
        page.navigate("${httpServer.prefixWithDomain}/beforeunload.html")
        page.click("body")
        val didShowDialog = arrayOf(false)
        page.onDialog { didShowDialog[0] = true }
        page.close()
        assertFalse(didShowDialog[0])
    }

    @Test
    fun `should set the page close state`() {
        assertFalse(page.isClosed())
        page.close()
        assertTrue(page.isClosed())
    }

    @Test
    fun `should terminate network waiters`() {
        try {
            page.waitForResponse("**") {
                try {
                    page.waitForRequest(httpServer.emptyPage) { page.close() }
                    fail("waitForRequest() should throw")
                } catch (e: PlaywrightException) {
                    assertTrue(e.message!!.contains("Page closed"))
                    assertFalse(e.message!!.contains("Timeout"))
                }
            }
            fail("waitForResponse() should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Page closed"))
            assertFalse(e.message!!.contains("Timeout"))
        }
    }

    @Test
    fun `check that close should be callable several times`() {
        try {
            page.close()
            page.close()
            page.close()
        } catch (e: PlaywrightException) {
            fail("Error should not be thrown")
        }
    }

    @Test
    fun `check that page url should include hashes`() {
        var expectedUrl = "${httpServer.emptyPage}#hash"
        page.navigate(expectedUrl)
        assertEquals(expectedUrl, page.url())
        page.evaluate("() => { window.location.hash = 'dynamic'; }")
        expectedUrl = "${httpServer.emptyPage}#dynamic"
        assertEquals(expectedUrl, page.url())
    }

    @Test
    fun `check navigate method in browser`() {
        val navigatedUrl = httpServer.emptyPage
        val page = createPageForHttps()
        assertEquals("about:blank", page.url())
        val response = page.navigate(navigatedUrl)
        assert(response != null)
        assertEquals(navigatedUrl, response?.url())
    }

    @Test
    fun `check title method should return title of page`() {
        val navigatedUrl = httpServer.emptyPage
        page.navigate(navigatedUrl)
        assertEquals("Empty Page", page.title())
    }

    @Test
    fun `check page close should work with page close`() {
        try {
            page.waitForClose { page.close() }
        } catch (e: PlaywrightException) {
            fail("Error should not be thrown")
        }
    }

    @Test
    fun `check page frame should respect name`() {
        page.setContent("<iframe name=target></iframe>")
        assertNull(page.frame("bogus"))
        val frame = page.frame("target")
        assertNotNull(frame)
        assertEquals((page.mainFrame() as Frame).childFrames.toList()[0], frame)
    }

    @Test
    fun `check page frame should respect url`() {
        val emptyPage = httpServer.emptyPage
        page.setContent("<iframe src='$emptyPage'></iframe>")
        assertNull(page.frameByUrl(Pattern.compile("bogus")))
        val frame = page.frameByUrl(Pattern.compile(".*empty.*"))
        assertNotNull(frame)
        assertEquals(emptyPage, frame.url())
    }

    @Test
    fun `check screenshot should be saved`() {
        val screenShotFile = Path("screenshot.png")
        try {
            page.navigate(httpServer.emptyPage)
            val byte = page.screenshot(ScreenshotOptions { it.path = screenShotFile })
            assertTrue(Files.exists(screenShotFile))
            assertTrue(screenShotFile.toFile().readBytes().contentEquals(byte))
        } finally {
            Files.delete(screenShotFile)
        }
    }

    @Test
    fun `check page press should work`() {
        val pressedButton = "q"
        page.navigate("${httpServer.prefixWithDomain}/input/textarea.html")
        page.press("textarea", pressedButton)
        assertEquals(pressedButton, page.evaluate("() => document.querySelector('textarea').value"))
    }

    @Test
    fun `check page press should work for enter`() {
        page.setContent("<input onkeypress='console.log(\"press\")'></input>")
        val messages = arrayListOf<IConsoleMessage>()
        page.onConsoleMessage { messages.add(it) }
        page.press("input", "Enter")
        assertEquals("press", messages[0].text())
        assertEquals("log", messages[0].type())
    }

    @Test
    fun `check to provide access to the opener page`() {
        val popup = page.waitForPopup { page.evaluate("() => window.open('about:blank')") }
        assertNotNull(popup)
        val opener = popup.opener()
        assertEquals(page, opener)
    }

    @Test
    fun `check to return null if page has been closed`() {
        val popup = page.waitForPopup { page.evaluate("() => window.open('about:blank')") }
        page.close()
        assertNotNull(popup)
        val opener = popup.opener()
        assertNull(opener)
    }

    @Test
    fun `check correct work for page close with window close`() {
        val newPage =
            page.waitForPopup { page.evaluate("() => window['newPage'] = window.open('about:blank')") }
        assertNotNull(newPage)
        assertFalse(newPage.isClosed())
        newPage.waitForClose { page.evaluate("() => window['newPage'].close()") }
        assertTrue(newPage.isClosed())
    }
}