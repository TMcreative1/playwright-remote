package com.playwright.remote.evalselector

import com.playwright.remote.base.BaseTest
import com.playwright.remote.core.exceptions.PlaywrightException
import org.junit.jupiter.api.Test
import kotlin.test.*

class TestEvalOnSelector : BaseTest() {

    @Test
    fun `check correct work with css selector`() {
        page.setContent("<section id='testAttribute'>23</section>")
        val idAttribute = page.evalOnSelector("css=section", "e => e.id")
        assertEquals("testAttribute", idAttribute)
    }

    @Test
    fun `check correct work with id selector`() {
        page.setContent("<section id='testAttribute'>23</section>")
        val idAttribute = page.evalOnSelector("id=testAttribute", "e => e.id")
        assertEquals("testAttribute", idAttribute)
    }

    @Test
    fun `check correct work with data test selector`() {
        page.setContent("<section data-test=age id='testAttribute'>23</section>")
        val idAttribute = page.evalOnSelector("data-test=age", "e => e.id")
        assertEquals("testAttribute", idAttribute)
    }

    @Test
    fun `check correct work with data test id selector`() {
        page.setContent("<section data-testid=age id='testAttribute'>23</section>")
        val idAttribute = page.evalOnSelector("data-testid=age", "e => e.id")
        assertEquals("testAttribute", idAttribute)
    }

    @Test
    fun `check correct work with text selector`() {
        page.setContent("<section id='testAttribute'>23</section>")
        val idAttribute = page.evalOnSelector("text='23'", "e => e.id")
        assertEquals("testAttribute", idAttribute)
    }

    @Test
    fun `check correct work with text selector without single quote`() {
        page.setContent("<section id='testAttribute'>23</section>")
        val idAttribute = page.evalOnSelector("text=23", "e => e.id")
        assertEquals("testAttribute", idAttribute)
    }

    @Test
    fun `check correct work with xpath selector`() {
        page.setContent("<section id='testAttribute'>23</section>")
        val idAttribute = page.evalOnSelector("xpath=/html/body/section", "e => e.id")
        assertEquals("testAttribute", idAttribute)
    }

    @Test
    fun `check correct work with auto detect css selector`() {
        page.setContent("<section id='testAttribute'>23</section>")
        val idAttribute = page.evalOnSelector("section", "e => e.id")
        assertEquals("testAttribute", idAttribute)
    }

    @Test
    fun `check correct work with auto detect css selector with attribute`() {
        page.setContent("<section id='testAttribute'>23</section>")
        val idAttribute = page.evalOnSelector("section[id='testAttribute']", "e => e.id")
        assertEquals("testAttribute", idAttribute)
    }

    @Test
    fun `check correct work with auto detect nested css selector`() {
        page.setContent("<div foo=bar><section>23<span>Hello<div id=target></div></span></section></div>)")
        val idAttribute = page.evalOnSelector("div[foo=bar] > section >> 'Hello' >> div", "e => e.id")
        assertEquals("target", idAttribute)
    }

    @Test
    fun `check to accept arguments`() {
        page.setContent("<section>age</section>")
        val text = page.evalOnSelector("section", "(e, suffix) => e.textContent + suffix", " 23")
        assertEquals("age 23", text)
    }

    @Test
    fun `check to accept element handles as arguments`() {
        page.setContent("<section>hello</section><div> world</div>")
        val divHandle = page.querySelector("div")
        val text = page.evalOnSelector("section", "(e, div) => e.textContent + div.textContent", divHandle)
        assertEquals("hello world", text)
    }

    @Test
    fun `check to throw error if element not found`() {
        try {
            page.evalOnSelector("section", "e => e.id")
            fail("evalOnSelector should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("failed to find element matching selector \"section\""))
        }
    }

    @Test
    fun `check to support syntax`() {
        page.setContent("<section><div>age</div></section>")
        val text = page.evalOnSelector("css=section >> css=div", "(e, suffix) => e.textContent + suffix", " 23")
        assertEquals("age 23", text)
    }

    @Test
    fun `check to support syntax with different engines`() {
        page.setContent("<section><div><span>age</span></div></section>")
        val text = page.evalOnSelector(
            "xpath=/html/body/section >> css=div >> text='age'",
            "(e, suffix) => e.textContent + suffix",
            " 23"
        )
        assertEquals("age 23", text)
    }

    @Test
    fun `check to support spaces with syntax`() {
        page.navigate("${httpServer.prefixWithDomain}/deep-shadow.html")
        val text = page.evalOnSelector(" css = div >>css=div>>css   = span  ", "e => e.textContent")
        assertEquals("Hello from root2", text)
    }

    @Test
    fun `check to not stop at first failure with syntax`() {
        page.setContent("<div><span>Next</span><button>Previous</button><button>Next</button></div>")
        val html = page.evalOnSelector("button >> 'Next'", "e => e.outerHTML")
        assertEquals("<button>Next</button>", html)
    }

    @Test
    fun `check to support capture`() {
        page.setContent("<section><div><span>a</span></div></section><section><div><span>b</span></div></section>")
        assertEquals("<div><span>b</span></div>", page.evalOnSelector("*css=div >> 'b'", "e => e.outerHTML"))
        assertEquals("<div><span>b</span></div>", page.evalOnSelector("section >> *css=div >> 'b'", "e => e.outerHTML"))
        assertEquals("<span>b</span>", page.evalOnSelector("css=div >> *text='b'", "e => e.outerHTML"))
        assertNotNull(page.querySelector("*"))
    }

    @Test
    fun `check to throw error on multiple captures`() {
        try {
            page.evalOnSelector("*css=div >> *css=span", "e => e.outerHTML")
            fail("evalOnSelector should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Only one of the selectors can capture using * modifier"))
        }
    }

    @Test
    fun `check to throw on malformed capture`() {
        try {
            page.evalOnSelector("*=div", "e = e.outerHTML")
            fail("evalOnSelector should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Unknown engine \"\" while parsing selector *=div"))
        }
    }

    @Test
    fun `check correct work with spaces in css attributes`() {
        page.setContent("<div><input placeholder='Select date'></div>")
        assertNotNull(page.waitForSelector("[placeholder=\"Select date\"]"))
        assertNotNull(page.waitForSelector("[placeholder='Select date']"))
        assertNotNull(page.waitForSelector("input[placeholder=\"Select date\"]"))
        assertNotNull(page.waitForSelector("input[placeholder='Select date']"))
        assertNotNull(page.waitForSelector("[placeholder=\"Select date\"]"))
        assertNotNull(page.waitForSelector("[placeholder='Select date']"))
        assertNotNull(page.waitForSelector("input[placeholder=\"Select date\"]"))
        assertNotNull(page.waitForSelector("input[placeholder='Select date']"))

        assertEquals(
            "<input placeholder=\"Select date\">",
            page.evalOnSelector("[placeholder=\"Select date\"]", "e => e.outerHTML")
        )
        assertEquals(
            "<input placeholder=\"Select date\">",
            page.evalOnSelector("[placeholder='Select date']", "e => e.outerHTML")
        )
        assertEquals(
            "<input placeholder=\"Select date\">",
            page.evalOnSelector("input[placeholder=\"Select date\"]", "e => e.outerHTML")
        )
        assertEquals(
            "<input placeholder=\"Select date\">",
            page.evalOnSelector("input[placeholder='Select date']", "e => e.outerHTML")
        )
        assertEquals(
            "<input placeholder=\"Select date\">",
            page.evalOnSelector("css=[placeholder=\"Select date\"]", "e => e.outerHTML")
        )
        assertEquals(
            "<input placeholder=\"Select date\">",
            page.evalOnSelector("css=[placeholder='Select date']", "e => e.outerHTML")
        )
        assertEquals(
            "<input placeholder=\"Select date\">",
            page.evalOnSelector("css=input[placeholder=\"Select date\"]", "e => e.outerHTML")
        )
        assertEquals(
            "<input placeholder=\"Select date\">",
            page.evalOnSelector("css=input[placeholder='Select date']", "e => e.outerHTML")
        )
        assertEquals(
            "<input placeholder=\"Select date\">",
            page.evalOnSelector("div >> [placeholder=\"Select date\"]", "e => e.outerHTML")
        )
        assertEquals(
            "<input placeholder=\"Select date\">",
            page.evalOnSelector("div >> [placeholder='Select date']", "e => e.outerHTML")
        )
    }

    @Test
    fun `check correct work with quotes in css attributes`() {
        assertSelectorsWithQuotes(
            "<div><input placeholder=\"Select&quot;date\"></div>",
            "[placeholder=\"Select\\\"date\"]",
            "[placeholder='Select\"date']"
        )

        assertSelectorsWithQuotes(
            "<div><input placeholder=\"Select &quot; date\"></div>",
            "[placeholder=\"Select \\\" date\"]",
            "[placeholder='Select \" date']"
        )

        assertSelectorsWithQuotes(
            "<div><input placeholder=\"Select&apos;date\"></div>",
            "[placeholder=\"Select'date\"]",
            "[placeholder='Select\\'date']"
        )

        assertSelectorsWithQuotes(
            "<div><input placeholder=\"Select &apos; date\"></div>",
            "[placeholder=\"Select ' date\"]",
            "[placeholder='Select \\' date']"
        )
    }

    @Test
    fun `check correct work with spaces in css attributes when missing`() {
        assertNull(page.querySelector("[placeholder='Select date']"))
        page.setContent("<div><input placeholder='Select date'></div>")
        page.waitForSelector("[placeholder='Select date']")
    }

    @Test
    fun `check correct work with quotes in css attributes when missing`() {
        assertNull(page.querySelector("[placeholder='Select\\\"date']"))
        page.setContent("<div><input placeholder='Select&quot;date'></div>")
        page.waitForSelector("[placeholder='Select\\\"date']")
    }

    @Test
    fun `check to return complex values`() {
        page.setContent("<section id='testAttribute'>23</section>")
        val idAttribute = page.evalOnSelector("css=section", "e => [{ id: e.id }]")
        assertEquals(listOf(mapOf("id" to  "testAttribute")), idAttribute)
    }

    private fun assertSelectorsWithQuotes(content: String, firstSelector: String, secondSelector: String) {
        page.setContent(content)
        assertNotNull(page.querySelector(firstSelector))
        assertNotNull(page.querySelector(secondSelector))
    }
}