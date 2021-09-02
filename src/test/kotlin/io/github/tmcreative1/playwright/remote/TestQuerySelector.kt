package io.github.tmcreative1.playwright.remote

import io.github.tmcreative1.playwright.remote.base.BaseTest
import io.github.tmcreative1.playwright.remote.core.exceptions.PlaywrightException
import org.junit.jupiter.api.Test
import kotlin.test.*

class TestQuerySelector : BaseTest() {

    @Test
    fun `check to throw error for non string selector`() {
        try {
            page.querySelector(null)
            fail("querySelector should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("selector: expected string, got undefined"))
        }
    }

    @Test
    fun `check to find element with css selector`() {
        page.setContent("<section>test</section>")
        val element = page.querySelector("css=section")
        assertNotNull(element)
    }

    @Test
    fun `check to find element with text selector`() {
        page.setContent("<section>test</section>")
        val element = page.querySelector("text='test'")
        assertNotNull(element)
    }

    @Test
    fun `check to find element with xpath selector`() {
        page.setContent("<section>test</section>")
        val element = page.querySelector("xpath=/html/body/section")
        assertNotNull(element)
    }

    @Test
    fun `check to return null for non existing element`() {
        val element = page.querySelector("non-existing-element")
        assertNull(element)
    }

    @Test
    fun `check to auto detect xpath selector`() {
        page.setContent("<section>test</section>")
        val element = page.querySelector("//html/body/section")
        assertNotNull(element)
    }

    @Test
    fun `check to auto detect xpath selector with starting parenthesis`() {
        page.setContent("<section>test</section>")
        val element = page.querySelector("(//section)[1]")
        assertNotNull(element)
    }

    @Test
    fun `check to auto detect text selector`() {
        page.setContent("<section>test</section>")
        val element = page.querySelector("'test'")
        assertNotNull(element)
    }

    @Test
    fun `check to auto detect css selector`() {
        page.setContent("<section>test</section>")
        val element = page.querySelector("section")
        assertNotNull(element)
    }

    @Test
    fun `check to support syntax`() {
        page.setContent("<section><div>test</div></section>")
        val element = page.querySelector("css=section >> css=div")
        assertNotNull(element)
    }

    @Test
    fun `check to find elements`() {
        page.setContent("<div>A</div><br/><div>B</div>")
        val elements = page.querySelectorAll("div")
        assertNotNull(elements)
        assertEquals(2, elements.size)
        val results = arrayListOf<Any>()
        for (element in elements) {
            results.add(page.evaluate("e => e.textContent", element))
        }
        assertEquals(listOf<Any>("A", "B"), results)
    }

    @Test
    fun `check to return empty array if nothing is found`() {
        page.navigate(httpServer.emptyPage)
        val elements = page.querySelectorAll("div")
        assertNotNull(elements)
        assertEquals(0, elements.size)
    }

    @Test
    fun `check to find elements with xpath`() {
        page.setContent("<section>test</section>")
        val elements = page.querySelectorAll("xpath=/html/body/section")
        assertNotNull(elements)
        assertNotNull(elements[0])
        assertEquals(1, elements.size)
    }

    @Test
    fun `check to return empty array for non existing element`() {
        val elements = page.querySelectorAll("//html/body/non-existing-element")
        assertEquals(emptyList(), elements)
    }

    @Test
    fun `check to return multiple elements with xpath`() {
        page.setContent("<div></div><div></div>")
        val elements = page.querySelectorAll("xpath=/html/body/div")
        assertNotNull(elements)
        assertEquals(2, elements.size)
    }

    @Test
    fun `check correct work of query selector all with bogus array from`() {
        page.setContent("<div>hello</div><div></div>")
        val jsScript = """() => {
            |   Array.from = () => [];
            |   return document.querySelector('div');
            |}
        """.trimMargin()
        val div = page.evaluateHandle(jsScript)
        val elements = page.querySelectorAll("div")
        assertNotNull(elements)
        assertEquals(2, elements.size)
        assertEquals(true, elements[0].evaluate("(div, div1) => div === div1", div))
    }
}