package io.github.tmcreative1.playwright.remote.locator

import io.github.tmcreative1.playwright.remote.base.BaseTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TestLocatorEvaluate : BaseTest() {

    @Test
    fun `check correct work`() {
        page.setContent("<html><body><div class='tweet'><div class='like'>100</div><div class='retweets'>10</div></div></body></html>")
        val tweet = page.locator(".tweet .like")
        val content = tweet.evaluate("node => node.innerText")
        assertEquals("100", content)
    }

    @Test
    fun `check to retrieve content from subtree`() {
        val htmlContent = "<div class='a'>not-a-child-div</div><div id='myId'><div class='a'>a-child-div</div></div>"
        page.setContent(htmlContent)
        val locator = page.locator("#myId .a")
        val content = locator.evaluate("node => node.innerText")
        assertEquals("a-child-div", content)
    }

    @Test
    fun `check correct work of evaluate all method`() {
        page.setContent("<html><body><div class='tweet'><div class='like'>100</div><div class='like'>10</div></div></body></html>")
        val tweet = page.locator(".tweet .like")
        val content = tweet.evaluateAll("nodes => nodes.map(n => n.innerText)")
        assertEquals(listOf("100", "10"), content)
    }

    @Test
    fun `check to retrieve content from subtree for all`() {
        val htmlContent =
            "<div class='a'>not-a-child-div</div><div id='myId'><div class='a'>a1-child-div</div><div class='a'>a2-child-div</div></div>"
        page.setContent(htmlContent)
        val element = page.locator("#myId .a")
        val content = element.evaluateAll("nodes => nodes.map(n => n.innerText)")
        assertEquals(listOf("a1-child-div", "a2-child-div"), content)
    }

    @Test
    fun `check to not throw throw in case of missing selector after all`() {
        val htmlContent = "<div class='a'>not-a-child-div</div><div id='myId'></div>"
        page.setContent(htmlContent)
        val element = page.locator("#myId .a")
        val nodesLength = element.evaluateAll("nodes => nodes.length")
        assertEquals(0, nodesLength)
    }
}