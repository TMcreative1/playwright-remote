package com.playwright.remote.engine.waits.impl

import com.playwright.remote.core.enums.EventType
import com.playwright.remote.core.enums.EventType.SOCKETERROR
import com.playwright.remote.core.exceptions.PlaywrightException
import com.playwright.remote.engine.listener.ListenerCollection

class WaitWebSocketError<T>(
    listeners: ListenerCollection<EventType>
) : WaitEvent<EventType, T>(
    listeners, SOCKETERROR
) {
    override fun get(): T {
        throw PlaywrightException("Socket error")
    }
}