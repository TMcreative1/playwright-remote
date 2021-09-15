package io.github.tmcreative1.playwright.remote.engine.waits.impl

import io.github.tmcreative1.playwright.remote.core.enums.EventType
import io.github.tmcreative1.playwright.remote.core.exceptions.PlaywrightException
import io.github.tmcreative1.playwright.remote.engine.frame.api.IFrame
import io.github.tmcreative1.playwright.remote.engine.listener.ListenerCollection

class WaitFrameDetach(
    listeners: ListenerCollection<EventType>,
    frame: IFrame
) : WaitEvent<EventType, IFrame>(listeners, EventType.FRAMEDETACHED, { detachedFrame -> frame == detachedFrame }) {
    override fun get(): IFrame {
        throw PlaywrightException("Navigating frame was detached")
    }
}