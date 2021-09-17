package io.github.tmcreative1.playwright.remote.locator

import io.github.tmcreative1.playwright.remote.base.BaseTest
import org.junit.jupiter.api.Test
import kotlin.streams.toList
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TestLocatorElementHandle : BaseTest() {

    @Test
    fun `check to query existing element`() {
        page.navigate("${httpServer.prefixWithDomain}/playground.html")
        page.setContent("<html><body><div class='second'><div class='inner'>A</div></div></body></html>")
        val html = page.locator("html")
        val second = html.locator(".second")
        val inner = second.locator(".inner")
        val content = page.evaluate("e => e.textContent", inner.elementHandle())
        assertEquals("A", content)
    }

    @Test
    fun `check to query existing elements`() {
        page.setContent("<html><body><div>A</div><br/><div>B</div></body></html>")
        val html = page.locator("html")
        val elements = html.locator("div").elementHandles()
        assertNotNull(elements)
        assertEquals(2, elements.size)
        val texts = elements.stream().map { page.evaluate("e => e.textContent", it) }.toList()
        assertEquals(listOf("A", "B"), texts)
    }

    @Test
    fun `check to return empty array of non existing elements`() {
        page.setContent("<html><body><span>A</span><br/><span>B</span></body></html>")
        val html = page.locator("html")
        val elements = html.locator("div").elementHandles()
        assertNotNull(elements)
        assertEquals(0, elements.size)
    }


    @Test
    fun `check to find element by xpath`() {
        page.navigate("${httpServer.prefixWithDomain}/playground.html")
        page.setContent("<html><body><div class='second'><div class='inner'>A</div></div></body></html>")
        val html = page.locator("html")
        val second = html.locator("xpath=./body/div[contains(@class, 'second')]")
        val inner = second.locator("xpath=./div[contains(@class, 'inner')]")
        val content = page.evaluate("e => e.textContent", inner.elementHandle())
        assertEquals("A", content)
    }

    @Test
    fun `check to return null for non existing element by xpath`() {
        page.setContent("<html><body><div class='second'><div class='inner'>B</div></div></body></html>")
        val html = page.locator("html")
        val second = html.locator("xpath=/div[contains(@class, 'third')]").elementHandles()
        assertEquals(listOf(), second)
    }
}