package com.playwright.remote

import com.playwright.remote.base.BaseTest
import com.playwright.remote.core.enums.KeyboardModifier
import com.playwright.remote.engine.handle.element.api.IElementHandle
import com.playwright.remote.engine.handle.js.api.IJSHandle
import com.playwright.remote.engine.options.NewContextOptions
import com.playwright.remote.engine.options.element.TapOptions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.OutputStreamWriter
import java.util.*
import java.util.concurrent.Semaphore
import kotlin.test.assertEquals

class TestTap : BaseTest() {

    @BeforeEach
    private fun beforeTest() {
        browserContext = browser.newContext(NewContextOptions { it.hasTouch = true })
        page = browserContext.newPage()
    }

    @Test
    fun `check to send all of the correct events`() {
        val content = """
            <div id='a' style='background: lightblue; width: 50px; height: 50px'>a</div>
            <div id='b' style='background: pink; width: 50px; height: 50px'>b</div>
        """.trimIndent()
        page.setContent(content)
        page.tap("#a")
        val eventsHandle = trackEvents(page.querySelector("#b")!!)
        page.tap("#b")
        assertEquals(
            listOf(
                "pointerover", "pointerenter",
                "pointerdown", "touchstart",
                "pointerup", "pointerout",
                "pointerleave", "touchend",
                "mouseover", "mouseenter",
                "mousemove", "mousedown",
                "mouseup", "click"
            ), eventsHandle.jsonValue()
        )

    }

    @Test
    fun `check to not send mouse events touchstart is cancelled`() {
        page.setContent("<div style='width: 50px; height: 50px; background: red'>")
        val jsScript = """() => {
            |   // touchstart is not cancelable unless passive is false
            |   document.addEventListener('touchstart', t => t.preventDefault(), {passive: false});
            |}
        """.trimMargin()
        page.evaluate(jsScript)
        val eventsHandle = trackEvents(page.querySelector("div")!!)
        page.tap("div")
        assertEquals(
            listOf(
                "pointerover", "pointerenter",
                "pointerdown", "touchstart",
                "pointerup", "pointerout",
                "pointerleave", "touchend"
            ), eventsHandle.jsonValue()
        )
    }

    @Test
    fun `check to not send mouse events when touched is cancelled`() {
        page.setContent("<div style='width: 50px; height: 50px; background: red'>")
        page.evaluate("() => document.addEventListener('touchend', t => t.preventDefault())")
        val eventsHandle = trackEvents(page.querySelector("div")!!)
        page.tap("div")
        assertEquals(
            listOf(
                "pointerover", "pointerenter",
                "pointerdown", "touchstart",
                "pointerup", "pointerout",
                "pointerleave", "touchend"
            ), eventsHandle.jsonValue()
        )
    }

    @Test
    fun `check to wait for a navigation caused by a tap`() {
        page.navigate(httpServer.emptyPage)
        page.setContent("<a href='/intercept-this.html'>link</a>;")
        val responseWritten = Semaphore(0)
        val events = Collections.synchronizedList(arrayListOf<String>())
        httpServer.setRoute("/intercept-this.html") {
            try {
                Thread.sleep(100)
            } catch (e: InterruptedException) {
                events.add("interrupted")
            }
            it.responseHeaders.add("Content-Type", "application/octet-stream")
            it.sendResponseHeaders(200, 0)
            OutputStreamWriter(it.responseBody).use { wr ->
                wr.write("foo")
            }
            events.add("sent response")
            responseWritten.release()
        }
        page.tap("a")
        events.add("tap finished")
        responseWritten.acquire()
        assertEquals(listOf("sent response", "tap finished"), events)
    }

    @Test
    fun `check correct work with modifiers`() {
        page.setContent("hello world")
        val jsScript = """() => {
            |   window.touchPromise = new Promise(resolve => {
            |       document.addEventListener('touchstart', event => {
            |           resolve(event.altKey);
            |       }, {passive: false});
            |   });
            |}
        """.trimMargin()
        page.evaluate(jsScript)
        page.tap("body", TapOptions { it.modifiers = listOf(KeyboardModifier.ALT) })
        val altKey = page.evaluate("() => window.touchPromise")
        assertEquals(true, altKey)
    }

    @Test
    fun `check to send well formed touch points`() {
        var jsScript = """() => {
            |   window.touchStartPromise = new Promise(resolve => {
            |       document.addEventListener('touchstart', event => {
            |           resolve([...event.touches].map(t => ({
            |               identifier: t.identifier,
            |               clientX: t.clientX,
            |               clientY: t.clientY,
            |               pageX: t.pageX,
            |               pageY: t.pageY,
            |               radiusX: 'radiusX' in t ? t.radiusX : t['webkitRadiusX'],
            |               radiusY: 'radiusY' in t ? t.radiusY : t['webkitRadiusY'],
            |               rotationAngle: 'rotationAngle' in t ? t.rotationAngle : t['webkitRotationAngle'],
            |               force: 'force' in t ? t.force : t['webkitForce'],
            |           })));
            |       }, false);
            |   })
            |}
        """.trimMargin()
        page.evaluate(jsScript)

        jsScript = """() => {
            |   window.touchEndPromise = new Promise(resolve => {
            |       document.addEventListener('touchend', event => {
            |           resolve([...event.touches].map(t => ({
            |               identifier: t.identifier,
            |               clientX: t.clientX,
            |               clientY: t.clientY,
            |               pageX: t.pageX,
            |               pageY: t.pageY,
            |               radiusX: 'radiusX' in t ? t.radiusX : t['webkitRadiusX'],
            |               radiusY: 'radiusY' in t ? t.radiusY : t['webkitRadiusY'],
            |               rotationAngle: 'rotationAngle' in t ? t.rotationAngle : t['webkitRotationAngle'],
            |               force: 'force' in t ? t.force : t['webkitForce'],
            |           })));
            |       }, false);
            |   })
            |}
        """.trimMargin()
        page.evaluate(jsScript)

        page.touchScreen().tap(40.0, 60.0)
        val touchStart = page.evaluate("() => window.touchStartPromise")
        assertEquals(
            listOf(
                mapOf(
                    "clientX" to 40,
                    "clientY" to 60,
                    "force" to 1,
                    "identifier" to 0,
                    "pageX" to 40,
                    "pageY" to 60,
                    "radiusX" to 1,
                    "radiusY" to 1,
                    "rotationAngle" to 0
                )
            ), touchStart
        )
        val touchEnd = page.evaluate("() => window.touchEndPromise")
        assertEquals(emptyList<Any>(), touchEnd)
    }

    private fun trackEvents(target: IElementHandle): IJSHandle {
        val jsScript = """target => {
            |   const events = [];
            |   for (const event of [
            |       'mousedown', 'mouseenter', 'mouseleave', 'mousemove', 'mouseout', 'mouseover', 'mouseup', 'click',
            |       'pointercancel', 'pointerdown', 'pointerenter', 'pointerleave', 'pointermove', 'pointerout', 'pointerover', 'pointerup',
            |       'touchstart', 'touchend', 'touchmove', 'touchcancel'])
            |       target.addEventListener(event, () => events.push(event), false);
            |   return events;
            |}
        """.trimMargin()
        return target.evaluateHandle(jsScript)
    }
}