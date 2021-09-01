package com.playwright.remote

import com.playwright.remote.base.BaseTest
import com.playwright.remote.core.exceptions.PlaywrightException
import com.playwright.remote.engine.options.RegisterOptions
import org.junit.jupiter.api.Test
import java.nio.file.Paths
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.fail

class TestSelectorRegister : BaseTest() {

    @Test
    fun `check correct work`() {
        val selectorScript = """{
            |   create(root, target) {
            |       return target.nodeName;
            |   },
            |   query(root, selector) {
            |       return root.querySelector(selector);
            |   },
            |   queryAll(root, selector) {
            |       return Array.from(root.querySelectorAll(selector));
            |   }
            |}
        """.trimMargin()
        browser.selectors().register("tag", selectorScript)

        val context = browser.newContext()
        browser.selectors().register("tag2", selectorScript)

        val pg = context.newPage()
        pg.setContent("<div><span></span></div><div></div>")

        assertEquals("DIV", pg.evalOnSelector("tag=DIV", "e => e.nodeName"))
        assertEquals("SPAN", pg.evalOnSelector("tag=SPAN", "e => e.nodeName"))
        assertEquals(2, pg.evalOnSelectorAll("tag=DIV", "es => es.length"))

        assertEquals("DIV", pg.evalOnSelector("tag2=DIV", "e => e.nodeName"))
        assertEquals("SPAN", pg.evalOnSelector("tag2=SPAN", "e => e.nodeName"))
        assertEquals(2, pg.evalOnSelectorAll("tag2=DIV", "es => es.length"))

        try {
            pg.querySelector("tAG=DIV")
            fail("querySelector should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Unknown engine \"tAG\" while parsing selector tAG=DIV"))
        }
        context.close()
    }

    @Test
    fun `check correct work with path`() {
        browser.selectors().register("foo", Paths.get("src/test/resources/scripts/section-selector-engine.js"))
        page.setContent("<section></section>")
        assertEquals("SECTION", page.evalOnSelector("foo=whatever", "e => e.nodeName"))
    }

    @Test
    fun `check correct work in main and isolate world`() {
        val createSelector = """{
            |   create(root, target) { },
            |   query(root, selector) {
            |       return window['__answer'];
            |   },
            |   queryAll(root, selector) {
            |       return window['__answer'] ? [window['__answer'], document.body, document.documentElement] : [];
            |   }
            |}
        """.trimMargin()
        browser.selectors().register("main", createSelector)
        browser.selectors().register("isolated", createSelector, RegisterOptions { it.contentScript = true })

        page.setContent("<div><span><section></section></span></div>")
        page.evaluate("() => window['__answer'] = document.querySelector('span')")

        assertEquals("SPAN", page.evalOnSelector("main=ignored", "e => e.nodeName"))
        assertEquals("SPAN", page.evalOnSelector("css=div >> main=ignored", "e => e.nodeName"))
        assertEquals(true, page.evalOnSelectorAll("main=ignored", "es => window['__answer'] !== undefined"))
        assertEquals(3, page.evalOnSelectorAll("main=ignored", "es => es.filter(e => e).length"))

        assertNull(page.querySelector("isolated=ignored"))
        assertNull(page.querySelector("css=div >> isolated=ignored"))

        assertEquals(true, page.evalOnSelectorAll("isolated=ignored", "es => window['__answer'] !== undefined"))
        assertEquals(3, page.evalOnSelectorAll("isolated=ignored", "es => es.filter(e => e).length"))
        assertEquals("SPAN", page.evalOnSelector("main=ignored >> isolated=ignored", "e => e.nodeName"))
        assertEquals("SPAN", page.evalOnSelector("isolated=ignored >> main=ignored", "e => e.nodeName"))
        assertEquals("SECTION", page.evalOnSelector("main=ignored >> css=section", "e => e.nodeName"))
    }

    @Test
    fun `check to handle errors`() {
        try {
            page.querySelector("neverregister=ignored")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Unknown engine \"neverregister\" while parsing selector neverregister=ignored"))
        }
        val createSelector = """{
            |   create(root, target) {
            |       return target.nodeName;
            |   },
            |   query(root, selector) {
            |       return root.querySelector("test");
            |   },
            |   queryAll(root, selector) {
            |       return Array.from(root.querySelectorAll("test"));
            |   }
            |}
        """.trimMargin()

        try {
            browser.selectors().register("$", createSelector)
            fail("register should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Selector engine name may only contain [a-zA-Z0-9_] characters"))
        }

        browser.selectors().register("sel", createSelector)
        browser.selectors().register("seL", createSelector)

        try {
            browser.selectors().register("sel", createSelector)
            fail("register should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("\"sel\" selector engine has been already registered"))
        }
        try {
            browser.selectors().register("css", createSelector)
            fail("register should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("\"css\" is a predefined selector engine"))
        }
    }
}