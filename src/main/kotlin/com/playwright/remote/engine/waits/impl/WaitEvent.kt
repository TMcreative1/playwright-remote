package com.playwright.remote.engine.waits.impl

import com.playwright.remote.engine.listener.ListenerCollection
import com.playwright.remote.engine.listener.UniversalConsumer
import com.playwright.remote.engine.waits.api.IWait
import java.util.function.Consumer

typealias Predicate<T> = (T) -> Boolean

open class WaitEvent<EventType, T>(
    private val listeners: ListenerCollection<EventType>,
    private val type: EventType,
    private var eventArg: T? = null,
    private val predicate: Predicate<T> = { false }
) : IWait<T>, Consumer<T> {

    init {
        @Suppress("UNCHECKED_CAST")
        listeners.add(type, this as UniversalConsumer)
    }

    override fun isFinished(): Boolean = eventArg != null

    override fun get(): T = eventArg!!

    override fun accept(t: T) {
        if (!predicate(t)) {
            return
        }

        eventArg = t
        dispose()
    }

    @Suppress("UNCHECKED_CAST")
    override fun dispose() = listeners.remove(type, this as UniversalConsumer)

}