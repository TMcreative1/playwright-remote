package io.github.tmcreative1.playwright.remote

import io.github.tmcreative1.playwright.remote.base.BaseTest
import io.github.tmcreative1.playwright.remote.engine.page.api.IPage
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TestWheel : BaseTest() {

    @Test
    fun `check to dispatch wheel events`() {
        page.setContent("<div style='width: 5000px; height: 5000px;'></div>")
        page.mouse().move(50.0, 60.0)
        listenForWheelEvents(page)
        page.mouse().wheel(0.0, 100.0)

        val expected = mapOf<String, Any>(
            "deltaX" to 0,
            "deltaY" to 100,
            "clientX" to 50,
            "clientY" to 60,
            "deltaMode" to 0,
            "ctrlKey" to false,
            "shiftKey" to false,
            "altKey" to false,
            "metaKey" to false
        )
        assertEquals(expected, page.evaluate("window.lastEvent"))
        page.waitForFunction("window.scrollY === 100")
    }

    @Test
    fun `check to scroll when nobody is listening`() {
        page.navigate("${httpServer.prefixWithDomain}/input/scrollable.html")
        page.mouse().move(50.0, 60.0)
        page.mouse().wheel(0.0, 100.0)
        page.waitForFunction("window.scrollY === 100")
    }

    @Test
    fun `check to set the modifiers`() {
        page.setContent("<div style='width: 5000px; height: 5000px;'></div>")
        page.mouse().move(50.0, 60.0)
        listenForWheelEvents(page)
        page.keyboard().down("Shift")
        page.mouse().wheel(0.0, 100.0)
        val expected = mapOf<String, Any>(
            "deltaX" to 0,
            "deltaY" to 100,
            "clientX" to 50,
            "clientY" to 60,
            "deltaMode" to 0,
            "ctrlKey" to false,
            "shiftKey" to true,
            "altKey" to false,
            "metaKey" to false
        )
        assertEquals(expected, page.evaluate("window.lastEvent"))
    }

    @Test
    fun `check to scroll horizontally`() {
        page.setContent("<div style='width: 5000px; height: 5000px;'></div>")
        page.mouse().move(50.0, 60.0)
        listenForWheelEvents(page)
        page.mouse().wheel(100.0, 0.0)
        val expected = mapOf<String, Any>(
            "deltaX" to 100,
            "deltaY" to 0,
            "clientX" to 50,
            "clientY" to 60,
            "deltaMode" to 0,
            "ctrlKey" to false,
            "shiftKey" to false,
            "altKey" to false,
            "metaKey" to false
        )
        assertEquals(expected, page.evaluate("window.lastEvent"))
        page.waitForFunction("window.scrollX === 100")
    }

    @Test
    fun `check to work when the event is canceled`() {
        page.setContent("<div style='width: 5000px; height: 5000px;'></div>")
        page.mouse().move(50.0, 60.0)
        listenForWheelEvents(page)
        val jsScript = """() => {
            |   document.querySelector('div').addEventListener('wheel', e => e.preventDefault());
            |}
        """.trimMargin()
        page.evaluate(jsScript)
        page.mouse().wheel(0.0, 100.0)
        val expected = mapOf<String, Any>(
            "deltaX" to 0,
            "deltaY" to 100,
            "clientX" to 50,
            "clientY" to 60,
            "deltaMode" to 0,
            "ctrlKey" to false,
            "shiftKey" to false,
            "altKey" to false,
            "metaKey" to false
        )
        assertEquals(expected, page.evaluate("window.lastEvent"))
        page.waitForTimeout(100.0)
        assertEquals(0, page.evaluate("window.scrollY"))
    }

    private fun listenForWheelEvents(page: IPage) {
        val jsScript = """selector => {
            |   document.querySelector(selector).addEventListener('wheel', e => {
            |       window['lastEvent'] = {
            |           deltaX: e.deltaX,
            |           deltaY: e.deltaY,
            |           clientX: e.clientX,
            |           clientY: e.clientY,
            |           deltaMode: e.deltaMode,
            |           ctrlKey: e.ctrlKey,
            |           shiftKey: e.shiftKey,
            |           altKey: e.altKey,
            |           metaKey: e.metaKey,
            |       };
            |   }, { passive: false });
            |}
        """.trimMargin()
        page.evaluate(jsScript, "div")
    }
}