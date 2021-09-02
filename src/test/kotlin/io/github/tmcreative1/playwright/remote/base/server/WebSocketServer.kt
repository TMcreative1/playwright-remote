package io.github.tmcreative1.playwright.remote.base.server

import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.net.InetSocketAddress
import java.util.concurrent.Semaphore

class WebSocketServer(host: String, port: Int) : WebSocketServer(InetSocketAddress(host, port)), AutoCloseable {
    var lastClientHandshake: ClientHandshake? = null
    private val startSemaphore = Semaphore(0)

    override fun start() {
        super.start()
        startSemaphore.acquire()
    }

    override fun onOpen(conn: WebSocket?, handshake: ClientHandshake?) {
        lastClientHandshake = handshake
        conn?.send("incoming")
    }

    override fun onClose(conn: WebSocket?, code: Int, reason: String?, remote: Boolean) {
        println("Connection was closed with code $code and reason $reason")
    }

    override fun onMessage(conn: WebSocket?, message: String?) {
        println("Message was received $message")
    }

    override fun onError(conn: WebSocket?, ex: Exception?) {
        ex?.printStackTrace()
        startSemaphore.release()
    }

    override fun onStart() {
        startSemaphore.release()
    }

    override fun close() {
        stop()
    }
}