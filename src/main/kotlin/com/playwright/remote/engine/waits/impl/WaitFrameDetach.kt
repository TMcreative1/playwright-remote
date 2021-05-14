package com.playwright.remote.engine.waits.impl

import com.playwright.remote.core.enums.EventType
import com.playwright.remote.core.exceptions.PlaywrightException
import com.playwright.remote.engine.frame.api.IFrame
import com.playwright.remote.engine.listener.ListenerCollection

class WaitFrameDetach(
    listeners: ListenerCollection<EventType>,
    frame: IFrame
) : WaitEvent<EventType, IFrame>(listeners, EventType.FRAMEDETACHED, { detachedFrame -> frame == detachedFrame }) {
    override fun get(): IFrame {
        throw PlaywrightException("Navigating frame was detached")
    }
}