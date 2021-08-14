package com.hyejineee.rssreader.search

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.actor

import kotlinx.coroutines.newSingleThreadContext

@ObsoleteCoroutinesApi()
object ResultCounter {
    private val context = newSingleThreadContext("counter")
    private var counter = 0

    private val _notification = Channel<Int>(Channel.CONFLATED)
    val notification: ReceiveChannel<Int> = _notification

    private val actor = GlobalScope.actor<Action>(context) {
        for (msg in channel) {
            when (msg) {
                Action.INCREASE -> counter++
                Action.RESET -> counter = 0
            }
            _notification.send(counter)
        }
    }

    suspend fun increment() = actor.send(Action.INCREASE)

    suspend fun reset() = actor.send(Action.RESET)

    enum class Action {
        INCREASE, RESET
    }

}