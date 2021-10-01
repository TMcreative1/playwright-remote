package io.github.tmcreative1.playwright.remote.engine.waits.impl

import io.github.tmcreative1.playwright.remote.core.enums.EventType
import io.github.tmcreative1.playwright.remote.core.enums.EventType.CRASH
import io.github.tmcreative1.playwright.remote.core.exceptions.PlaywrightException
import io.github.tmcreative1.playwright.remote.engine.listener.ListenerCollection

class WaitPageCrash<T>(
    listeners: ListenerCollection<EventType>
) : WaitEvent<EventType, T>(
    listeners, CRASH
) {
    override fun get(): T {
        throw PlaywrightException("Page crashed")
    }
}