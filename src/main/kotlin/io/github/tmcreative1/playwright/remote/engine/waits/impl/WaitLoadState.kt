package io.github.tmcreative1.playwright.remote.engine.waits.impl

import io.github.tmcreative1.playwright.remote.core.enums.InternalEventType
import io.github.tmcreative1.playwright.remote.core.enums.LoadState
import io.github.tmcreative1.playwright.remote.engine.listener.ListenerCollection
import io.github.tmcreative1.playwright.remote.engine.listener.UniversalConsumer
import io.github.tmcreative1.playwright.remote.engine.waits.api.IWait

class WaitLoadState(
    private val expectedState: LoadState?,
    private val internalListeners: ListenerCollection<InternalEventType>,
    loadStates: Set<LoadState>
) : IWait<Void?>, (LoadState) -> Unit {
    private var isFinished: Boolean = loadStates.contains(expectedState)

    init {
        if (!isFinished) {
            @Suppress("UNCHECKED_CAST")
            internalListeners.add(InternalEventType.LOADSTATE, this as UniversalConsumer)
        }
    }

    override fun invoke(p1: LoadState) {
        if (expectedState == p1) {
            isFinished = true
            dispose()
        }
    }

    override fun isFinished(): Boolean {
        return isFinished
    }

    override fun get(): Void? {
        return null
    }

    @Suppress("UNCHECKED_CAST")
    override fun dispose() {
        internalListeners.remove(InternalEventType.LOADSTATE, this as UniversalConsumer)
    }
}