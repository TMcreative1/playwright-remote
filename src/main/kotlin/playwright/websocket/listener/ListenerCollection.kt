package playwright.websocket.listener

import java.util.function.Consumer

class ListenerCollection<EventType> {
    private val listeners: HashMap<EventType, MutableList<Consumer<*>>> = HashMap()

    fun <T> notify(eventType: EventType, param: T) {
        val list: MutableList<Consumer<*>> = listeners[eventType] ?: return
        list
            .filterIsInstance<Consumer<T>>()
            .forEach {
                it.accept(param)
            }
    }

    fun add(eventType: EventType, listener: Consumer<*>) {
        var list: MutableList<Consumer<*>>? = listeners[eventType]
        if (list == null) {
            list = arrayListOf()
            listeners[eventType] = list
        }
        list.add(listener)
    }

    fun remove(eventType: EventType, listener: Consumer<*>) {
        val list: MutableList<Consumer<*>> = listeners[eventType] ?: return
        list.removeAll(listOf(listener))
        if (list.isEmpty()) {
            listeners.remove(eventType)
        }
    }

    fun hasListeners(eventType: EventType): Boolean {
        return listeners.containsKey(eventType)
    }
}