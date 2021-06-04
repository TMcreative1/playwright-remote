package com.playwright.remote

import com.playwright.remote.base.BaseTest
import com.playwright.remote.core.enums.Media
import com.playwright.remote.core.exceptions.PlaywrightException
import com.playwright.remote.engine.console.api.IConsoleMessage
import com.playwright.remote.engine.frame.impl.Frame
import com.playwright.remote.engine.options.*
import com.playwright.remote.engine.options.enum.ColorScheme.DARK
import com.playwright.remote.engine.options.enum.ColorScheme.LIGHT
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.util.regex.Pattern
import kotlin.io.path.Path
import kotlin.test.*

class TestPage : BaseTest() {

    private fun createPageForHttps(
        options: NewPageOptions? = NewPageOptions { it.ignoreHTTPSErrors = true }
    ) = browser.newPage(options)

    //region Basic
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
        page.navigate("$HTTP_PREFIX/beforeunload.html")

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
        page.navigate("$HTTP_PREFIX/beforeunload.html")
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
                    page.waitForRequest("$HTTP_PREFIX/empty.html") { page.close() }
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
        var expectedUrl = "$HTTP_PREFIX/empty.html#hash"
        page.navigate(expectedUrl)
        assertEquals(expectedUrl, page.url())
        page.evaluate("() => { window.location.hash = 'dynamic'; }")
        expectedUrl = "$HTTP_PREFIX/empty.html#dynamic"
        assertEquals(expectedUrl, page.url())
    }

    @Test
    fun `check navigate method in browser`() {
        val navigatedUrl = "$HTTPS_PREFIX/empty.html"
        val page = createPageForHttps()
        assertEquals("about:blank", page.url())
        val response = page.navigate(navigatedUrl)
        assert(response != null)
        assertEquals(navigatedUrl, response?.url())
    }

    @Test
    fun `check title method should return title of page`() {
        val navigatedUrl = "$HTTP_PREFIX/empty.html"
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
        val emptyPage = "$HTTP_PREFIX/empty.html"
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
            page.navigate("$HTTP_PREFIX/empty.html")
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
        page.navigate("$HTTP_PREFIX/input/textarea.html")
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
    //endregion

    //region Dialog
    @Test
    fun `check dialog with type and value`() {
        page.onDialog {
            assertEquals("alert", it.type())
            assertEquals("", it.defaultValue())
            assertEquals("test", it.message())
            it.accept()
        }
        page.evaluate("alert('test')")
    }

    @Test
    fun `check dialog allow accepting prompts`() {
        page.onDialog {
            assertEquals("prompt", it.type())
            assertEquals("yes", it.defaultValue())
            assertEquals("question?", it.message())
            it.accept("answer")
        }
        val result = page.evaluate("prompt('question?', 'yes')")
        assertEquals("answer", result)
    }

    @Test
    fun `check dialog dismiss the prompt`() {
        page.onDialog {
            it.dismiss()
        }
        val result = page.evaluate("prompt('question?')")
        assertNull(result)
    }

    @Test
    fun `check dialog accept the confirm prompt`() {
        page.onDialog {
            it.accept()
        }
        val result = page.evaluate("confirm('boolean?')")
        assertEquals(true, result)
    }

    @Test
    fun `check dialog dismiss the confirm prompt`() {
        page.onDialog {
            it.dismiss()
        }
        val result = page.evaluate("() => confirm('boolean?')")
        assertEquals(false, result)
    }

    @Test
    fun `check closing with open alert`() {
        val didShowDialog = arrayListOf(false)
        page.onDialog {
            didShowDialog[0] = true
        }
        page.evaluate("() => setTimeout(() => alert('hello'), 0)")
        while (!didShowDialog[0]) {
            page.waitForTimeout(100.0)
        }
    }

    @Test
    fun `check handling of multiple alerts`() {
        page.onDialog {
            it.accept()
        }
        page.setContent(
            """
        <p>Test</p>
        <script>
            alert('Please dismiss this dialog');
            alert('Please dismiss this dialog');
            alert('Please dismiss this dialog');
        </script>
        """.trimIndent()
        )
        assertEquals("Test", page.textContent("p"))
    }

    @Test
    fun `check handling of multiple confirms`() {
        page.onDialog {
            it.accept()
        }
        page.setContent(
            """
        <p>Test</p>
        <script>
            confirm('Please confirm me');
            confirm('Please confirm me');
            confirm('Please confirm me');
        </script>
        """.trimIndent()
        )
        assertEquals("Test", page.textContent("p"))
    }

    @Test
    fun `check to dismiss the prompt without listeners`() {
        val result = page.evaluate("() => prompt('question?')")
        assertNull(result)
    }

    @Test
    fun `check to dismiss the alert without listeners`() {
        page.setContent("<div onclick='window.alert(123); window._clicked=true'>Click me</div>")
        page.click("div")
        assertEquals(true, page.evaluate("window._clicked"))
    }
    //endregion

    //region EmulateMedia
    @Test
    fun `check emulate type`() {
        assertEquals(true, page.evaluate("() => matchMedia('screen').matches"))
        assertEquals(false, page.evaluate("() => matchMedia('print').matches"))

        page.emulateMedia(EmulateMediaOptions { it.media = Media.PRINT })
        assertEquals(false, page.evaluate("() => matchMedia('screen').matches"))
        assertEquals(true, page.evaluate("() => matchMedia('print').matches"))

        page.emulateMedia(EmulateMediaOptions {})
        assertEquals(false, page.evaluate("() => matchMedia('screen').matches"))
        assertEquals(true, page.evaluate("() => matchMedia('print').matches"))

        page.emulateMedia(EmulateMediaOptions { it.media = null })
        assertEquals(true, page.evaluate("() => matchMedia('screen').matches"))
        assertEquals(false, page.evaluate("() => matchMedia('print').matches"))
    }

    @Test
    fun `check emulate scheme work`() {
        page.emulateMedia(EmulateMediaOptions { it.colorScheme = LIGHT })
        assertEquals(true, page.evaluate("() => matchMedia('(prefers-color-scheme: light)').matches"))
        assertEquals(false, page.evaluate("() => matchMedia('(prefers-color-scheme: dark)').matches"))

        page.emulateMedia(EmulateMediaOptions { it.colorScheme = DARK })
        assertEquals(false, page.evaluate("() => matchMedia('(prefers-color-scheme: light)').matches"))
        assertEquals(true, page.evaluate("() => matchMedia('(prefers-color-scheme: dark)').matches"))
    }

    @Test
    fun `check default scheme is light`() {
        assertEquals(true, page.evaluate("() => matchMedia('(prefers-color-scheme: light)').matches"))
        assertEquals(false, page.evaluate("() => matchMedia('(prefers-color-scheme: dark)').matches"))

        page.emulateMedia(EmulateMediaOptions { it.colorScheme = DARK })
        assertEquals(false, page.evaluate("() => matchMedia('(prefers-color-scheme: light)').matches"))
        assertEquals(true, page.evaluate("() => matchMedia('(prefers-color-scheme: dark)').matches"))

        page.emulateMedia(EmulateMediaOptions { it.colorScheme = null })
        assertEquals(true, page.evaluate("() => matchMedia('(prefers-color-scheme: light)').matches"))
        assertEquals(false, page.evaluate("() => matchMedia('(prefers-color-scheme: dark)').matches"))
    }

    @Test
    fun `check work in cross provess iframe`() {
        val page = browser.newPage(NewPageOptions { it.colorScheme = DARK })
        page.navigate("$HTTP_PREFIX/empty.html")
        attachFrame(page, "frame1", "$HTTP_PREFIX/empty.html")
        val frame = page.frames()[1]
        assertEquals(true, frame.evaluate("() => matchMedia('(prefers-color-scheme: dark)').matches"))
        page.close()
    }

    @Test
    fun `check changing the actual colors in css`() {
        val content = """
            <style>
                @media (prefers-color-scheme: dark) {
                    div {
                        background: black;
                        color: white;
                    }
                }

                @media (prefers-color-scheme: light) {
                    div {
                        background: white;
                        color: black;
                    }
                }
            </style>
            <div>Test</div>
        """.trimIndent()
        page.setContent(content)
        val backgroundColor = { page.evalOnSelector("div", "div => window.getComputedStyle(div).backgroundColor") }

        page.emulateMedia(EmulateMediaOptions { it.colorScheme = LIGHT })
        assertEquals("rgb(255, 255, 255)", backgroundColor())

        page.emulateMedia(EmulateMediaOptions { it.colorScheme = DARK })
        assertEquals("rgb(0, 0, 0)", backgroundColor())

        page.emulateMedia(EmulateMediaOptions { it.colorScheme = LIGHT })
        assertEquals("rgb(255, 255, 255)", backgroundColor())
    }
    //endregion
}