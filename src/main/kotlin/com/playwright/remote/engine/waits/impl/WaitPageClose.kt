package com.playwright.remote.engine.waits.impl

import com.playwright.remote.core.enums.EventType
import com.playwright.remote.core.enums.EventType.CLOSE
import com.playwright.remote.core.exceptions.PlaywrightException
import com.playwright.remote.engine.listener.ListenerCollection


class WaitPageClose<T>(
    listeners: ListenerCollection<EventType>
) : WaitEvent<EventType, T>(
    listeners, CLOSE
) {
    override fun get(): T {
        throw PlaywrightException("Page closed")
    }
}