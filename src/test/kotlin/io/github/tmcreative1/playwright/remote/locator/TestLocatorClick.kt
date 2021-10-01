package io.github.tmcreative1.playwright.remote.locator

import io.github.tmcreative1.playwright.remote.base.BaseTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TestLocatorClick : BaseTest() {

    @Test
    fun `check correct work`() {
        page.navigate("${httpServer.prefixWithDomain}/input/button.html")
        val button = page.locator("button")
        button.click()
        assertEquals("Clicked", page.evaluate("() => window['result']"))
    }

    @Test
    fun `check correct work with node removed`() {
        page.navigate("${httpServer.prefixWithDomain}/input/button.html")
        page.evaluate("() => delete window['Node']")
        val button = page.locator("button")
        button.click()
        assertEquals("Clicked", page.evaluate("() => window['result']"))
    }

    @Test
    fun `check correct work of double click`() {
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
        val button = page.locator("button")
        button.doubleClick()
        assertEquals(true, page.evaluate("double"))
        assertEquals("Clicked", page.evaluate("result"))
    }
}