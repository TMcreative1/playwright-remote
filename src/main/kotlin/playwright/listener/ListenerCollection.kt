package playwright.listener

import java.util.function.Consumer

class ListenerCollection<EventType, C> {
    private val listeners: HashMap<EventType, MutableList<(C) -> Unit>> = HashMap()

    fun <T> notify(eventType: EventType, param: T) {
        val list: MutableList<(C) -> Unit> = listeners[eventType] ?: return
        list
            .filterIsInstance<Consumer<T>>()
            .forEach {
                it.accept(param)
            }
    }

    fun add(eventType: EventType, listener: (C) -> Unit) {
        var list: MutableList<(C) -> Unit>? = listeners[eventType]
        if (list == null) {
            list = arrayListOf()
            listeners[eventType] = list
        }
        list.add(listener)
    }

    fun remove(eventType: EventType, listener: (C) -> Unit) {
        val list: MutableList<(C) -> Unit> = listeners[eventType] ?: return
        list.removeAll(listOf(listener))
        if (list.isEmpty()) {
            listeners.remove(eventType)
        }
    }

    fun hasListeners(eventType: EventType): Boolean = listeners.containsKey(eventType)
}