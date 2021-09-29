package io.github.tmcreative1.playwright.remote.elementhandle

import io.github.tmcreative1.playwright.remote.base.BaseTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TestElementHandleMisc : BaseTest() {

    @Test
    fun `check correct work of hover`() {
        page.navigate("${httpServer.prefixWithDomain}/input/scrollable.html")
        val button = page.querySelector("#button-6")
        assertNotNull(button)
        button.hover()
        assertEquals("button-6", page.evaluate("document.querySelector('button:hover').id"))
    }

    @Test
    fun `check to hover when node is removed`() {
        page.navigate("${httpServer.prefixWithDomain}/input/scrollable.html")
        page.evaluate("() => delete window['Node']")
        val button = page.querySelector("#button-6")
        assertNotNull(button)
        button.hover()
        assertEquals("button-6", page.evaluate("document.querySelector('button:hover').id"))
    }

    @Test
    fun `check to fill input`() {
        page.navigate("${httpServer.prefixWithDomain}/input/textarea.html")
        val handle = page.querySelector("input")
        assertNotNull(handle)
        handle.fill("my value")
        assertEquals("my value", page.evaluate("window['result']"))
    }

    @Test
    fun `check to fill input when node is removed`() {
        page.navigate("${httpServer.prefixWithDomain}/input/textarea.html")
        page.evaluate("() => delete window['Node']")
        val handle = page.querySelector("input")
        assertNotNull(handle)
        handle.fill("my value")
        assertEquals("my value", page.evaluate("window['result']"))
    }

    @Test
    fun `check to check the box`() {
        page.setContent("<input id='checkbox' type='checkbox'></input>")
        val input = page.querySelector("input")
        assertNotNull(input)
        input.check()
        assertEquals(true, page.evaluate("checkbox.checked"))
    }

    @Test
    fun `check to uncheck the box`() {
        page.setContent("<input id='checkbox' type='checkbox' checked></input>")
        val input = page.querySelector("input")
        assertNotNull(input)
        input.uncheck()
        assertEquals(false, page.evaluate("checkbox.checked"))
    }

    @Test
    fun `check to select single option`() {
        page.navigate("${httpServer.prefixWithDomain}/input/select.html")
        val select = page.querySelector("select")
        assertNotNull(select)
        select.selectOption("blue")
        assertEquals(listOf("blue"), page.evaluate("window['result'].onInput"))
        assertEquals(listOf("blue"), page.evaluate("window['result'].onChange"))
    }

    @Test
    fun `check to focus a button`() {
        page.navigate("${httpServer.prefixWithDomain}/input/button.html")
        val button = page.querySelector("button")
        assertNotNull(button)
        assertEquals(false, button.evaluate("button => document.activeElement === button"))
        button.focus()
        assertEquals(true, button.evaluate("button => document.activeElement === button"))
    }
}