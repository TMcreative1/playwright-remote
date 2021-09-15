package io.github.tmcreative1.playwright.remote.engine.listener

typealias UniversalConsumer = (Any) -> Unit

open class ListenerCollection<EventType> {
    private val listeners: HashMap<EventType, MutableList<UniversalConsumer>> = HashMap()

    fun notify(eventType: EventType, param: Any) {
        val list: MutableList<UniversalConsumer> = listeners[eventType] ?: return
        list
            .toTypedArray()
            .asSequence()
            .forEach { consumer ->
                consumer(param)
            }
    }

    open fun add(eventType: EventType, listener: UniversalConsumer) {
        var list: MutableList<UniversalConsumer>? = listeners[eventType]
        if (list == null) {
            list = arrayListOf()
            listeners[eventType] = list
        }
        list.add(listener)
    }

    open fun remove(eventType: EventType, listener: UniversalConsumer) {
        val list: MutableList<UniversalConsumer> = listeners[eventType] ?: return
        list.removeAll(listOf(listener))
        if (list.isEmpty()) {
            listeners.remove(eventType)
        }
    }

    fun hasListeners(eventType: EventType): Boolean = listeners.containsKey(eventType)
}