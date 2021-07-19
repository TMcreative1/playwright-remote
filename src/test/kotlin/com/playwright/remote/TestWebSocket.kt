package com.playwright.remote

import com.playwright.remote.base.BaseTest
import com.playwright.remote.base.server.WebSocketServer
import com.playwright.remote.core.exceptions.PlaywrightException
import com.playwright.remote.engine.options.wait.WaitForFrameReceivedOptions
import com.playwright.remote.engine.options.wait.WaitForFrameSentOptions
import com.playwright.remote.engine.websocket.api.IWebSocket
import com.playwright.remote.engine.websocket.api.IWebSocketFrame
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant
import kotlin.test.*

class TestWebSocket : BaseTest() {

    companion object {
        @JvmStatic
        lateinit var webSocketServer: WebSocketServer

        @JvmStatic
        @BeforeAll
        fun startWebSocketServer() {
            webSocketServer = WebSocketServer("localhost", 8099)
            webSocketServer.start()
        }

        @JvmStatic
        @AfterAll
        fun stopWebSocketServer() {
            webSocketServer.stop()
        }
    }

    private fun waitForCondition(condition: Array<Boolean>) {
        assertEquals(1, condition.size)
        val start = Instant.now()
        while (!condition[0]) {
            page.waitForTimeout(100.0)
            assertTrue(Duration.between(start, Instant.now()).seconds < 30, "Timed out")
        }
    }

    @Test
    fun `check correct work`() {
        val jsScript = """port => {
            |   let cb;
            |   const result = new Promise(f => cb = f);
            |   const ws = new WebSocket('ws://localhost:' + port + '/ws');
            |   ws.addEventListener('message', data => { ws.close(); cb(data.data); });
            |   return result;
            |}
        """.trimMargin()
        val result = page.evaluate(jsScript, webSocketServer.port)
        assertEquals("incoming", result)
    }

    @Test
    fun `check to emit close events`() {
        val socketClosed = arrayOf(false)
        val log = arrayListOf<String>()
        val webSocket = arrayListOf<IWebSocket?>(null)
        page.onWebSocket {
            log.add("open ${it.url()}")
            webSocket[0] = it
            it.onClose {
                log.add("close")
                socketClosed[0] = true
            }
        }
        val jsScript = """port => {
            |   const ws = new WebSocket('ws://localhost:' + port + '/ws');
            |   ws.addEventListener('open', () => ws.close());
            |}
        """.trimMargin()
        page.evaluate(jsScript, webSocketServer.port)
        waitForCondition(socketClosed)
        assertEquals(arrayListOf("open ws://localhost:${webSocketServer.port}/ws", "close"), log)
        assertTrue(webSocket[0]!!.isClosed())
    }

    @Test
    fun `check to emit frame events`() {
        val socketClosed = arrayOf(false)
        val log = arrayListOf<String>()
        page.onWebSocket {
            log.add("open")
            it.onFrameSent { frame -> log.add("send ${frame.text()}") }
            it.onFrameReceived { frame -> log.add("received ${frame.text()}") }
            it.onClose {
                log.add("close")
                socketClosed[0] = true
            }
        }
        val jsScript = """port => {
            |   const ws = new WebSocket('ws://localhost:' + port + '/ws');
            |   ws.addEventListener('open', () => ws.send('outgoing'));
            |   ws.addEventListener('message', () => { ws.close(); });
            |}
        """.trimMargin()
        page.evaluate(jsScript, webSocketServer.port)
        waitForCondition(socketClosed)
        if (isWebkit()) {
            log.remove("received A+g=")
        }
        assertEquals("open", log[0], "Events: $log")
        assertEquals("close", log[3], "Events: $log")
        assertEquals(arrayListOf("open", "send outgoing", "received incoming", "close"), log)
    }

    @Test
    fun `check to emit binary frame events`() {
        val socketClosed = arrayOf(false)
        val send = arrayListOf<IWebSocketFrame>()
        page.onWebSocket {
            it.onClose { socketClosed[0] = true }
            it.onFrameSent { frame -> send.add(frame) }
        }
        val jsScript = """port => {
            |   const ws = new WebSocket('ws://localhost:' + port + '/ws');
            |   ws.addEventListener('open', () => {
            |       const binary = new Uint8Array(5);
            |       for (let i = 0; i < 5; ++i)
            |           binary[i] = i;
            |       ws.send('text');
            |       ws.send(binary);
            |       ws.close();
            |   });
            |}
        """.trimMargin()
        page.evaluate(jsScript, webSocketServer.port)
        waitForCondition(socketClosed)
        assertEquals("text", send[0].text())
        for (index in 0..4) {
            assertEquals(index.toByte(), send[1].binary()[index])
        }
    }

    @Test
    fun `check to emit error`() {
        val socketError = arrayOf(false)
        val error = arrayListOf<String?>(null)
        page.onWebSocket {
            it.onSocketError { e ->
                error[0] = e
                socketError[0] = true
            }
        }
        val jsScript = """port => {
            |   new WebSocket('ws://localhost:' + port + '/bogus-ws');
            |}
        """.trimMargin()
        page.evaluate(jsScript, httpServer.serverPort)
        waitForCondition(socketError)
        if (isFirefox()) {
            assertEquals("CLOSE_ABNORMAL", error[0])
        } else {
            assertTrue(error[0]!!.contains("404"), error[0])
        }
        assertTrue(socketError[0])
    }

    @Test
    fun `check to not have stray error events`() {
        val ws = page.waitForWebSocket {
            val jsScript = """port => {
                |   window.ws = new WebSocket('ws://localhost:' + port + '/ws');
                |}
            """.trimMargin()
            page.evaluate(jsScript, webSocketServer.port)
        }
        val error = arrayOf(false)
        assertNotNull(ws)

        ws.onSocketError { error[0] = true }
        ws.waitForFrameReceived {}
        page.evaluate("window.ws.close()")
        assertFalse(error[0])
    }

    @Test
    fun `check to reject future event on socket close`() {
        val ws = page.waitForWebSocket {
            val jsScript = """port => {
                |   window.ws = new WebSocket('ws://localhost:' + port + '/ws');
                |}
            """.trimMargin()
            page.evaluate(jsScript, webSocketServer.port)
        }
        assertNotNull(ws)
        try {
            ws.waitForFrameSent { page.evaluate("window.ws.close()") }
            fail("waitForFrameSent method should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Socket closed"))
        }
    }

    @Test
    fun `check to reject future event on page close`() {
        val ws = page.waitForWebSocket {
            val jsScript = """port => {
                |   window.ws = new WebSocket('ws://localhost:' + port + '/ws');
                |}
            """.trimMargin()
            page.evaluate(jsScript, webSocketServer.port)
        }
        assertNotNull(ws)
        ws.waitForFrameReceived {}
        try {
            ws.waitForFrameSent { page.close() }
            fail("waitForFrameSent method should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Page closed"))
        }
    }

    @Test
    fun `check to call frame received predicate`() {
        val ws = page.waitForWebSocket {
            val jsScript = """port => {
            |   window.ws = new WebSocket('ws://localhost:' + port + '/ws');
            |}
        """.trimMargin()
            page.evaluate(jsScript, webSocketServer.port)
        }
        assertNotNull(ws)

        val text = arrayListOf<String?>(null)
        val predicate: (IWebSocketFrame) -> Boolean = {
            if ("incoming" == it.text()) {
                text[0] = it.text()
            }
            text[0] != null
        }
        val frame = ws.waitForFrameReceived(WaitForFrameReceivedOptions { it.predicate = predicate }) {}
        assertNotNull(frame)
        assertEquals("incoming", text[0])
        assertEquals("incoming", frame.text())
    }

    @Test
    fun `check to call frame sent predicate`() {
        val ws = page.waitForWebSocket {
            val jsScript = """port => {
                |   window.ws = new WebSocket('ws://localhost:' + port + '/ws');
                |   return new Promise(f => ws.addEventListener('open', f));
                |}
            """.trimMargin()
            page.evaluate(jsScript, webSocketServer.port)
        }
        assertNotNull(ws)

        val text = arrayListOf<String?>(null)
        val predicate: (IWebSocketFrame) -> Boolean = {
            if ("outgoing" == it.text()) {
                text[0] = it.text()
            }
            text[0] != null
        }
        val frame = ws.waitForFrameSent(WaitForFrameSentOptions {
            it.predicate = predicate
        }) { page.evaluate("ws.send('outgoing')") }
        assertEquals("outgoing", text[0])
        assertNotNull(frame)
        assertEquals("outgoing", frame.text())
    }

    @Test
    fun `check correct work frame received with timeout`() {
        val ws = page.waitForWebSocket {
            val jsScript = """port => {
                |   window.ws = new WebSocket('ws://localhost:' + port + '/ws');
                |   return new Promise(f => ws.addEventListener('open', f))
                |}
            """.trimMargin()
            page.evaluate(jsScript, webSocketServer.port)
        }
        assertNotNull(ws)

        try {
            ws.waitForFrameReceived(WaitForFrameReceivedOptions {
                it.predicate = { false }
                it.timeout = 1.0
            }) {}
            fail("waitForFrameReceived method should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Timeout"), e.message)
        }
    }

    @Test
    fun `check correct work frame sent with timeout`() {
        val ws = page.waitForWebSocket {
            val jsScript = """port => {
                |   window.ws = new WebSocket('ws://localhost:' + port + '/ws');
                |   return new Promise(f => ws.addEventListener('open', f));
                |}
            """.trimMargin()
            page.evaluate(jsScript, webSocketServer.port)
        }

        assertNotNull(ws)
        try {
            ws.waitForFrameSent(WaitForFrameSentOptions {
                it.predicate = { false }
                it.timeout = 1.0
            }) { page.evaluate("ws.send('outgoing');") }
            fail("waitForFrameSent method should throw")
        } catch (e: PlaywrightException) {
            assertTrue(e.message!!.contains("Timeout"), e.message)
        }
    }
}