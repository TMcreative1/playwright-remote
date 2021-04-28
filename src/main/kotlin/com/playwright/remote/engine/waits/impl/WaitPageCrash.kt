package com.playwright.remote.engine.waits.impl

import com.playwright.remote.core.enums.EventType
import com.playwright.remote.core.enums.EventType.CRASH
import com.playwright.remote.core.exceptions.PlaywrightException
import com.playwright.remote.engine.listener.ListenerCollection

class WaitPageCrash<T>(
    listeners: ListenerCollection<EventType>
) : WaitEvent<EventType, T>(
    listeners, CRASH
) {
    override fun get(): T {
        throw PlaywrightException("Page crashed")
    }
}