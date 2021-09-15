package io.github.tmcreative1.playwright.remote.evalselector

import io.github.tmcreative1.playwright.remote.base.BaseTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TestEvalOnSelectorAll : BaseTest() {

    @Test
    fun `check correct work with css selector`() {
        page.setContent("<div>hello</div><div>beautiful</div><div>world!</div>")
        val divCount = page.evalOnSelectorAll("css=div", "divList => divList.length")
        assertEquals(3, divCount)
    }

    @Test
    fun `check correct work with text selector`() {
        page.setContent("<div>hello</div><div>beautiful</div><div>beautiful</div><div>world!</div>")
        val divCount = page.evalOnSelectorAll("text='beautiful'", "divList => divList.length")
        assertEquals(2, divCount)
    }

    @Test
    fun `check correct work with xpath selector`() {
        page.setContent("<div>hello</div><div>beautiful</div><div>world!</div>")
        val divCount = page.evalOnSelectorAll("xpath=/html/body/div", "divList => divList.length")
        assertEquals(3, divCount)
    }

    @Test
    fun `check to auto detect css selector`() {
        page.setContent("<div>hello</div><div>beautiful</div><div>world!</div>")
        val divCount = page.evalOnSelectorAll("div", "divList => divList.length")
        assertEquals(3, divCount)
    }

    @Test
    fun `check to support syntax`() {
        page.setContent("<div><span>hello</span></div><div>beautiful</div><div><span>wo</span><span>rld!</span></div><span>Not this one</span>")
        val spanCount = page.evalOnSelectorAll("css=div >> css=span", "spanList => spanList.length")
        assertEquals(3, spanCount)
    }

    @Test
    fun `check to support capture`() {
        page.setContent("<section><div><span>a</span></div></section><section><div><span>b</span></div></section>")
        assertEquals(1, page.evalOnSelectorAll("*css=div >> 'b'", "els => els.length"))
        assertEquals(1, page.evalOnSelectorAll("section >> *css=div >> 'b'", "els => els.length"))
        assertEquals(4, page.evalOnSelectorAll("section >> *", "els => els.length"))

        page.setContent("<section><div><span>a</span><span>a</span></div></section>")
        assertEquals(1, page.evalOnSelectorAll("*css=div >> 'a'", "els => els.length"))
        assertEquals(1, page.evalOnSelectorAll("section >> *css=div >> 'a'", "els => els.length"))

        page.setContent("<div><span>a</span></div><div><span>a</span></div><section><div><span>a</span></div></section>")
        assertEquals(3, page.evalOnSelectorAll("*css=div >> 'a'", "els => els.length"))
        assertEquals(1, page.evalOnSelectorAll("section >> *css=div >> 'a'", "els => els.length"))
    }

    @Test
    fun `check to support capture when multiple paths match`() {
        page.setContent("<div><div><span></span></div></div><div></div>")
        assertEquals(2, page.evalOnSelectorAll("*css=div >> span", "els => els.length"))
        page.setContent("<div><div><span></span></div><span></span><span></span></div><div></div>")
        assertEquals(2, page.evalOnSelectorAll("*css=div >> span", "els => els.length"))
    }

    @Test
    fun `check to return complex values`() {
        page.setContent("<div>hello</div><div>beautiful</div><div>world!</div>")
        val texts = page.evalOnSelectorAll("css=div", "divs => divs.map(div => div.textContent)")
        assertEquals(arrayListOf("hello", "beautiful", "world!"), texts)
    }
}