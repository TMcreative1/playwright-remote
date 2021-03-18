package com.playwright.remote

import com.playwright.remote.core.exceptions.WebSocketException
import com.playwright.remote.playwright.browser.RemoteBrowser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail

import org.junit.jupiter.api.Test


class TestRemoteBrowser {

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
}