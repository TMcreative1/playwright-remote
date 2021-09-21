package io.github.tmcreative1.playwright.remote.page

import io.github.tmcreative1.playwright.remote.base.BaseTest
import io.github.tmcreative1.playwright.remote.engine.console.api.IConsoleMessage
import io.github.tmcreative1.playwright.remote.engine.options.wait.WaitForConsoleMessageOptions
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestPageEventConsole : BaseTest() {

    @Test
    fun `check correct work`() {
        val event = arrayListOf<IConsoleMessage?>(null)
        page.onConsoleMessage { event[0] = it }
        val message = page.waitForConsoleMessage { page.evaluate("() => console.log('hello', 5, {foo: 'bar'});") }
        assertNotNull(message)
        if (isFirefox()) {
            assertEquals("hello 5 JSHandle@object", message.text())
        } else {
            assertEquals("hello 5 {foo: bar}", message.text())
        }
        assertEquals("log", message.type())
        assertEquals("hello", message.args()[0].jsonValue())
        assertEquals(5, message.args()[1].jsonValue())
        assertEquals(mapOf("foo" to "bar"), message.args()[2].jsonValue())
        assertEquals(message, event[0])
    }

    @Test
    fun `check to emit same log twice`() {
        val messages = arrayListOf<String>()
        page.onConsoleMessage { messages.add(it.text()) }
        page.evaluate("() => { for (let i = 0; i < 2; ++i) console.log('hello'); }")
        assertEquals(listOf("hello", "hello"), messages)
    }

    @Test
    fun `check correct work for different console api calls`() {
        val messages = arrayListOf<IConsoleMessage>()
        page.onConsoleMessage { messages.add(it) }
        val jsScript = """() => {
            |   // A pair of time/timeEnd generates only one Console API call.
            |   console.time('calling console.time');
            |   console.timeEnd('calling console.time');
            |   console.trace('calling console.trace');
            |   console.dir('calling console.dir');
            |   console.warn('calling console.warn');
            |   console.error('calling console.error');
            |   console.log(Promise.resolve('should not wait until resolved!'));
            |}
        """.trimMargin()
        page.evaluate(jsScript)
        assertEquals(listOf("timeEnd", "trace", "dir", "warning", "error", "log"), messages.map { it.type() }.toList())
        assertTrue(messages[0].text().contains("calling console.time"))

        assertEquals(
            listOf(
                "calling console.trace",
                "calling console.dir",
                "calling console.warn",
                "calling console.error",
                "Promise"
            ),
            messages.subList(1, messages.size).map { it.text() }.toList()
        )
    }

    @Test
    fun `check to not fail for window object`() {
        val message = page.waitForConsoleMessage { page.evaluate("console.error(window)") }
        assertNotNull(message)
        assertEquals(if (isFirefox()) "JSHandle@object" else "Window", message.text())
    }

    @Test
    fun `check to trigger correct log`() {
        page.navigate("about:blank")
        val message = page.waitForConsoleMessage {
            page.evaluate("async url => fetch(url).catch(e => {})", httpServer.emptyPage)
        }
        assertNotNull(message)
        assertTrue(message.text().contains("Access-Control-Allow-Origin"))
        assertEquals("error", message.type())
    }

    @Test
    fun `check to have location for console api calls`() {
        page.navigate(httpServer.emptyPage)
        val message = page.waitForConsoleMessage(WaitForConsoleMessageOptions {
            it.predicate = { p -> "log from console" == p.text() }
        }) { page.navigate("${httpServer.prefixWithDomain}/console-log.html") }
        assertNotNull(message)
        assertEquals("log", message.type())
        assertTrue(message.location().startsWith("${httpServer.prefixWithDomain}/console-log.html:8:"))
    }

    @Test
    fun `check to support predicate`() {
        page.navigate(httpServer.emptyPage)
        val message = page.waitForConsoleMessage(WaitForConsoleMessageOptions {
            it.predicate = { p -> "info" == p.type() }
        }) {
            page.evaluate("console.log(1)")
            page.evaluate("console.info(2)")
        }
        assertNotNull(message)
        assertEquals("2", message.text())
        assertEquals("info", message.type())
    }
}