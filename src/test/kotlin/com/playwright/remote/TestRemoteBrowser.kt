package com.playwright.remote

import com.playwright.remote.base.BaseTest
import com.playwright.remote.core.exceptions.WebSocketException
import com.playwright.remote.engine.browser.RemoteBrowser
import com.playwright.remote.engine.browser.impl.Browser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail

import org.junit.jupiter.api.Test
import kotlin.test.assertTrue


class TestRemoteBrowser : BaseTest() {

    @Test
    fun `cannot create browser without url`() {
        try {
            RemoteBrowser.connectWs("")
            fail("no one exception thrown")
        } catch (e: WebSocketException) {
            assertEquals("Inappropriate url: Expected URL scheme 'http' or 'https' but no colon was found", e.message)
        }
    }

    @Test
    fun `cannot create browser with incorrect url`() {
        try {
            RemoteBrowser.connectWs("qwerty")
            fail("no one exception thrown")
        } catch (e: WebSocketException) {
            assertEquals("Inappropriate url: Expected URL scheme 'http' or 'https' but no colon was found", e.message)
        }
    }

    @Test
    fun `cannot create browser when connection failed`() {
        try {
            RemoteBrowser.connectWs("ws://127.0.0.1:4444/7dc9385fedfed927e424380d15016c3f")
            fail("no one exception thrown")
        } catch (e: WebSocketException) {
            assertEquals("Failed to connect to /127.0.0.1:4444", e.message)
        }
    }

    @Test
    fun `check success connection to browser`() {
        try {
            RemoteBrowser.connectWs(wsUrl).use {
                assertTrue(it is Browser)
            }
        } catch (e: WebSocketException) {
            fail("Exception was thrown ${e.message}")
        }
    }
}