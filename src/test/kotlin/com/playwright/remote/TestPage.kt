package com.playwright.remote

import com.playwright.remote.base.BaseTest
import com.playwright.remote.core.enums.Media
import com.playwright.remote.core.exceptions.PlaywrightException
import com.playwright.remote.engine.console.api.IConsoleMessage
import com.playwright.remote.engine.frame.impl.Frame
import com.playwright.remote.engine.options.CloseOptions
import com.playwright.remote.engine.options.EmulateMediaOptions
import com.playwright.remote.engine.options.NewPageOptions
import com.playwright.remote.engine.options.ScreenshotOptions
import com.playwright.remote.engine.options.enum.ColorScheme.DARK
import com.playwright.remote.engine.options.enum.ColorScheme.LIGHT
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledIfSystemProperty
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
}