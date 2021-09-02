package io.github.tmcreative1.playwright.remote

import io.github.tmcreative1.playwright.remote.base.BaseTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TestDispatchEvent : BaseTest() {

    @Test
    fun `check to dispatch click event`() {
        page.navigate("${httpServer.prefixWithDomain}/input/button.html")
        page.dispatchEvent("button", "click")
        assertEquals("Clicked", page.evaluate("() => window['result']"))
    }

    @Test
    fun `check to dispatch click event properties`() {
        page.navigate("${httpServer.prefixWithDomain}/input/button.html")
        page.dispatchEvent("button", "click")
        assertNotNull(page.evaluate("bubbles"))
        assertNotNull(page.evaluate("cancelable"))
        assertNotNull(page.evaluate("composed"))
    }

    @Test
    fun `check to dispatch click svg`() {
        val content = """<svg height='100' width='100'>
            |   <circle onclick='javascript:window.__CLICKED=23' cx='50' cy='50' r='40' stroke='black' stroke-width='3' fill='red' />
            |</svg>
        """.trimMargin()
        page.setContent(content)
        page.dispatchEvent("circle", "click")
        assertEquals(23, page.evaluate("() => window['__CLICKED']"))
    }

    @Test
    fun `check to dispatch click on a span with an inline element inside`() {
        val content = """<style>
            |span::before {
            |   content: 'q';
            |}
            |</style>
            |<span onclick='javascript:window.CLICKED=23'></span>
        """.trimMargin()
        page.setContent(content)
        page.dispatchEvent("span", "click")
        assertEquals(23, page.evaluate("() => window['CLICKED']"))
    }

    @Test
    fun `check to dispatch click after navigation`() {
        page.navigate("${httpServer.prefixWithDomain}/input/button.html")
        page.dispatchEvent("button", "click")
        page.navigate("${httpServer.prefixWithDomain}/input/button.html")
        page.dispatchEvent("button", "click")
        assertEquals("Clicked", page.evaluate("() => window['result']"))
    }

    @Test
    fun `check to dispatch click after a cross origin navigation`() {
        page.navigate("${httpServer.prefixWithDomain}/input/button.html")
        page.dispatchEvent("button", "click")
        page.navigate("${httpServer.prefixWithIP}/input/button.html")
        page.dispatchEvent("button", "click")
        assertEquals("Clicked", page.evaluate("() => window['result']"))
    }

    @Test
    fun `check to not fail when element is blocked on hover`() {
        val content = """<style>
            |   container { display: block; position: relative; width: 200px; height: 50px; }
            |   div, button { position: absolute; left: 0; top: 0; bottom: 0; right: 0; }
            |   div { pointer-events: none; }
            |   container:hover div { pointer-events: auto; background: red; }
            |</style>
            |<container>
            |   <button onclick='window.clicked=true'>Click me</button>
            |   <div></div>
            |</container>
        """.trimMargin()
        page.setContent(content)
        page.dispatchEvent("button", "click")
        assertNotNull(page.evaluate("() => window['clicked']"))
    }

    @Test
    fun `check to dispatch click when node is added in shadow dom`() {
        page.navigate(httpServer.emptyPage)
        var jsScript = """() => {
            |   const div = document.createElement('div');
            |   div.attachShadow({mode: 'open'});
            |   document.body.appendChild(div);
            |}
        """.trimMargin()
        page.evaluate(jsScript)
        page.evaluate("() => new Promise(f => setTimeout(f, 100))")
        jsScript = """() => {
            |   const span = document.createElement('span');
            |   span.textContent = 'Hello from shadow';
            |   span.addEventListener('click', () => window['clicked'] = true);
            |   document.querySelector('div').shadowRoot.appendChild(span);
            |}
        """.trimMargin()
        page.evaluate(jsScript)
        page.dispatchEvent("span", "click")
        assertEquals(true, page.evaluate("() => window['clicked']"))
    }

    @Test
    fun `check to dispatch drag drop events`() {
        page.navigate("${httpServer.prefixWithDomain}/drag-n-drop.html")
        val dataTransfer = page.evaluateHandle("() => new DataTransfer()")
        page.dispatchEvent("#source", "dragstart", mapOf("dataTransfer" to dataTransfer))
        page.dispatchEvent("#target", "drop", mapOf("dataTransfer" to dataTransfer))
        val source = page.querySelector("#source")
        val target = page.querySelector("#target")
        val jsScript = """({source, target}) => {
            |   return source.parentElement === target;
            |}
        """.trimMargin()
        assertEquals(true, page.evaluate(jsScript, mapOf("source" to source, "target" to target)))
    }

    @Test
    fun `check to dispatch drag drop events on handle`() {
        page.navigate("${httpServer.prefixWithDomain}/drag-n-drop.html")
        val dataTransfer = page.evaluateHandle("() => new DataTransfer()")
        val source = page.querySelector("#source")
        assertNotNull(source)
        source.dispatchEvent("dragstart", mapOf("dataTransfer" to dataTransfer))
        val target = page.querySelector("#target")
        assertNotNull(target)
        target.dispatchEvent("drop", mapOf("dataTransfer" to dataTransfer))
        val jsScript = """({source, target}) => {
            |   return source.parentElement === target;
            |}
        """.trimMargin()
        assertEquals(true, page.evaluate(jsScript, mapOf("source" to source, "target" to target)))
    }
}