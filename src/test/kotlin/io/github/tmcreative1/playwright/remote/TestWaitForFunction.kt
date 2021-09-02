package io.github.tmcreative1.playwright.remote

import io.github.tmcreative1.playwright.remote.base.BaseTest
import io.github.tmcreative1.playwright.remote.core.exceptions.PlaywrightException
import io.github.tmcreative1.playwright.remote.core.exceptions.TimeoutException
import io.github.tmcreative1.playwright.remote.engine.options.wait.WaitForFunctionOptions
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

class TestWaitForFunction : BaseTest() {

    @Test
    fun `check correct work with timeout`() {
        val startTime = Instant.now()
        val timeout = 42.0
        page.waitForTimeout(timeout)
        assertTrue(Duration.between(startTime, Instant.now()).toMillis() > timeout / 2)
    }

    @Test
    fun `check to accept a string`() {
        page.evaluate("() => window['__FOO'] = 1")
        val element = page.waitForFunction("window.__FOO == 1")
        val result = element.evaluate("window.__FOO")
        assertEquals(1, result)
    }

    @Test
    fun `check correct work when resolved right before execution context disposal`() {
        page.addInitScript("window['__RELOADED'] = true")
        page.waitForFunction(
            """() => {
            |   if  (!window['__RELOADED'])
            |       window.location.reload();
            |   return true;
            |}
        """.trimMargin()
        )
    }

    @Test
    fun `check to poll on interval`() {
        val polling = 100.0
        val jsScript = """() => {
            |   if (!window["__startTime"]) {
            |       window["__startTime"] = Date.now();
            |       return false;
            |   }
            |   return Date.now() - window["__startTime"];
            |}
        """.trimMargin()
        val timeDelta = page.waitForFunction(jsScript, null, WaitForFunctionOptions { it.pollingInterval = polling })
        val delta = timeDelta.evaluate("h => h") as Int
        assertTrue(delta >= polling)
    }

    @Test
    fun `check to avoid side effects after timeout`() {
        val counter = arrayOf(0)
        page.onConsoleMessage { counter[0]++ }

        try {
            val jsScript = """() => {
                |   window['counter'] = (window['counter'] || 0) + 1;
                |   console.log(window['counter']);
                |}
            """.trimMargin()
            page.waitForFunction(jsScript, null, WaitForFunctionOptions {
                it.pollingInterval = 1.0
                it.timeout = 1000.0
            })
            fail("waitForFunction should throw")
        } catch (e: TimeoutException) {
            assertTrue(e.message!!.contains("Timeout 1000ms exceeded"))
        }

        val savedCounter = counter[0]
        page.waitForTimeout(2000.0)
        assertEquals(savedCounter, counter[0])
    }

    @Test
    fun `check to poll on raf`() {
        page.evaluate("() => window['__FOO'] = 'hit'")
        page.waitForFunction("() => window['__FOO'] === 'hit'", null, WaitForFunctionOptions {})
    }

    @Test
    fun `check to fail with predicate throwing on first call`() {
        try {
            page.waitForFunction("() => { throw new Error('my error'); }")
            fail("waitForFunction should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("my error"))
        }
    }

    @Test
    fun `check to fail with predicate throwing sometimes`() {
        try {
            val jsScript = """() => {
                |   window['counter'] = (window['counter'] || 0) + 1;
                |   if (window['counter'] === 3)
                |       throw new Error('Fail!');
                |   return window['counter'] === 5 ? 'result' : false;
                |}
            """.trimMargin()
            page.waitForFunction(jsScript)
            fail("waitForFunction should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Fail!"))
        }
    }

    @Test
    fun `check to fail with reference error on wrong page`() {
        try {
            page.waitForFunction("() => globalVar === 123")
            fail("waitForFunction should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("globalVar"))
        }
    }

    @Test
    fun `check correct work with strict CSP policy`() {
        httpServer.setCSP("/empty.html", "script-src ${httpServer.prefixWithDomain}")
        page.navigate(httpServer.emptyPage)

        page.evaluate("() => window['__FOO'] = 'hit'")
        page.waitForFunction("() => window['__FOO'] === 'hit'")
    }

    @Test
    fun `check to throw negative polling interval`() {
        try {
            page.waitForFunction("() => !!document.body", null, WaitForFunctionOptions { it.pollingInterval = -10.0 })
            fail("waitForFunction should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Cannot poll with non-positive interval"))
        }
    }

    @Test
    fun `check to return the success value as a js handle`() {
        assertEquals(5, page.waitForFunction("5").jsonValue())
    }

    @Test
    fun `check to return the window as a success value`() {
        assertNotNull(page.waitForFunction("() => window"))
    }

    @Test
    fun `check to accept element handle arguments`() {
        page.setContent("<div></div>")
        val div = page.querySelector("div")
        page.evaluate("element => element.remove()", div)
        page.waitForFunction("element => !element.parentElement", div)
    }

    @Test
    fun `check correct work wait for function with timeout`() {
        try {
            page.waitForFunction("false", null, WaitForFunctionOptions { it.timeout = 10.0 })
            fail("waitForFunction should throw")
        } catch (e: TimeoutException) {
            assertTrue(e.message!!.contains("Timeout 10ms exceeded"))
        }
    }

    @Test
    fun `check correct work with default timeout`() {
        page.setDefaultTimeout(1.0)
        try {
            page.waitForFunction("false")
            fail("waitForFunction should throw")
        } catch (e: TimeoutException) {
            assertTrue(e.message!!.contains("Timeout 1ms exceeded"))
        }
    }

    @Test
    fun `check to disable timeout when it set to zero`() {
        val jsScript = """() => {
            |   window['__counter'] = (window['__counter'] || 0) + 1;
            |   return window['__counter'] > 10;
            |}
        """.trimMargin()
        page.waitForFunction(jsScript, null, WaitForFunctionOptions {
            it.timeout = 0.0
            it.pollingInterval = 10.0
        })
    }

    @Test
    fun `check to survive cross process navigation`() {
        page.navigate(httpServer.emptyPage)
        page.reload()
        page.navigate("${httpServer.prefixWithIP}/grid.html")
        page.evaluate("() => window['__FOO'] = 1")
        val result = page.waitForFunction("window.__FOO === 1")
        assertNotNull(result)
    }

    @Test
    fun `check to survive navigations`() {
        page.navigate(httpServer.emptyPage)
        page.navigate("${httpServer.prefixWithDomain}/console-log.html")
        page.evaluate("() => window['__done'] = true")
        page.waitForFunction("() => window['__done']")
    }

    @Test
    fun `check correct work with multiline body`() {
        val result = page.waitForFunction("\n () => true\n")
        assertEquals(true, result.jsonValue())
    }

    @Test
    fun `check to wait for predicate with arguments`() {
        page.waitForFunction("({arg1, arg2}) => arg1 + arg2 === 3", mapOf("arg1" to 1, "arg2" to 2))
    }

    @Test
    fun `check to not be called after finishing successfully`() {
        page.navigate(httpServer.emptyPage)
        val messages = arrayListOf<String>()
        page.onConsoleMessage {
            if (it.text().startsWith("waitForFunction")) {
                messages.add(it.text())
            }
        }
        var jsScript = """() => {
            |   console.log('waitForFunction1');
            |   return true;
            |}
        """.trimMargin()
        page.waitForFunction(jsScript)
        page.reload()

        jsScript = """() => {
            |   console.log('waitForFunction2');
            |   return true;
            |}
        """.trimMargin()
        page.waitForFunction(jsScript)
        page.reload()

        jsScript = """() => {
            |   console.log('waitForFunction3');
            |   return true;
            |}
        """.trimMargin()
        page.waitForFunction(jsScript)
        assertEquals(listOf("waitForFunction1", "waitForFunction2", "waitForFunction3"), messages)
    }

    @Test
    fun `check to not be called after finishing unsuccessfully`() {
        page.navigate(httpServer.emptyPage)
        val messages = arrayListOf<String>()
        page.onConsoleMessage {
            if (it.text().startsWith("waitForFunction")) {
                messages.add(it.text())
            }
        }
        val jsScript: String
        try {
            jsScript = """() => {
                |   console.log('waitForFunction1');
                |   throw new Error('waitForFunction1');
                |}
            """.trimMargin()
            page.waitForFunction(jsScript)
            fail("waitForFunction should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("waitForFunction1"))
        }
        page.reload()

        try {
            jsScript = """() => {
                |   console.log('waitForFunction2');
                |   throw new Error('waitForFunction2');
                |}
            """.trimMargin()
            page.waitForFunction(jsScript)
            fail("waitForFunction should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("waitForFunction2"))
        }
        page.reload()

        try {
            jsScript = """() => {
                |   console.log('waitForFunction3');
                |   throw new Error('waitForFunction3');
                |}
            """.trimMargin()
            page.waitForFunction(jsScript)
            fail("waitForFunction should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("waitForFunction3"))
        }
        assertEquals(listOf("waitForFunction1", "waitForFunction2", "waitForFunction3"), messages)
    }
}