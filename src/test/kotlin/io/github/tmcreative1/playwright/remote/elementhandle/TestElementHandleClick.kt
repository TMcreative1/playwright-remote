package com.playwright.remote.elementhandle

import com.playwright.remote.base.BaseTest
import com.playwright.remote.core.exceptions.PlaywrightException
import com.playwright.remote.engine.options.element.ClickOptions
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

class TestElementHandleClick : BaseTest() {

    @Test
    fun `check correct work`() {
        page.navigate("${httpServer.prefixWithDomain}/input/button.html")
        val button = page.querySelector("button")
        assertNotNull(button)
        button.click()
        assertEquals("Clicked", page.evaluate("() => window['result']"))
    }

    @Test
    fun `check correct work with node removed`() {
        page.navigate("${httpServer.prefixWithDomain}/input/button.html")
        page.evaluate("() => delete window['Node']")
        val button = page.querySelector("button")
        assertNotNull(button)
        button.click()
        assertEquals("Clicked", page.evaluate("() => window['result']"))
    }

    @Test
    fun `check correct work with shadow dom`() {
        page.navigate("${httpServer.prefixWithDomain}/shadow.html")
        val button = page.evaluateHandle("() => window['button']").asElement()
        assertNotNull(button)
        button.click()
        assertEquals(true, page.evaluate("clicked"))
    }

    @Test
    fun `check correct work with text nodes`() {
        page.navigate("${httpServer.prefixWithDomain}/input/button.html")
        val button = page.evaluateHandle("() => document.querySelector('button').firstChild").asElement()
        assertNotNull(button)
        button.click()
        assertEquals("Clicked", page.evaluate("() => window['result']"))
    }

    @Test
    fun `check to throw for detached nodes`() {
        page.navigate("${httpServer.prefixWithDomain}/input/button.html")
        val button = page.querySelector("button")
        assertNotNull(button)
        page.evaluate("button => button.remove()", button)
        try {
            button.click()
            fail("click should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Element is not attached to the DOM"))
        }
    }

    @Test
    fun `check to throw for hidden nodes with force`() {
        page.navigate("${httpServer.prefixWithDomain}/input/button.html")
        val button = page.querySelector("button")
        assertNotNull(button)
        page.evaluate("button => button.style.display = 'none'", button)
        try {
            button.click(ClickOptions { it.force = true })
            fail("click should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Element is not visible"))
        }
    }

    @Test
    fun `check to throw for br element with force`() {
        page.setContent("hello <br> goodbye")
        val br = page.querySelector("br")
        assertNotNull(br)
        try {
            br.click(ClickOptions { it.force = true })
            fail("click should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Element is outside of the viewport"))
        }
    }

    @Test
    fun `check to double click the button`() {
        page.navigate("${httpServer.prefixWithDomain}/input/button.html")
        val jsScript = """() => {
            |   window['double'] = false;
            |   const button = document.querySelector('button');
            |   button.addEventListener('dblclick', event => {
            |       window['double'] = true;
            |   });
            |}
        """.trimMargin()
        page.evaluate(jsScript)
        val button = page.querySelector("button")
        assertNotNull(button)
        button.doubleClick()
        assertEquals(true, page.evaluate("double"))
        assertEquals("Clicked", page.evaluate("result"))
    }
}