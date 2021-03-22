package com.playwright.remote.engine.waits.impl

import com.playwright.remote.engine.listener.ListenerCollection
import com.playwright.remote.engine.listener.UniversalConsumer
import com.playwright.remote.engine.waits.api.IWait
import java.util.function.Predicate


open class WaitEvent<EventType, T>(
    private val listeners: ListenerCollection<EventType>,
    private val type: EventType,
    private val predicate: Predicate<T>? = null,
    private var eventArg: T? = null
) : IWait<T>, (T) -> Unit {

    init {
        @Suppress("UNCHECKED_CAST")
        listeners.add(type, this as UniversalConsumer)
    }

    override fun isFinished(): Boolean = eventArg != null

    override fun get(): T = eventArg!!

    @Suppress("UNCHECKED_CAST")
    override fun dispose() = listeners.remove(type, this as UniversalConsumer)

    override fun invoke(p1: T) {
        if (predicate != null && !predicate.test(p1)) {
            return
        }
        eventArg = p1
        dispose()
    }
}