package com.playwright.remote.network

import com.playwright.remote.base.BaseTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestNetworkResponse : BaseTest() {
    @Test
    fun `check to return server address`() {
        val response = page.navigate(httpServer.emptyPage)
        assertNotNull(response)
        val address = response.serverAddress()
        assertNotNull(address)
        assertEquals(httpServer.serverPort, address.port)
        assertTrue(listOf("127.0.0.1", "::1").contains(address.ipAddress), address.ipAddress)
    }
}