package io.github.tmcreative1.playwright.remote.elementhandle

import io.github.tmcreative1.playwright.remote.base.BaseTest
import io.github.tmcreative1.playwright.remote.core.enums.LoadState
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TestElementHandleQuerySelector : BaseTest() {

    @Test
    fun `check correct work`() {
        page.navigate("${httpServer.prefixWithDomain}/playground.html")
        page.setContent("<html><body><div class=\"second\"><div class=\"inner\">A</div></div></body></html>")
        val html = page.querySelector("html")
        assertNotNull(html)
        val second = html.querySelector(".second")
        assertNotNull(second)
        val inner = second.querySelector(".inner")
        val content = page.evaluate("e => e.textContent", inner)
        assertEquals("A", content)
    }

    @Test
    fun `check to return null for non existing element`() {
        page.setContent("<html><body><div class=\"second\"><div class=\"inner\">B</div></div></body></html>")
        val html = page.querySelector("html")
        assertNotNull(html)
        val second = html.querySelector(".third")
        assertNull(second)
    }

    @Test
    fun `check correct work adopted elements`() {
        page.navigate(httpServer.emptyPage)
        val popup =
            page.waitForPopup { page.evaluate("url => window['__popup'] = window.open(url)", httpServer.emptyPage) }
        var jsScript = """() => {
            |   const div = document.createElement('div');
            |   document.body.appendChild(div);
            |   const span = document.createElement('span');
            |   span.textContent = 'hello';
            |   div.appendChild(span);
            |   return div;
            |}
        """.trimMargin()
        val divHandle = page.evaluateHandle(jsScript)
        assertNotNull(divHandle.asElement()!!.querySelector("span"))
        assertEquals("hello", divHandle.asElement()!!.querySelector("span")!!.evaluate("e => e.textContent"))
        assertNotNull(popup)

        popup.waitForLoadState(LoadState.DOMCONTENTLOADED)
        jsScript = """() => {
            |   const div = document.querySelector('div');
            |   window['__popup'].document.body.appendChild(div);
            |}
        """.trimMargin()
        page.evaluate(jsScript)
        assertNotNull(divHandle.asElement()!!.querySelector("span"))
        assertEquals("hello", divHandle.asElement()!!.querySelector("span")!!.evaluate("e => e.textContent"))
        assertNotNull(popup.querySelector("span"))
        assertEquals("hello", popup.querySelector("span")!!.evaluate("e => e.textContent"))
    }

    @Test
    fun `check correct work of query`() {
        page.setContent("<html><body><div>A</div><br/><div>B</div></body></html>")
        val html = page.querySelector("html")
        assertNotNull(html)
        val elements = html.querySelectorAll("div")
        assertNotNull(elements)
        assertEquals(2, elements.size)
        val result = arrayListOf<String>()
        elements.forEach { result.add((page.evaluate("e => e.textContent", it) as String)) }
        assertEquals(listOf("A", "B"), result)
    }

    @Test
    fun `check to return empty array for non existing element`() {
        page.setContent("<html><body><span>A</span><br/><span>B</span></body></html>")
        val html = page.querySelector("html")
        assertNotNull(html)
        val elements = html.querySelectorAll("div")
        assertNotNull(elements)
        assertEquals(0, elements.size)
    }

    @Test
    fun `check to find elements by xpath`() {
        page.navigate("${httpServer.prefixWithDomain}/playground.html")
        page.setContent("<html><body><div class=\"second\"><div class=\"inner\">A</div></div></body></html>")
        val html = page.querySelector("html")
        assertNotNull(html)
        val second = html.querySelectorAll("xpath=./body/div[contains(@class, 'second')]")
        assertNotNull(second)
        val inner = second[0].querySelectorAll("xpath=./div[contains(@class, 'inner')]")
        assertNotNull(inner)
        val content = page.evaluate("e => e.textContent", inner[0])
        assertEquals("A", content)
    }

    @Test
    fun `check to return null for non existing elements by xpath`() {
        page.setContent("<html><body><div class=\"second\"><div class=\"inner\">B</div></div></body></html>")
        val html = page.querySelector("html")
        assertNotNull(html)
        val second = html.querySelectorAll("xpath=/div[contains(@class, 'third')]")
        assertNotNull(second)
        assertTrue(second.isEmpty())
    }
}