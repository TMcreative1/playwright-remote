package io.github.tmcreative1.playwright.remote.engine.waits.impl

import io.github.tmcreative1.playwright.remote.core.enums.EventType
import io.github.tmcreative1.playwright.remote.core.enums.EventType.SOCKETERROR
import io.github.tmcreative1.playwright.remote.core.exceptions.PlaywrightException
import io.github.tmcreative1.playwright.remote.engine.listener.ListenerCollection

class WaitWebSocketError<T>(
    listeners: ListenerCollection<EventType>
) : WaitEvent<EventType, T>(
    listeners, SOCKETERROR
) {
    override fun get(): T {
        throw PlaywrightException("Socket error")
    }
}