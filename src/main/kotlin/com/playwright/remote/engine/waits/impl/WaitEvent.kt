package com.playwright.remote.engine.waits.impl

import com.playwright.remote.engine.listener.ListenerCollection
import com.playwright.remote.engine.listener.UniversalConsumer
import com.playwright.remote.engine.waits.api.IWait

typealias Predicate<T> = (T) -> Boolean

open class WaitEvent<EventType, T>(
    private val listeners: ListenerCollection<EventType>,
    private val type: EventType,
    private val predicate: Predicate<T> = { false },
    private var eventArg: T? = null
) : IWait<T>, (T) -> Unit {

    init {
        @Suppress("UNCHECKED_CAST", "LeakingThis")
        listeners.add(type, this as UniversalConsumer)
    }

    override fun invoke(p1: T) {
        if (predicate(p1)) {
            return
        }
        eventArg = p1
        dispose()
    }

    override fun isFinished(): Boolean = eventArg != null

    override fun get(): T = eventArg!!

    @Suppress("UNCHECKED_CAST")
    override fun dispose() =
        listeners.remove(type, this as UniversalConsumer)

}