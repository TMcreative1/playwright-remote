package com.playwright.remote.page

import com.playwright.remote.base.BaseTest
import com.playwright.remote.core.exceptions.PlaywrightException
import com.playwright.remote.engine.console.api.IConsoleMessage
import com.playwright.remote.engine.frame.impl.Frame
import com.playwright.remote.engine.options.CloseOptions
import com.playwright.remote.engine.options.NewPageOptions
import com.playwright.remote.engine.options.ScreenshotOptions
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
        BaseTest.page.close()
        try {
            BaseTest.page.evaluate("() => new Promise(r => {})")
            fail("evaluate should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Protocol error"))
        }
    }

    @Test
    fun `closed page should not be visible in context`() {
        assertTrue(BaseTest.browserContext.pages().contains(BaseTest.page))
        BaseTest.page.close()
        assertFalse(BaseTest.browserContext.pages().contains(BaseTest.page))
    }

    @Test
    fun `check run beforeunload if asked for`() {
        BaseTest.page.navigate("${BaseTest.httpServer.prefixWithDomain}/beforeunload.html")

        BaseTest.page.click("body")
        val didShowDialog = arrayOf(false)
        BaseTest.page.onDialog {
            didShowDialog[0] = true
            assertEquals("beforeunload", it.type())
            assertEquals("", it.defaultValue())
            when {
                BaseTest.isChromium() -> {
                    assertEquals("", it.message())
                }
                BaseTest.isWebkit() -> {
                    assertEquals("Leave?", it.message())
                }
                BaseTest.isFirefox() -> {
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
        BaseTest.page.close(CloseOptions { it.runBeforeUnload = true })
        for (index in 0..300) {
            if (didShowDialog[0]) {
                break
            }
            BaseTest.page.waitForTimeout(100.0)
        }
        assertTrue(didShowDialog[0])
    }

    @Test
    fun `should not run beforeunload by default`() {
        BaseTest.page.navigate("${BaseTest.httpServer.prefixWithDomain}/beforeunload.html")
        BaseTest.page.click("body")
        val didShowDialog = arrayOf(false)
        BaseTest.page.onDialog { didShowDialog[0] = true }
        BaseTest.page.close()
        assertFalse(didShowDialog[0])
    }

    @Test
    fun `should set the page close state`() {
        assertFalse(BaseTest.page.isClosed())
        BaseTest.page.close()
        assertTrue(BaseTest.page.isClosed())
    }

    @Test
    fun `should terminate network waiters`() {
        try {
            BaseTest.page.waitForResponse("**") {
                try {
                    BaseTest.page.waitForRequest(BaseTest.httpServer.emptyPage) { BaseTest.page.close() }
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
            BaseTest.page.close()
            BaseTest.page.close()
            BaseTest.page.close()
        } catch (e: PlaywrightException) {
            fail("Error should not be thrown")
        }
    }

    @Test
    fun `check that page url should include hashes`() {
        var expectedUrl = "${BaseTest.httpServer.emptyPage}#hash"
        BaseTest.page.navigate(expectedUrl)
        assertEquals(expectedUrl, BaseTest.page.url())
        BaseTest.page.evaluate("() => { window.location.hash = 'dynamic'; }")
        expectedUrl = "${BaseTest.httpServer.emptyPage}#dynamic"
        assertEquals(expectedUrl, BaseTest.page.url())
    }

    @Test
    fun `check navigate method in browser`() {
        val navigatedUrl = BaseTest.httpServer.emptyPage
        val page = createPageForHttps()
        assertEquals("about:blank", page.url())
        val response = page.navigate(navigatedUrl)
        assert(response != null)
        assertEquals(navigatedUrl, response?.url())
    }

    @Test
    fun `check title method should return title of page`() {
        val navigatedUrl = BaseTest.httpServer.emptyPage
        BaseTest.page.navigate(navigatedUrl)
        assertEquals("Empty Page", BaseTest.page.title())
    }

    @Test
    fun `check page close should work with page close`() {
        try {
            BaseTest.page.waitForClose { BaseTest.page.close() }
        } catch (e: PlaywrightException) {
            fail("Error should not be thrown")
        }
    }

    @Test
    fun `check page frame should respect name`() {
        BaseTest.page.setContent("<iframe name=target></iframe>")
        assertNull(BaseTest.page.frame("bogus"))
        val frame = BaseTest.page.frame("target")
        assertNotNull(frame)
        assertEquals((BaseTest.page.mainFrame() as Frame).childFrames.toList()[0], frame)
    }

    @Test
    fun `check page frame should respect url`() {
        val emptyPage = BaseTest.httpServer.emptyPage
        BaseTest.page.setContent("<iframe src='$emptyPage'></iframe>")
        assertNull(BaseTest.page.frameByUrl(Pattern.compile("bogus")))
        val frame = BaseTest.page.frameByUrl(Pattern.compile(".*empty.*"))
        assertNotNull(frame)
        assertEquals(emptyPage, frame.url())
    }

    @Test
    fun `check screenshot should be saved`() {
        val screenShotFile = Path("screenshot.png")
        try {
            BaseTest.page.navigate(BaseTest.httpServer.emptyPage)
            val byte = BaseTest.page.screenshot(ScreenshotOptions { it.path = screenShotFile })
            assertTrue(Files.exists(screenShotFile))
            assertTrue(screenShotFile.toFile().readBytes().contentEquals(byte))
        } finally {
            Files.delete(screenShotFile)
        }
    }

    @Test
    fun `check page press should work`() {
        val pressedButton = "q"
        BaseTest.page.navigate("${BaseTest.httpServer.prefixWithDomain}/input/textarea.html")
        BaseTest.page.press("textarea", pressedButton)
        assertEquals(pressedButton, BaseTest.page.evaluate("() => document.querySelector('textarea').value"))
    }

    @Test
    fun `check page press should work for enter`() {
        BaseTest.page.setContent("<input onkeypress='console.log(\"press\")'></input>")
        val messages = arrayListOf<IConsoleMessage>()
        BaseTest.page.onConsoleMessage { messages.add(it) }
        BaseTest.page.press("input", "Enter")
        assertEquals("press", messages[0].text())
        assertEquals("log", messages[0].type())
    }

    @Test
    fun `check to provide access to the opener page`() {
        val popup = BaseTest.page.waitForPopup { BaseTest.page.evaluate("() => window.open('about:blank')") }
        assertNotNull(popup)
        val opener = popup.opener()
        assertEquals(BaseTest.page, opener)
    }

    @Test
    fun `check to return null if page has been closed`() {
        val popup = BaseTest.page.waitForPopup { BaseTest.page.evaluate("() => window.open('about:blank')") }
        BaseTest.page.close()
        assertNotNull(popup)
        val opener = popup.opener()
        assertNull(opener)
    }

    @Test
    fun `check correct work for page close with window close`() {
        val newPage =
            BaseTest.page.waitForPopup { BaseTest.page.evaluate("() => window['newPage'] = window.open('about:blank')") }
        assertNotNull(newPage)
        assertFalse(newPage.isClosed())
        newPage.waitForClose { BaseTest.page.evaluate("() => window['newPage'].close()") }
        assertTrue(newPage.isClosed())
    }
}