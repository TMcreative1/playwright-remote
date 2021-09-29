package io.github.tmcreative1.playwright.remote

import io.github.tmcreative1.playwright.remote.base.BaseTest
import org.junit.jupiter.api.Test

class TestBeforeunload : BaseTest() {

    @Test
    fun `check to be able navigate away from page with beforeunload`() {
        page.navigate("${httpServer.prefixWithDomain}/beforeunload.html")
        page.click("body")
        page.navigate(httpServer.emptyPage)
    }
}