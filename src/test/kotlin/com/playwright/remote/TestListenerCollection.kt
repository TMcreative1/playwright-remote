package com.playwright.remote

import com.playwright.remote.core.enums.EventType
import com.playwright.remote.playwright.listener.ListenerCollection
import kotlin.test.Test
import kotlin.test.assertEquals


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
    fun `can remove element to listener`() {
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
}