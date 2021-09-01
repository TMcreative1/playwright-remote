package io.github.tmcreative1.playwright.remote

import io.github.tmcreative1.playwright.remote.core.enums.EventType
import io.github.tmcreative1.playwright.remote.engine.listener.ListenerCollection
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


class TestListenerCollection {
    data class TestListenerData(var notify: String = "")

    private val cl = ListenerCollection<EventType>()

    @Test
    fun `can add element to listener`() {
        val consumer: (Any) -> Unit = { println() }
        cl.add(EventType.FRAMESENT, consumer)

        assertEquals(cl.hasListeners(EventType.FRAMESENT), true)
    }

    @Test
    fun `can remove element from listener collection`() {
        val consumer: (Any) -> Unit = { println() }
        cl.add(EventType.FRAMESENT, consumer)
        assertEquals(cl.hasListeners(EventType.FRAMESENT), true)

        cl.remove(EventType.FRAMESENT, consumer)
        assertEquals(cl.hasListeners(EventType.FRAMESENT), false)
    }

    @Test
    fun `can notify from listener`() {
        val testNotifyValue = "Changed"
        val defaultTestdata = TestListenerData()
        val consumer: (Any) -> Unit = { defaultTestdata.notify = testNotifyValue }
        cl.add(EventType.FRAMESENT, consumer)
        cl.notify(EventType.FRAMESENT, this)

        assertEquals(testNotifyValue, defaultTestdata.notify)
    }

    @Test
    fun `can notify for multiple consumers from listener`() {
        val testNotifyValue1 = "Changed1"
        val defaultTestdata1 = TestListenerData()
        val consumer1: (Any) -> Unit = { defaultTestdata1.notify = testNotifyValue1 }

        val testNotifyValue2 = "Changed2"
        val defaultTestdata2 = TestListenerData()
        val consumer2: (Any) -> Unit = { defaultTestdata2.notify = testNotifyValue2 }

        cl.add(EventType.FRAMESENT, consumer1)
        cl.add(EventType.FRAMESENT, consumer2)
        cl.notify(EventType.FRAMESENT, this)

        assertEquals(testNotifyValue1, defaultTestdata1.notify)
        assertEquals(testNotifyValue2, defaultTestdata2.notify)
    }
}