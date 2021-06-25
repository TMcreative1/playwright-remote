package com.playwright.remote

import com.playwright.remote.base.BaseTest
import com.playwright.remote.core.enums.Media
import com.playwright.remote.core.enums.Platform.*
import com.playwright.remote.core.enums.WaitUntilState.LOAD
import com.playwright.remote.core.exceptions.PlaywrightException
import com.playwright.remote.engine.callback.api.IBindingCallback
import com.playwright.remote.engine.console.api.IConsoleMessage
import com.playwright.remote.engine.frame.impl.Frame
import com.playwright.remote.engine.handle.js.api.IJSHandle
import com.playwright.remote.engine.options.*
import com.playwright.remote.engine.options.enum.ColorScheme.DARK
import com.playwright.remote.engine.options.enum.ColorScheme.LIGHT
import com.playwright.remote.engine.options.wait.WaitForNavigationOptions
import com.playwright.remote.engine.route.api.IRoute
import com.playwright.remote.engine.route.request.api.IRequest
import com.playwright.remote.engine.route.response.api.IResponse
import com.playwright.remote.utils.PlatformUtils.Companion.getCurrentPlatform
import jdk.jfr.Description
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledIfSystemProperty
import java.io.ByteArrayInputStream
import java.nio.file.Files
import java.util.Collections.singletonList
import java.util.regex.Pattern
import javax.imageio.ImageIO
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
        val newPage = page.waitForPopup { page.evaluate("() => window['newPage'] = window.open('about:blank')") }
        assertNotNull(newPage)
        assertFalse(newPage.isClosed())
        newPage.waitForClose { page.evaluate("() => window['newPage'].close()") }
        assertTrue(newPage.isClosed())
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
        page.navigate(httpServer.emptyPage)
        attachFrame(page, "frame1", httpServer.emptyPage)
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

    //region Evaluate
    @Test
    fun `check the returning result of evaluate`() {
        val result = page.evaluate("() => 5 * 2")
        assertEquals(10, result)
    }

    @Test
    fun `check transferring of nan`() {
        val result = page.evaluate("a => a", Double.NaN)
        assertTrue((result as Double).isNaN())
    }

    @Test
    fun `check transferring of 0`() {
        val result = page.evaluate("a => a", -0.0)
        assertEquals(Double.NEGATIVE_INFINITY, 1 / (result as Double))
    }

    @Test
    fun `check transferring of infinity`() {
        val result = page.evaluate("a => a", Double.POSITIVE_INFINITY)
        assertEquals(Double.POSITIVE_INFINITY, result)
    }

    @Test
    fun `check transferring of negative infinity`() {
        val result = page.evaluate("a => a", Double.NEGATIVE_INFINITY)
        assertEquals(Double.NEGATIVE_INFINITY, result)
    }

    @Test
    fun `check transferring of unserializable values`() {
        val value = mapOf<String, Any>(
            "infinity" to Double.POSITIVE_INFINITY,
            "nInfinity" to Double.NEGATIVE_INFINITY,
            "nZero" to -0.0,
            "nan" to Double.NaN
        )
        val result = page.evaluate("value => value", value)
        assertEquals(value, result)
    }

    @Test
    fun `check transferring of promise`() {
        val jsScript = "value => Promise.resolve(value)"
        var result = page.evaluate(jsScript, null)
        assertNull(result)

        result = page.evaluate(jsScript, Double.POSITIVE_INFINITY)
        assertEquals(Double.POSITIVE_INFINITY, result)

        result = page.evaluate(jsScript, -0.0)
        assertEquals(Double.NEGATIVE_INFINITY, 1 / (result as Double))
    }

    @Test
    fun `check transferring of promise with unserializable values`() {
        val value = mapOf<String, Any>(
            "infinity" to Double.POSITIVE_INFINITY,
            "nInfinity" to Double.NEGATIVE_INFINITY,
            "nZero" to -0.0,
            "nan" to Double.NaN
        )
        val result = page.evaluate("value => Promise.resolve(value)", value)
        assertEquals(value, result)
    }

    @Test
    fun `check transferring of array`() {
        val expectedList = listOf(1, 2, 3)
        val result = page.evaluate("a => a", expectedList)
        assertEquals(expectedList, result)
    }

    @Test
    fun `check transferring of array with boolean result`() {
        val result = page.evaluate("a => Array.isArray(a)", listOf(1, 2, 3))
        assertEquals(true, result)
    }

    @Test
    fun `check to modify the global environment`() {
        page.evaluate("() => window['globalVar'] = 123")
        assertEquals(123, page.evaluate("globalVar"))
    }

    @Test
    fun `check to evaluate the global environment on the page context`() {
        page.navigate("${httpServer.prefixWithDomain}/global-var.html")
        assertEquals(321, page.evaluate("globalVar"))
    }

    @Test
    fun `check to return undefined for objects with symbols`() {
        var jsScript = "() => [Symbol('foo4')]"
        assertEquals(listOf(null), page.evaluate(jsScript))
        jsScript = """() => {
            |   const a = {};
            |   a[Symbol('foo4')] = 42;
            |   return a;
            |}
        """.trimMargin()
        assertEquals(emptyMap<Any, Any>(), page.evaluate(jsScript))
        jsScript = """() => {
            |   return { foo: [{ a: Symbol('foo4') }] };
            |}
        """.trimMargin()
        assertEquals(mapOf("foo" to listOf(mapOf("a" to null))), page.evaluate(jsScript))
    }

    @Test
    fun `check to return value with unicode chars`() {
        val result = page.evaluate("a => a['中文字符']", mapOf("中文字符" to 10))
        assertEquals(10, result)
    }

    @Test
    fun `check throw error when evaluation triggers reload`() {
        try {
            val jsScript = """() => {
                |   location.reload();
                |   return new Promise(() => { });
                |}
            """.trimMargin()
            page.evaluate(jsScript)
            fail("evaluate should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("navigation"))
        }
    }

    @Test
    fun `check to await promise`() {
        val result = page.evaluate("() => Promise.resolve(5 * 5)")
        assertEquals(25, result)
    }

    @Test
    fun `check correct work after frame navigated`() {
        val frameEvaluation = arrayListOf<Any>(0)
        page.onFrameNavigated {
            frameEvaluation[0] = it.evaluate("() => 2 * 3")
        }
        page.navigate(httpServer.emptyPage)
        assertEquals(6, frameEvaluation[0])
    }

    @Test
    fun `check correct work after a cross origin navigation`() {
        page.navigate(httpServer.emptyPage)
        val frameEvaluation = arrayListOf<Any>(0)
        page.onFrameNavigated {
            frameEvaluation[0] = it.evaluate("() => 4 * 3")
        }
        page.navigate("${httpServer.prefixWithIP}/empty.page")
        assertEquals(12, frameEvaluation[0])
    }

    @Test
    fun `check correct work from inside an exposed function`() {
        page.exposeFunction("callController") {
            page.evaluate("({ a, b}) => a * b", mapOf("a" to it[0], "b" to it[1]))
        }
        val jsScript = """async function() {
            |   return await window['callController'](3, 6);
            |}
        """.trimMargin()
        val result = page.evaluate(jsScript)
        assertEquals(18, result)
    }

    @Test
    fun `check to reject promise with exception`() {
        try {
            page.evaluate("() => not_existing_object.property")
            fail("evaluate should throw ")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("not_existing_object"))
        }
    }

    @Test
    fun `check thrown string as the error message`() {
        try {
            page.evaluate("() => { throw 'err'; }")
            fail("evaluate should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("err"))
        }
    }

    @Test
    fun `check thrown number as the error message`() {
        try {
            page.evaluate("() => { throw 404; }")
            fail("evaluate should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("404"))
        }
    }

    @Test
    fun `check to return complex object`() {
        val map = mapOf<String, Any>("foo" to "bar!")
        val result = page.evaluate("a => a", map)
        assertNotSame(map, result)
        assertEquals(map, result)
    }

    @Test
    fun `check to return NaN`() {
        val result = page.evaluate("() => NaN")
        assertEquals(Double.NaN, result)
    }

    @Test
    fun `check to return 0`() {
        val result = page.evaluate("() => -0")
        assertEquals(Double.NEGATIVE_INFINITY, 1 / (result as Double))
    }

    @Test
    fun `check to return infinity`() {
        val result = page.evaluate("() => Infinity")
        assertEquals(Double.POSITIVE_INFINITY, result)
    }

    @Test
    fun `check to return negative infinity`() {
        val result = page.evaluate("() => -Infinity")
        assertEquals(Double.NEGATIVE_INFINITY, result)
    }

    @Test
    fun `check correct work with overwritten promise`() {
        var jsScript = """() => {
            |   const initialPromise = window.Promise;
            |   class Promise2 {
            |       static all(arg) {
            |           return wrap(initialPromise.all(arg));
            |       }
            |       static race(arg) {
            |           return wrap(initialPromise.race(arg));
            |       }
            |       static resolve(arg) {
            |           return wrap(initialPromise.resolve(arg));
            |       }
            |       constructor(f) {
            |           this._promise = new initialPromise(f);
            |       }
            |       then(f, r) {
            |           return wrap(this._promise.then(f, r));
            |       }
            |       catch(f) {
            |           return wrap(this._promise.catch(f));
            |       }
            |       finally(f) {
            |           return wrap(this._promise.finally(f));
            |       }
            |   }
            |   const wrap = p => {
            |       const result = new Promise2(() => { });
            |       result._promise = p;
            |       return result;
            |   };
            |   window.Promise = Promise2;
            |   window['__Promise2'] = Promise2;
            |}
        """.trimMargin()
        page.evaluate(jsScript)
        jsScript = """() => {
            |   const p = Promise.all([Promise.race([]), new Promise(() => { }).then(() => { })]);
            |   return p instanceof window['__Promise2'];
            |}
        """.trimMargin()
        assertEquals(true, page.evaluate(jsScript))
        assertEquals(23, page.evaluate("() => Promise.resolve(23)"))
    }

    @Test
    fun `check to serialize undefined fields`() {
        val result = page.evaluate("() => ({ a: undefined })")
        assertEquals(mapOf("a" to null), result)
    }

    @Test
    fun `check to return null`() {
        val result = page.evaluate("x => x", null)
        assertNull(result)
    }

    @Test
    fun `check to serialize null fields`() {
        val result = page.evaluate("() => ({ a: null })")
        assertEquals(mapOf("a" to null), result)
    }

    @Test
    fun `check to return undefined values for unserializable objects`() {
        val result = page.evaluate("() => window")
        assertNull(result)
    }

    @Test
    fun `check to return value for the looped object`() {
        val jsScript = """() => {
            |   const a = {};
            |   const b = { a };
            |   a.b = b;
            |   return a;
            |}
        """.trimMargin()
        assertNull(page.evaluate(jsScript))
    }

    @Test
    fun `check to able to throw a tricky error`() {
        val windowHandle = page.evaluateHandle("() => window")
        val errorText: String?
        try {
            windowHandle.jsonValue()
            fail("jsonValue should throw")
        } catch (e: PlaywrightException) {
            errorText = e.message
        }
        assertNotNull(errorText)
        try {
            val jsScript = """errorText => {
                |   throw new Error(errorText);
                |}
            """.trimMargin()
            page.evaluate(jsScript, errorText)
            fail("evaluate should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains(errorText))
        }
    }

    @Test
    fun `check to accept value as string`() {
        val result = page.evaluate("1 + 2")
        assertEquals(3, result)
    }

    @Test
    fun `check to accept value with semicolon`() {
        val result = page.evaluate("1 + 5;")
        assertEquals(6, result)
    }

    @Test
    fun `check to accept values with comments`() {
        val result = page.evaluate("2 + 5;\n//test comments")
        assertEquals(7, result)
    }

    @Test
    fun `check to accept element handle as an argument`() {
        page.setContent("<section>23</section>")
        val element = page.querySelector("section")
        val text = page.evaluate("e => e.textContent", element)
        assertEquals("23", text)
    }

    @Test
    fun `check to throw if the underlying element was disposed`() {
        page.setContent("<section>23</section>")
        val element = page.querySelector("section")
        assertNotNull(element)
        element.dispose()
        try {
            page.evaluate("e => e.textContent", element)
            fail("evaluate should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("JSHandle is disposed"))
        }
    }

    @Test
    fun `check to simulate a user gesture`() {
        val jsScript = """() => {
            |   document.body.appendChild(document.createTextNode('test'));
            |   document.execCommand('selectAll');
            |   return document.execCommand('copy');
            |}
        """.trimMargin()
        assertEquals(true, page.evaluate(jsScript))
    }

    @Test
    fun `check to throw error after navigation`() {
        try {
            val jsScript = """() => {
                |   const promise = new Promise(f => window['__resolve'] = f);
                |   window.location.reload();
                |   setTimeout(() => window['__resolve'](23), 1000);
                |   return promise;
                |}
            """.trimMargin()
            page.waitForNavigation {
                page.evaluate(jsScript)
            }
            fail("navigation should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("navigation"))
        }
    }

    @Test
    fun `check to not throw an error when evaluation does a navigation`() {
        page.navigate("${httpServer.prefixWithDomain}/page-with-one-style.html")
        val jsScript = """() => {
            |   window.location.href = '/empty.html';
            |   return [23];
            |}
        """.trimMargin()
        val result = page.evaluate(jsScript)
        assertEquals(listOf(23), result)
    }

    @Test
    @DisabledIfSystemProperty(named = "browser", matches = "^\$|webkit")
    fun `check not throw an error when evaluation does a synchronous navigation and returns and object`() {
        val jsScript = """() => {
            |   window.location.reload();
            |   return { a: 23 };
            |}
        """.trimMargin()
        val result = page.evaluate(jsScript)
        assertEquals(mapOf("a" to 23), result)
    }

    @Test
    fun `check to not throw an error when evaluation does a synchronous navigation and returns undefined`() {
        val jsScript = """() => {
            |   window.location.reload();
            |   return undefined;
            |}
        """.trimMargin()
        val result = page.evaluate(jsScript)
        assertNull(result)
    }

    @Test
    fun `check to transfer 100mb of data from page to node js`() {
        val result = page.evaluate("() => Array(100* 1024 * 1024 + 1).join('a')")
        result as String
        assertEquals(100 * 1024 * 1024, result.length)
        result.forEachIndexed { index, char ->
            if ('a' != char) {
                fail("Unexpected char at position $index")
            }
        }
    }

    @Test
    fun `check to throw error with detailed information inside promise`() {
        try {
            val jsScript = """() => new Promise(() => {
                |   throw new Error('Error in promise');
                |})""".trimMargin()
            page.evaluate(jsScript)
            fail("evaluate should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Error in promise"))
        }
    }

    @Test
    fun `check correct work when json is set to null`() {
        val jsScript = "() => { window.JSON.stringify = null; window.JSON = null; }"
        page.evaluate(jsScript)
        val result = page.evaluate("() => ({ abc: 123 })")
        assertEquals(mapOf("abc" to 123), result)
    }

    @Test
    fun `check to await promise from popup`() {
        page.navigate(httpServer.emptyPage)
        val jsScript = """() => {
            |   const win = window.open('about:blank');
            |   return new win['Promise'](f => f(23));
            |}
        """.trimMargin()
        val result = page.evaluate(jsScript)
        assertEquals(23, result)
    }

    @Test
    fun `check correct work with new function and CSP`() {
        httpServer.setCSP("/empty.html", "script-src ${httpServer.prefixWithDomain}")
        page.navigate(httpServer.emptyPage)
        val result = page.evaluate("() => new Function('return true')()")
        assertEquals(true, result)
    }

    @Test
    fun `check correct work with non strict expression`() {
        val jsScript = """() => {
            |   y = 3.14;
            |   return y;
            |}
        """.trimMargin()
        assertEquals(3.14, page.evaluate(jsScript))
    }

    @Test
    fun `check to throw with strict expression`() {
        try {
            val jsScript = """() => {
                |   'use strict';
                |   // @ts-ignore
                |   variableY = 3.14;
                |   // @ts-ignore
                |   return variableY;
                |}
            """.trimMargin()
            page.evaluate(jsScript)
            fail("evaluate should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("variableY"))
        }
    }

    @Test
    fun `check not to leak utility script`() {
        assertEquals(true, page.evaluate("() => this == window"))
    }

    @Test
    fun `check not to leak handles`() {
        try {
            page.evaluate("handles.length")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("handles"))
        }
    }

    @Test
    fun `check correct work with CSP`() {
        httpServer.setCSP("/empty.html", "script-src 'self'")
        page.navigate(httpServer.emptyPage)
        assertEquals(4, page.evaluate("() => 2 + 2"))
    }

    @Test
    fun `check to evaluate exception`() {
        val jsScript = """() => {
                |   return (function functionOnStack() {
                |       return new Error('error message');
                |   })();
                |}
            """.trimMargin()
        val result = page.evaluate(jsScript)
        result as String
        assertTrue(result.contains("Error: error message"))
        assertTrue(result.contains("functionOnStack"))
    }

    @Test
    fun `check to not use json when evaluating`() {
        val result = page.evaluate("() => ({ toJSON: () => 'string', data: 'data' })")
        assertEquals(mapOf("data" to "data", "toJSON" to emptyMap<Any, Any>()), result)
    }

    @Test
    fun `check to not use json in json value`() {
        val result = page.evaluateHandle("() => ({ toJSON: () => 'string', data: 'data' })")
        assertEquals(mapOf("data" to "data", "toJSON" to emptyMap<Any, Any>()), result.jsonValue())
    }
    //endregion

    //region EventNetwork
    @Test
    fun `check request events`() {
        val requests = arrayListOf<IRequest>()
        page.onRequest { requests.add(it) }
        page.navigate(httpServer.emptyPage)
        assertEquals(1, requests.size)
        assertEquals(httpServer.emptyPage, requests[0].url())
        assertEquals("document", requests[0].resourceType())
        assertEquals("GET", requests[0].method())
        assertNotNull(requests[0].response())
        assertEquals(page.mainFrame(), requests[0].frame())
        assertEquals(httpServer.emptyPage, requests[0].frame().url())
    }

    @Test
    fun `check response events`() {
        val responses = arrayListOf<IResponse>()
        page.onResponse { responses.add(it) }
        page.navigate(httpServer.emptyPage)
        assertEquals(1, responses.size)
        assertEquals(httpServer.emptyPage, responses[0].url())
        assertEquals(200, responses[0].status())
        assertTrue(responses[0].ok())
        assertNotNull(responses[0].request())
    }

    @Test
    fun `check request failed events`() {
        httpServer.setRoute("/one-style.css") { it.responseBody.close() }
        val failedRequests = arrayListOf<IRequest>()
        page.onRequestFailed { failedRequests.add(it) }
        page.navigate("${httpServer.prefixWithDomain}/page-with-one-style.html")
        assertEquals(1, failedRequests.size)
        assertTrue(failedRequests[0].url().contains("one-style.css"))
        assertNull(failedRequests[0].response())
        assertEquals("stylesheet", failedRequests[0].resourceType())
        when {
            isChromium() -> assertEquals("net::ERR_EMPTY_RESPONSE", failedRequests[0].failure())
            isWebkit() -> when (getCurrentPlatform()) {
                MAC -> assertEquals("The network connection was lost.", failedRequests[0].failure())
                WINDOWS64, WINDOWS32 -> assertEquals(
                    "Server returned nothing (no headers, no data)",
                    failedRequests[0].failure()
                )
                else -> assertEquals("Message Corrupt", failedRequests[0].failure())
            }
            else -> assertEquals("NS_ERROR_NET_RESET", failedRequests[0].failure())
        }
        assertNotNull(failedRequests[0].frame())
    }

    @Test
    fun `check request finished events`() {
        val finishedRequests = arrayListOf<IRequest>()
        page.onRequestFinished { finishedRequests.add(it) }
        val response = page.navigate(httpServer.emptyPage)
        assertNotNull(response)
        assertNull(response.finished())
        assertNotNull(finishedRequests[0])
        assertEquals(response.request(), finishedRequests[0])
        assertEquals(httpServer.emptyPage, finishedRequests[0].url())
        assertNotNull(finishedRequests[0].response())
        assertEquals(page.mainFrame(), finishedRequests[0].frame())
        assertEquals(httpServer.emptyPage, finishedRequests[0].frame().url())
        assertTrue(finishedRequests[0].failure().isEmpty())
    }

    @Test
    fun `check events in proper order`() {
        val events = arrayListOf<String>()
        page.onRequest { events.add("request") }
        page.onResponse { events.add("response") }
        page.onRequestFinished { events.add("requestFinished") }
        val response = page.navigate(httpServer.emptyPage)
        assertNull(response!!.finished())
        assertEquals(listOf("request", "response", "requestFinished"), events)
    }

    @Test
    fun `check to support redirects`() {
        val events = arrayListOf<String>()
        page.onRequest { events.add("${it.method()} ${it.url()}") }
        page.onResponse { events.add("${it.status()} ${it.url()}") }
        page.onRequestFinished { events.add("DONE ${it.url()}") }
        page.onRequestFailed { events.add("FAILED ${it.url()}") }
        httpServer.setRedirect("/em.html", "/empty.html")
        val emUrl = "${httpServer.prefixWithDomain}/em.html"
        val response = page.navigate(emUrl)
        response!!.finished()
        assertEquals(
            listOf(
                "GET $emUrl",
                "302 $emUrl",
                "DONE $emUrl",
                "GET ${httpServer.emptyPage}",
                "200 ${httpServer.emptyPage}",
                "DONE ${httpServer.emptyPage}",
            ), events
        )
        val redirectFrom = response.request().redirectedFrom()
        assertNotNull(redirectFrom)
        assertTrue(redirectFrom.url().contains("/em.html"))
        assertNull(redirectFrom.redirectedFrom())
        assertEquals(response.request(), redirectFrom.redirectedTo())
    }
    //endregion

    //region ExposeFunction
    @Test
    fun `check correct work of expose function`() {
        val context = browser.newContext()
        val page = context.newPage()
        val bindingSource = arrayListOf<IBindingCallback.ISource?>(null)
        page.exposeBinding("add") { source, args ->
            bindingSource[0] = source
            args[0] as Int + args[1] as Int
        }
        val jsScript = """async function() {
            |   return window['add'](10, 13);
            |}
        """.trimMargin()
        val result = page.evaluate(jsScript)
        assertEquals(context, bindingSource[0]!!.context())
        assertEquals(page, bindingSource[0]!!.page())
        assertEquals(page.mainFrame(), bindingSource[0]!!.frame())
        assertEquals(23, result)
        context.close()
    }

    @Test
    fun `check correct work of expose function with await`() {
        val jsScript = """async function() {
            |   return await window['compute'](2, 12);
            |}
        """.trimMargin()
        page.exposeBinding("compute") { _, args -> args[0] as Int * args[1] as Int }
        val result = page.evaluate(jsScript)
        assertEquals(24, result)
    }

    @Test
    fun `check correct work with handles and complex objects`() {
        var jsScript = """() => {
            |   window['fooValue'] = { bar: 2 };
            |   return window['fooValue'];
            |}
        """.trimMargin()
        val handle = page.evaluateHandle(jsScript)
        page.exposeFunction("handle") { arrayListOf(mapOf("foo" to handle)) }
        jsScript = """async function() {
            |   const value = await window['handle']();
            |   const [{ foo }] = value;
            |   return foo === window['fooValue'];
            |}
        """.trimMargin()
        val result = page.evaluate(jsScript)
        assertEquals(true, result)
    }

    @Test
    fun `check to throw exception in page context`() {
        page.exposeFunction("callException") {
            throw RuntimeException("Exception called")
        }
        val jsScript = """async () => {
            |   try {
            |       await window["callException"]();
            |   } catch (e) {
            |       return { message: e.message, stack: e.stack };
            |   }
            |}
        """.trimMargin()
        val result = page.evaluate(jsScript)
        assertTrue(result is Map<*, *>)
        assertEquals("Exception called", result["message"])
        assertTrue((result["stack"] as String).contains("check to throw exception in page context"))
    }

    @Test
    fun `check of call inside function`() {
        val called = arrayListOf(false)
        page.exposeFunction("callMe") {
            called[0] = true
            called
        }
        page.addInitScript("window['callMe']()")
        page.reload()
        assertTrue(called[0])
    }

    @Test
    fun `check correct work with navigation`() {
        page.exposeFunction("compute") { args -> args[0] as Int * args[1] as Double }
        val jsScript = """async function() {
            |   return await window['compute'](10, 2.3);
            |}
        """.trimMargin()
        val result = page.evaluate(jsScript)
        page.navigate(httpServer.emptyPage)
        assertEquals(23, result)
    }

    @Test
    fun `check correct work with frames`() {
        page.exposeFunction("compute") { args -> args[0] as Int + args[1] as Int }
        page.navigate("${httpServer.prefixWithDomain}/frames/nested-frames.html")
        val frame = page.frames()[1]
        val jsScript = """async function() {
            |   return window['compute'](20, 3);
            |}
        """.trimMargin()
        val result = frame.evaluate(jsScript)
        assertEquals(23, result)
    }

    @Test
    fun `check correct work with frame before navigation`() {
        page.navigate("${httpServer.prefixWithDomain}/frames/nested-frames.html")
        page.exposeFunction("compute") { args -> args[0] as Int * args[1] as Double }
        val frame = page.frames()[1]
        val jsScript = """async function() {
            |   return window['compute'](10, 2.3);
            |}
        """.trimMargin()
        val result = frame.evaluate(jsScript)
        assertEquals(23, result)
    }

    @Test
    fun `check correct work after cross origin navigation`() {
        page.navigate(httpServer.emptyPage)
        page.exposeFunction("compute") { args -> args[0] as Int + args[1] as Int }

        page.navigate("${httpServer.prefixWithIP}/empty.html")
        val result = page.evaluate("window['compute'](3, 20)")
        assertEquals(23, result)
    }

    @Test
    fun `check correct work with complex objects`() {
        page.exposeFunction("complexObject") { args ->
            val a = args[0] as Map<*, *>
            val b = args[1] as Map<*, *>
            val sum = a["x"] as Int + b["x"] as Int
            mapOf("x" to sum)
        }
        val result = page.evaluate("async () => window['complexObject']({x: 18}, {x: 5})")
        assertTrue(result is Map<*, *>)
        assertEquals(23, result["x"])
    }

    @Test
    fun `check handle of expose binding`() {
        val target = arrayListOf<IJSHandle?>(null)
        page.exposeBinding("logMe", { _, args ->
            target[0] = args[0] as IJSHandle
            21
        }, ExposeBindingOptions { it.handle = true })
        val jsScript = """async function() {
            |   return window['logMe']({ age: 23 });
            |}
        """.trimMargin()
        val result = page.evaluate(jsScript)
        assertEquals(23, target[0]!!.evaluate("x => x.age"))
        assertEquals(21, result)
    }

    @Test
    fun `check to not throw exception during navigation`() {
        page.exposeBinding("logMe", { _, _ -> 23 }, ExposeBindingOptions { it.handle = true })
        page.navigate(httpServer.emptyPage)

        page.waitForNavigation(WaitForNavigationOptions { it.waitUntil = LOAD }) {
            val jsScript = """async url => {
                |   window['logMe']({ foo: 24 });
                |   window.location.href = url;
                |}
            """.trimMargin()
            page.evaluate(jsScript, "${httpServer.prefixWithDomain}/page-with-one-style.html")
        }
    }

    @Test
    fun `check to throw for duplicate registrations`() {
        page.exposeFunction("fun1") { "response" }
        try {
            page.exposeFunction("fun1") { "response" }
            fail("exposeFunction should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Function fun1 has been already registered"))
        }
    }

    @Test
    fun `check to throw for multiple arguments`() {
        var jsScript = """async function() {
            |   return window['logMe']({ age: 23 });
            |}
        """.trimMargin()
        page.exposeBinding("logMe", { _, _ -> 23 }, ExposeBindingOptions { it.handle = true })
        assertEquals(23, page.evaluate(jsScript))
        jsScript = """async function() {
            |   return window['logMe']({ age: 23 }, undefined, undefined);
            |}
        """.trimMargin()
        assertEquals(23, page.evaluate(jsScript))
        jsScript = """async function() {
            |   return window['logMe'](undefined, undefined, undefined);
            |}
        """.trimMargin()
        assertEquals(23, page.evaluate(jsScript))
        try {
            jsScript = """async function() {
                |   return window['logMe'](1, 2);
                |}
            """.trimMargin()
            page.evaluate(jsScript)
            fail("evaluate should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("exposeBindingHandle supports a single argument, 2 received"))
        }
    }
    //endregion

    //region Screenshot
    @Test
    fun `check correct work of screenshot`() {
        page.setViewportSize(500, 500)
        page.navigate("${httpServer.prefixWithDomain}/grid.html")
        val screenshot = page.screenshot()
        val image = ImageIO.read(ByteArrayInputStream(screenshot))
        assertEquals(500, image.width)
        assertEquals(500, image.height)
    }

    @Test
    fun `check correct work of screenshot with clip`() {
        page.setViewportSize(500, 500)
        page.navigate("${httpServer.prefixWithDomain}/grid.html")
        val screenshot = page.screenshot(ScreenshotOptions { it.clip = Clip(50.0, 100.0, 150.0, 100.0) })
        val image = ImageIO.read(ByteArrayInputStream(screenshot))
        assertEquals(150, image.width)
        assertEquals(100, image.height)
    }
    //endregion

    //region Fill
    @Test
    fun `check to fill textarea`() {
        page.navigate("${httpServer.prefixWithDomain}/input/textarea.html")
        page.fill("textarea", "my value")
        val result = page.evaluate("() => window['result']")
        assertEquals("my value", result)
    }

    @Test
    fun `check to fill input`() {
        page.navigate("${httpServer.prefixWithDomain}/input/textarea.html")
        page.fill("input", "my value")
        val result = page.evaluate("() => window['result']")
        assertEquals("my value", result)
    }

    @Test
    fun `check to throw unsupported inputs exception`() {
        page.navigate("${httpServer.prefixWithDomain}/input/textarea.html")
        arrayListOf("button", "checkbox", "file", "image", "radio", "range", "reset", "submit").forEach {
            page.evalOnSelector("input", "(input, type) => input.setAttribute('type', type)", it)
            try {
                page.fill("input", "")
                fail("fill should throw")
            } catch (e: PlaywrightException) {
                assertTrue(e.message!!.contains("input of type \"$it\" cannot be filled"), e.message)
            }
        }
    }

    @Test
    fun `check to fill different input types`() {
        page.navigate("${httpServer.prefixWithDomain}/input/textarea.html")
        arrayListOf("password", "search", "tel", "text", "url").forEach {
            page.evalOnSelector("input", "(input, type) => input.setAttribute('type', type)", it)
            val expectedValue = "text $it"
            page.fill("input", expectedValue)
            val result = page.evaluate("() => window['result']")
            assertEquals(expectedValue, result)
        }
    }

    @Test
    fun `check to fill date input after clicking`() {
        page.setContent("<input type=date>")
        page.click("input")
        val expectedDate = "2021-06-23"
        page.fill("input", expectedDate)
        val result = page.evalOnSelector("input", "input => input.value")
        assertEquals(expectedDate, result)
    }

    @Test
    @DisabledIfSystemProperty(named = "browser", matches = "^\$|webkit")
    fun `check to throw exception on incorrect date`() {
        page.setContent("<input type=date>")
        try {
            page.fill("input", "2021-13-23")
            fail("fill should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Malformed value"))
        }
    }

    @Test
    fun `check to fill time input`() {
        page.setContent("<input type=time>")
        val expectedTime = "14:32"
        page.fill("input", expectedTime)
        val result = page.evalOnSelector("input", "input => input.value")
        assertEquals(expectedTime, result)
    }

    @Test
    @DisabledIfSystemProperty(named = "browser", matches = "^\$|webkit")
    fun `check to throw exception on incorrect time`() {
        page.setContent("<input type=time>")
        try {
            page.fill("input", "26:10")
            fail("fill should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Malformed value"))
        }
    }

    @Test
    fun `check to fill datetime local input`() {
        page.setContent("<input type=datetime-local>")
        val expectedDateTime = "2021-06-23T15:25"
        page.fill("input", expectedDateTime)
        val result = page.evalOnSelector("input", "input => input.value", expectedDateTime)
        assertEquals(expectedDateTime, result)
    }

    @Test
    @DisabledIfSystemProperty(named = "browser", matches = "^\$|webkit")
    fun `check to throw exception on incorrect datetime local`() {
        page.setContent("<input type=datetime-local>")
        try {
            page.fill("input", "word")
            fail("fill should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Malformed value"))
        }
    }

    @Test
    fun `check to fill content editable`() {
        page.navigate("${httpServer.prefixWithDomain}/input/textarea.html")
        val expectedValue = "my value"
        page.fill("div[contenteditable]", expectedValue)
        val result = page.evalOnSelector("div[contenteditable]", "div => div.textContent")
        assertEquals(expectedValue, result)
    }

    @Test
    fun `check to fill elements with existing value and selection`() {
        page.navigate("${httpServer.prefixWithDomain}/input/textarea.html")

        page.evalOnSelector("input", "input => input.value = 'value one'")
        var expectedValue = "another value"
        page.fill("input", expectedValue)
        var result = page.evaluate("() => window['result']")
        assertEquals(expectedValue, result)

        page.evalOnSelector(
            "input", """input => {
            |   input.selectionStart = 1;
            |   input.selectionEnd = 2;
            |}
        """.trimMargin()
        )

        expectedValue = "or this value"
        page.fill("input", expectedValue)
        result = page.evaluate("() => window['result']")
        assertEquals(expectedValue, result)

        page.evalOnSelector(
            "div[contenteditable]", """div => {
            |   div.innerHTML = 'some text <span>some more text<span> and even more text';
            |   const range = document.createRange();
            |   range.selectNodeContents(div.querySelector('span'));
            |   const selection = window.getSelection();
            |   selection.removeAllRanges();
            |   selection.addRange(range);
            |}
        """.trimMargin()
        )
        expectedValue = "replace with this"
        page.fill("div[contenteditable]", expectedValue)
        result = page.evalOnSelector("div[contenteditable]", "div => div.textContent")
        assertEquals(expectedValue, result)
    }

    @Test
    fun `check to throw exception when element is not an input textarea or content editable`() {
        page.navigate("${httpServer.prefixWithDomain}/input/textarea.html")
        try {
            page.fill("body", "")
            fail("fill should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Element is not an <input>"))
        }
    }

    @Test
    fun `check to fill the body`() {
        page.setContent("<body contentEditable='true'></body>")
        val expectedValue = "my value"
        page.fill("body", expectedValue)
        val result = page.evaluate("() => document.body.textContent")
        assertEquals(expectedValue, result)
    }

    @Test
    fun `check to fill fixed position input`() {
        page.setContent("<input style='position: fixed;'/>")
        val expectedValue = "my value"
        page.fill("input", expectedValue)
        val result = page.evaluate("() => document.querySelector('input').value")
        assertEquals(expectedValue, result)
    }

    @Test
    fun `check to fill when focus in the wrong frame`() {
        page.setContent(
            """<div contentEditable='true'></div>
            |<iframe></iframe>
        """.trimMargin()
        )

        page.focus("iframe")
        val expectedValue = "my value"
        page.fill("div", expectedValue)
        val result = page.evalOnSelector("div", "d => d.textContent")
        assertEquals(expectedValue, result)
    }

    @Test
    fun `check to fill the input type number`() {
        page.setContent("<input id='input' type='number'></input>")
        val expectedValue = "23"
        page.fill("input", expectedValue)
        val result = page.evaluate("() => window['input'].value")
        assertEquals(expectedValue, result)
    }

    @Test
    fun `check to fill exponent into the input type number`() {
        page.setContent("<input id='input' type='number'></input>")
        val expectedValue = "-10e5"
        page.fill("input", expectedValue)
        val result = page.evaluate("() => window['input'].value")
        assertEquals(expectedValue, result)
    }

    @Test
    fun `check to fill input type number with empty string`() {
        page.setContent("<input id='input' type='number' value='123'></input>")
        page.fill("input", "")
        val result = page.evaluate("() => window['input'].value")
        assertEquals("", result)
    }

    @Test
    fun `check to fill text into the input type number`() {
        page.setContent("<input id='input' type='number'></input>")
        try {
            page.fill("input", "word")
            fail("fill should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Cannot type text into input[type=number]"))
        }
    }

    @Test
    fun `check to clear input`() {
        page.navigate("${httpServer.prefixWithDomain}/input/textarea.html")
        var expectedValue = "my value"
        page.fill("input", expectedValue)
        var result = page.evaluate("() => window['result']")
        assertEquals(expectedValue, result)

        expectedValue = ""
        page.fill("input", expectedValue)
        result = page.evaluate("() => window['result']")
        assertEquals(expectedValue, result)
    }
    //endregion

    //region Keyboard
    @Test
    fun `check to type into a textarea`() {
        val jsScript = """() => {
            |   const textarea = document.createElement('textarea');
            |   document.body.appendChild(textarea);
            |   textarea.focus();
            |}
        """.trimMargin()
        page.evaluate(jsScript)
        val text = "Hello world. I am the next that was typed!"
        page.keyboard().type(text)
        val result = page.evaluate("() => document.querySelector('textarea').value")
        assertEquals(text, result)
    }

    @Test
    fun `check to move with the arrow keys`() {
        page.navigate("${httpServer.prefixWithDomain}/input/textarea.html")
        var text = "Age 23"
        page.type("textarea", text)
        var result = page.evaluate("() => document.querySelector('textarea').value")
        assertEquals(text, result)

        for (i in 0 until "23".length) {
            page.keyboard().press("ArrowLeft")
        }
        page.keyboard().type("inserted ")
        text = "Age inserted 23"
        result = page.evaluate("() => document.querySelector('textarea').value")
        assertEquals(text, result)

        page.keyboard().down("Shift")
        for (i in 0 until "inserted ".length) {
            page.keyboard().press("ArrowLeft")
        }
        page.keyboard().up("Shift")
        page.keyboard().press("Backspace")
        text = "Age 23"
        result = page.evaluate("() => document.querySelector('textarea').value")
        assertEquals(text, result)
    }

    @Test
    fun `check to send a character by element handle press`() {
        page.navigate("${httpServer.prefixWithDomain}/input/textarea.html")
        val textarea = page.querySelector("textarea")
        assertNotNull(textarea)
        val character = "a"
        textarea.press(character)
        var result = page.evaluate("() => document.querySelector('textarea').value")
        assertEquals(character, result)

        page.evaluate("() => window.addEventListener('keydown', e => e.preventDefault(), true)")
        textarea.press("b")
        result = page.evaluate("() => document.querySelector('textarea').value")
        assertEquals(character, result)
    }

    @Test
    fun `check to send a character by insert text`() {
        page.navigate("${httpServer.prefixWithDomain}/input/textarea.html")
        page.focus("textarea")
        var character = "嗨"
        page.keyboard().insertText(character)
        var result = page.evaluate("() => document.querySelector('textarea').value")
        assertEquals(character, result)

        page.evaluate("() => window.addEventListener('keydown', e => e.preventDefault(), true)")
        page.keyboard().insertText("a")
        character += "a"
        result = page.evaluate("() => document.querySelector('textarea').value")
        assertEquals(character, result)
    }

    @Test
    fun `check to insert text should only emit input event`() {
        page.navigate("${httpServer.prefixWithDomain}/input/textarea.html")
        page.focus("textarea")
        val jsScript = """() => {
            |   const events = [];
            |   document.addEventListener('keydown', e => events.push(e.type));
            |   document.addEventListener('keyup', e => events.push(e.type));
            |   document.addEventListener('keypress', e => events.push(e.type));
            |   document.addEventListener('input', e => events.push(e.type));
            |   return events;
            |}
        """.trimMargin()
        val events = page.evaluateHandle(jsScript)
        page.keyboard().insertText("Hello!")
        assertEquals(arrayListOf("input"), events.jsonValue())
    }

    @Test
    fun `check to report shift key`() {
        // Don't test on MacOs Firefox
        Assumptions.assumeFalse(isFirefox() && isMac())
        page.navigate("${httpServer.prefixWithDomain}/input/keyboard.html")
        val keyboard = page.keyboard()
        val codeKey = hashMapOf(
            "Shift" to 16,
            "Alt" to 18,
            "Control" to 17
        )
        codeKey.forEach {
            keyboard.down(it.key)
            var expectedValue = "Keydown: ${it.key} ${it.key}Left ${it.value} [${it.key}]"
            var result = page.evaluate("getResult()")
            assertEquals(expectedValue, result)

            keyboard.down("!")
            expectedValue = "Keydown: ! Digit1 49 [${it.key}]"
            result = page.evaluate("getResult()")
            if ("Shift" == it.key) {
                expectedValue += "\nKeypress: ! Digit1 33 33 [${it.key}]"
            }
            assertEquals(expectedValue, result)

            keyboard.up("!")
            expectedValue = "Keyup: ! Digit1 49 [${it.key}]"
            result = page.evaluate("getResult()")
            assertEquals(expectedValue, result)

            keyboard.up(it.key)
            expectedValue = "Keyup: ${it.key} ${it.key}Left ${it.value} []"
            result = page.evaluate("getResult()")
            assertEquals(expectedValue, result)
        }
    }

    @Test
    fun `check to report multiple modifiers`() {
        page.navigate("${httpServer.prefixWithDomain}/input/keyboard.html")
        val keyboard = page.keyboard()
        keyboard.down("Control")
        var expectedResult = "Keydown: Control ControlLeft 17 [Control]"
        var result = page.evaluate("getResult()")
        assertEquals(expectedResult, result)

        keyboard.down("Alt")
        expectedResult = "Keydown: Alt AltLeft 18 [Alt Control]"
        result = page.evaluate("getResult()")
        assertEquals(expectedResult, result)

        keyboard.down(";")
        expectedResult = "Keydown: ; Semicolon 186 [Alt Control]"
        result = page.evaluate("getResult()")
        assertEquals(expectedResult, result)

        keyboard.up(";")
        expectedResult = "Keyup: ; Semicolon 186 [Alt Control]"
        result = page.evaluate("getResult()")
        assertEquals(expectedResult, result)

        keyboard.up("Control")
        expectedResult = "Keyup: Control ControlLeft 17 [Alt]"
        result = page.evaluate("getResult()")
        assertEquals(expectedResult, result)

        keyboard.up("Alt")
        expectedResult = "Keyup: Alt AltLeft 18 []"
        result = page.evaluate("getResult()")
        assertEquals(expectedResult, result)
    }

    @Test
    fun `check to send proper codes while typing`() {
        page.navigate("${httpServer.prefixWithDomain}/input/keyboard.html")
        page.keyboard().type("!")
        var expectedResult = arrayListOf(
            "Keydown: ! Digit1 49 []",
            "Keypress: ! Digit1 33 33 []",
            "Keyup: ! Digit1 49 []"
        ).joinToString(separator = "\n")
        var result = page.evaluate("getResult()")
        assertEquals(expectedResult, result)

        page.keyboard().type("^")
        expectedResult = arrayListOf(
            "Keydown: ^ Digit6 54 []",
            "Keypress: ^ Digit6 94 94 []",
            "Keyup: ^ Digit6 54 []"
        ).joinToString(separator = "\n")
        result = page.evaluate("getResult()")
        assertEquals(expectedResult, result)
    }

    @Test
    fun `check to send proper codes while typing with shift`() {
        page.navigate("${httpServer.prefixWithDomain}/input/keyboard.html")
        val keyboard = page.keyboard()
        keyboard.down("Shift")
        page.keyboard().type("~")
        val expectedResult = arrayListOf(
            "Keydown: Shift ShiftLeft 16 [Shift]",
            "Keydown: ~ Backquote 192 [Shift]",
            "Keypress: ~ Backquote 126 126 [Shift]",
            "Keyup: ~ Backquote 192 [Shift]"
        ).joinToString(separator = "\n")
        val result = page.evaluate("getResult()")
        assertEquals(expectedResult, result)
    }

    @Test
    fun `check to not type canceled events`() {
        page.navigate("${httpServer.prefixWithDomain}/input/textarea.html")
        page.focus("textarea")
        val jsScript = """() => {
            |   window.addEventListener('keydown', event => {
            |       event.stopPropagation();
            |       event.stopImmediatePropagation();
            |       if (event.key == 'l')
            |           event.preventDefault();
            |       if (event.key == 'o')
            |           event.preventDefault();
            |   }, false);
            |}
        """.trimMargin()
        page.evaluate(jsScript)
        page.keyboard().type("Hello World!")
        val result = page.evalOnSelector("textarea", "textarea => textarea.value")
        assertEquals("He Wrd!", result)
    }

    @Test
    fun `check to press plus`() {
        page.navigate("${httpServer.prefixWithDomain}/input/keyboard.html")
        page.keyboard().press("+")
        val expectedResult = arrayListOf(
            "Keydown: + Equal 187 []",
            "Keypress: + Equal 43 43 []",
            "Keyup: + Equal 187 []"
        ).joinToString(separator = "\n")
        val result = page.evaluate("getResult()")
        assertEquals(expectedResult, result)
    }

    @Test
    fun `check to press shift plus`() {
        page.navigate("${httpServer.prefixWithDomain}/input/keyboard.html")
        page.keyboard().press("Shift++")
        val expectedResult = arrayListOf(
            "Keydown: Shift ShiftLeft 16 [Shift]",
            "Keydown: + Equal 187 [Shift]",
            "Keypress: + Equal 43 43 [Shift]",
            "Keyup: + Equal 187 [Shift]",
            "Keyup: Shift ShiftLeft 16 []"
        ).joinToString(separator = "\n")
        val result = page.evaluate("getResult()")
        assertEquals(expectedResult, result)
    }

    @Test
    fun `check to support plus separated modifier`() {
        page.navigate("${httpServer.prefixWithDomain}/input/keyboard.html")
        page.keyboard().press("Shift+~")
        val expectedResult = arrayListOf(
            "Keydown: Shift ShiftLeft 16 [Shift]",
            "Keydown: ~ Backquote 192 [Shift]",
            "Keypress: ~ Backquote 126 126 [Shift]",
            "Keyup: ~ Backquote 192 [Shift]",
            "Keyup: Shift ShiftLeft 16 []"
        ).joinToString(separator = "\n")
        val result = page.evaluate("getResult()")
        assertEquals(expectedResult, result)
    }

    @Test
    fun `check to support multiple plus separated modifier`() {
        page.navigate("${httpServer.prefixWithDomain}/input/keyboard.html")
        page.keyboard().press("Control+Shift+~")
        val expectedResult = arrayListOf(
            "Keydown: Control ControlLeft 17 [Control]",
            "Keydown: Shift ShiftLeft 16 [Control Shift]",
            "Keydown: ~ Backquote 192 [Control Shift]",
            "Keyup: ~ Backquote 192 [Control Shift]",
            "Keyup: Shift ShiftLeft 16 [Control]",
            "Keyup: Control ControlLeft 17 []"
        ).joinToString(separator = "\n")
        val result = page.evaluate("getResult()")
        assertEquals(expectedResult, result)
    }

    @Test
    fun `check to shift raw codes`() {
        page.navigate("${httpServer.prefixWithDomain}/input/keyboard.html")
        page.keyboard().press("Shift+Digit3")
        val expectedResult = arrayListOf(
            "Keydown: Shift ShiftLeft 16 [Shift]",
            "Keydown: # Digit3 51 [Shift]",
            "Keypress: # Digit3 35 35 [Shift]",
            "Keyup: # Digit3 51 [Shift]",
            "Keyup: Shift ShiftLeft 16 []"
        ).joinToString(separator = "\n")
        val result = page.evaluate("getResult()")
        assertEquals(expectedResult, result)
    }

    @Test
    fun `check to specify repeat property`() {
        page.navigate("${httpServer.prefixWithDomain}/input/textarea.html")
        page.focus("textarea")
        val lastEvent = captureLastKeyDown()
        page.keyboard().down("a")
        assertEquals(false, lastEvent.evaluate("e => e.repeat"))

        page.keyboard().press("a")
        assertEquals(true, lastEvent.evaluate("e => e.repeat"))

        page.keyboard().down("b")
        assertEquals(false, lastEvent.evaluate("e => e.repeat"))

        page.keyboard().press("b")
        assertEquals(true, lastEvent.evaluate("e => e.repeat"))

        page.keyboard().up("a")
        page.keyboard().down("a")
        assertEquals(false, lastEvent.evaluate("e => e.repeat"))
    }

    @Test
    fun `check to type all kinds of characters`() {
        page.navigate("${httpServer.prefixWithDomain}/input/textarea.html")
        page.focus("textarea")
        val text = """This text goes onto two lines.
            |This is character is 嗨.
        """.trimMargin()
        page.keyboard().type(text)
        val result = page.evalOnSelector("textarea", "t => t.value")
        assertEquals(text, result)
    }

    @Test
    fun `check to specify location`() {
        page.navigate("${httpServer.prefixWithDomain}/input/textarea.html")
        val lastEvent = captureLastKeyDown()
        val textarea = page.querySelector("textarea")
        assertNotNull(textarea)
        textarea.press("Digit5")
        assertEquals(0, lastEvent.evaluate("e => e.location"))

        textarea.press("ControlLeft")
        assertEquals(1, lastEvent.evaluate("e => e.location"))

        textarea.press("ControlRight")
        assertEquals(2, lastEvent.evaluate("e => e.location"))

        textarea.press("NumpadSubtract")
        assertEquals(3, lastEvent.evaluate("e => e.location"))
    }

    @Test
    fun `check to press enter`() {
        page.setContent("<textarea></textarea>")
        page.focus("textarea")
        val lastEvent = captureLastKeyDown()
        testEnterKey(lastEvent, "Enter", "Enter")
        testEnterKey(lastEvent, "NumpadEnter", "NumpadEnter")
        testEnterKey(lastEvent, "\n", "Enter")
        testEnterKey(lastEvent, "\n", "Enter")
    }

    private fun testEnterKey(lastEventHandle: IJSHandle, key: String, expectedCode: String) {
        page.keyboard().press(key)
        val lastEvent = lastEventHandle.jsonValue() as LinkedHashMap<*, *>
        assertEquals("Enter", lastEvent["key"], lastEvent.toString())
        assertEquals(expectedCode, lastEvent["code"], lastEvent.values.joinToString(separator = ","))

        val value = page.evalOnSelector("textarea", "t => t.value")
        assertEquals("\n", value)
        page.evalOnSelector("textarea", "t => t.value = ''")
    }

    @Test
    fun `check to throw on unknown keys`() {
        testUnknownKey("NotARealKey")
        testUnknownKey("ё")
        testUnknownKey("☺")
    }

    private fun testUnknownKey(key: String) {
        try {
            page.keyboard().press(key)
            fail("press should throw")
        } catch (e: Exception) {
            assertTrue(e.message!!.contains("Unknown key: \"${key}\""))
        }
    }

    @Test
    fun `check to type emoji`() {
        page.navigate("${httpServer.prefixWithDomain}/input/textarea.html")
        val expectedText = "👹 Tokyo street Japan 🇯🇵"
        page.type("textarea", expectedText)
        val result = page.evalOnSelector("textarea", "textarea => textarea.value")
        assertEquals(expectedText, result)
    }

    @Test
    fun `check to type emoji into an iframe`() {
        page.navigate(httpServer.emptyPage)
        attachFrame(page, "emoji-test", "${httpServer.prefixWithDomain}/input/textarea.html")
        val frame = page.frames()[1]
        val textarea = frame.querySelector("textarea")
        assertNotNull(textarea)
        val expectedText = "👹 Tokyo street Japan 🇯🇵"
        textarea.type(expectedText)
        val result = frame.evalOnSelector("textarea", "textarea => textarea.value")
        assertEquals(expectedText, result)
    }

    @Test
    fun `check to handle select all`() {
        page.navigate("${httpServer.prefixWithDomain}/input/textarea.html")
        val textarea = page.querySelector("textarea")
        val expectedText = "my text"
        assertNotNull(textarea)
        textarea.type(expectedText)

        val modifier = if (isMac()) "Meta" else "Control"
        page.keyboard().down(modifier)
        page.keyboard().press("a")
        page.keyboard().up(modifier)
        page.keyboard().press("Backspace")

        val result = page.evalOnSelector("textarea", "textarea => textarea.value") as String
        assertTrue(result.isEmpty())
    }

    @Test
    fun `check to prevent select all`() {
        page.navigate("${httpServer.prefixWithDomain}/input/textarea.html")
        val textarea = page.querySelector("textarea")
        assertNotNull(textarea)
        val expectedText = "my text"
        textarea.type(expectedText)

        val jsScript = """textarea => {
            |   textarea.addEventListener('keydown', event => {
            |       if (event.key === 'a' && (event.metaKey || event.ctrlKey))
            |           event.preventDefault();
            |   }, false);
            |}
        """.trimMargin()
        page.evalOnSelector("textarea", jsScript)
        val modifier = if (isMac()) "Meta" else "Control"
        page.keyboard().down(modifier)
        page.keyboard().press("a")
        page.keyboard().up(modifier)
        page.keyboard().press("Backspace")
        val result = page.evalOnSelector("textarea", "textarea => textarea.value")
        assertEquals(expectedText.substring(0, expectedText.lastIndex), result)
    }

    @Test
    @Description("Test only for MacOs")
    fun `check to support macOs shortcuts`() {
        Assumptions.assumeTrue(isMac())
        Assumptions.assumeFalse(isFirefox())
        page.navigate("${httpServer.prefixWithDomain}/input/textarea.html")
        val textarea = page.querySelector("textarea")
        assertNotNull(textarea)
        val expectedText = "my text"
        textarea.type(expectedText)
        page.keyboard().press("Shift+Control+Alt+KeyB")
        page.keyboard().press("Backspace")
        val result = page.evalOnSelector("textarea", "textarea => textarea.value")
        assertEquals(expectedText, result)
    }

    @Test
    fun `check to press metaKey`() {
        val lastEvent = captureLastKeyDown()
        page.keyboard().press("Meta")
        val eventData = lastEvent.jsonValue() as LinkedHashMap<*, *>
        if (isFirefox() && !isMac()) {
            assertEquals("OS", eventData["key"])
            assertFalse(eventData["metaKey"] as Boolean)
        } else {
            assertEquals("Meta", eventData["key"])
            assertTrue(eventData["metaKey"] as Boolean)
        }
        if (isFirefox()) {
            assertEquals("OSLeft", eventData["code"])
        } else {
            assertEquals("MetaLeft", eventData["code"])
        }
    }

    @Test
    fun `check correct work after a cross origin navigation for keyboard`() {
        page.navigate(httpServer.emptyPage)
        page.navigate("${httpServer.prefixWithIP}/empty.html")
        val lastEvent = captureLastKeyDown()
        page.keyboard().press("a")
        val result = lastEvent.evaluate("l => l.key")
        assertEquals("a", result)
    }

    @Test
    @Description("Test only for WebKit")
    @DisabledIfSystemProperty(named = "browser", matches = "^chromium|firefox")
    fun `check to expose key identifier in web kit`() {
        val lastEvent = captureLastKeyDown()
        val keyMap = hashMapOf(
            "ArrowUp" to "Up",
            "ArrowDown" to "Down",
            "ArrowLeft" to "Left",
            "ArrowRight" to "Right",
            "Backspace" to "U+0008",
            "Tab" to "U+0009",
            "Delete" to "U+007F",
            "a" to "U+0041",
            "b" to "U+0042",
            "F12" to "F12",
        )
        keyMap.forEach {
            page.keyboard().press(it.key)
            val result = lastEvent.evaluate("e => e.keyIdentifier")
            assertEquals(it.value, result)
        }
    }

    @Test
    fun `check to scroll by page down`() {
        page.navigate("${httpServer.prefixWithDomain}/input/scrollable.html")
        page.click("body")
        page.keyboard().press("PageDown")
        page.waitForFunction("() => scrollY > 0")
    }
    //endregion

    //region Route
    @Test
    fun `check to intercept`() {
        val intercept = arrayListOf(false)
        page.route("**/empty.html") {
            val request = it.request()
            assertTrue(request.url().contains("empty.html"))
            assertNotNull(request.headers()["user-agent"])
            assertEquals("GET", request.method())
            assertNull(request.postData())
            assertTrue(request.isNavigationRequest())
            assertEquals("document", request.resourceType())
            assertEquals(request.frame(), page.mainFrame())
            assertEquals("about:blank", request.frame().url())
            it.resume()
            intercept[0] = true
        }
        val response = page.navigate(httpServer.emptyPage)
        assertNotNull(response)
        assertTrue(response.ok())
        assertTrue(intercept[0])
    }

    @Test
    fun `check to unroute`() {
        val intercepted = arrayListOf<Int>()
        val handler1: (IRoute) -> Unit = {
            intercepted.add(1)
            it.resume()
        }
        page.route("**/empty.html", handler1)
        page.route("**/empty.html") {
            intercepted.add(2)
            it.resume()
        }
        page.route("**/empty.html") {
            intercepted.add(3)
            it.resume()
        }
        page.route("**/*") {
            intercepted.add(4)
            it.resume()
        }
        page.navigate(httpServer.emptyPage)
        assertEquals(arrayListOf(1), intercepted)

        intercepted.clear()
        page.unroute("**/empty.html", handler1)
        page.navigate(httpServer.emptyPage)
        assertEquals(arrayListOf(2), intercepted)

        intercepted.clear()
        page.unroute("**/empty.html")
        page.navigate(httpServer.emptyPage)
        assertEquals(arrayListOf(4), intercepted)
    }

    @Test
    fun `check to correct work when post is redirected with 302`() {
        httpServer.setRedirect("/rredirect", "/empty.html")
        page.navigate(httpServer.emptyPage)
        page.route("**/*") { it.resume() }
        val content = """<form action='/rredirect' method='post'>
            |   <input type='hidden' id='foo' name='foo' value='FOOBAR'>
            |</form>
        """.trimMargin()
        page.setContent(content)
        page.waitForNavigation { page.evalOnSelector("form", "form => form.submit()") }
    }

    @Test
    fun `check correct work when header manipulation headers with redirect`() {
        httpServer.setRedirect("/rrredict", "/empty.html")
        page.route("**/*") {
            val headers = it.request().headers() as HashMap
            headers["age"] = "23"
            it.resume(ResumeOptions { it.headers = headers })
        }
        page.navigate("${httpServer.prefixWithDomain}/rrredirect")
    }

    @Test
    fun `check to remove headers`() {
        page.navigate(httpServer.emptyPage)
        page.route("**/*") {
            val headers = it.request().headers() as HashMap
            headers.remove("age")
            it.resume(ResumeOptions { it.headers = headers })
        }

        val serverRequest = httpServer.futureRequest("/title.html")
        page.evaluate("url => fetch(url, { headers: { age: '23'} })", "${httpServer.prefixWithDomain}/title.html")
        assertFalse(serverRequest.get().headers.containsKey("age"))
    }

    @Test
    fun `check to contain referer header`() {
        val requests = arrayListOf<IRequest>()
        page.route("**/*") {
            requests.add(it.request())
            it.resume()
        }
        page.navigate("${httpServer.prefixWithDomain}/page-with-one-style.html")
        assertTrue(requests[1].url().contains("/one-style.css"))
        assertTrue(requests[1].headers().containsKey("referer"))
        assertTrue(requests[1].headers()["referer"]!!.contains("/page-with-one-style.html"))
    }

    @Test
    fun `check to return navigation reponse when url has cookies`() {
        page.navigate(httpServer.emptyPage)
        browserContext.addCookies(singletonList(Cookie {
            it.name = "age"
            it.value = "23"
            it.url = httpServer.emptyPage
        }))
        page.route("**/*") { it.resume() }
        val response = page.reload()
        assertNotNull(response)
        assertEquals(200, response.status())
    }

    @Test
    fun `check to show custom http headers`() {
        page.setExtraHTTPHeaders(mapOf("age" to "23"))
        page.route("**/*") {
            assertEquals("23", it.request().headers()["age"])
            it.resume()
        }
        val response = page.navigate(httpServer.emptyPage)
        assertNotNull(response)
        assertTrue(response.ok())
    }

    @Test
    @DisabledIfSystemProperty(named = "browser", matches = "^\$|webkit")
    fun `check correct work route with redirect inside sync xhr`() {
        page.navigate(httpServer.emptyPage)
        httpServer.setRedirect("/logo.png", "/playwright.png")
        page.route("**/*") { it.resume() }
        val jsScript = """async () => {
            |   const request = new XMLHttpRequest();
            |   request.open('GET', '/logo.png', false);
            |   request.send(null);
            |   return request.status;
            |}
        """.trimMargin()
        val result = page.evaluate(jsScript)
        assertEquals(200, result)
    }

    @Test
    fun `check correct work route with custom referer headers`() {
        page.setExtraHTTPHeaders(mapOf("referer" to httpServer.emptyPage))
        page.route("**/*") {
            assertEquals(httpServer.emptyPage, it.request().headers()["referer"])
            it.resume()
        }
        val response = page.navigate(httpServer.emptyPage)
        assertNotNull(response)
        assertTrue(response.ok())
    }

    @Test
    fun `check to be aborted`() {
        page.route(Pattern.compile(".*\\.css$")) { it.abort() }
        val failed = arrayListOf(false)
        page.onRequestFailed {
            if (it.url().contains(".css"))
                failed[0] = true
        }
        val response = page.navigate("${httpServer.prefixWithDomain}/page-with-one-style.html")
        assertNotNull(response)
        assertTrue(response.ok())
        assertTrue(response.request().failure().isEmpty())
        assertTrue(failed[0])
    }
    //endregion
}