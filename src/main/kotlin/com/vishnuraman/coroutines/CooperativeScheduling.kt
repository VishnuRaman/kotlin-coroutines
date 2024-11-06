package com.vishnuraman.coroutines

import org.slf4j.LoggerFactory
import kotlinx.coroutines.*
import kotlin.random.Random

object CooperativeScheduling {
    val LOGGER = LoggerFactory.getLogger(this::class.java)

    suspend fun greedyDeveloper() {
        LOGGER.info("I want all the coffee.")
        while(System.currentTimeMillis() %1000 != 0L) {

        }
        LOGGER.info("I got all the coffee, let's code.")
    }

    suspend fun developer(index: Int) {
        LOGGER.info("[dev $index] I turn coffee into code.")
        delay(Random.nextLong(1000)) // suspension point
        LOGGER.info("[dev $index] I got coffee, let's turn it into code!")
    }

    suspend fun almostGreedyDeveloper() {
        LOGGER.info("I want all the coffee.")
        while(System.currentTimeMillis() %1000 != 0L) {
            yield() // fundamental suspension point
        }
        LOGGER.info("I got all the coffee, let's code.")
    }

    /*
        functions that can suspend a coroutine
        yield() - fundamental suspension point
        delay() - suspension point
        join(), await()/awaitAll() - semantically blocking
        suspendCancellableCoroutine() - low level suspension point
        suspendCoroutineUninterceptedOrReturn() - lowest level suspension point
        always add yielding/suspension points in our coroutine
        NEVER run coroutines that are CPU bound without some suspension points
        preemptive scheduling - heavy resources

     */

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun startUp() {
        LOGGER.info("It's 9AM, let's get going.")
        val singleThread = Dispatchers.Default.limitedParallelism(1)

        coroutineScope {
            launch(context = singleThread) { developer(42) }
            launch(context = singleThread) { almostGreedyDeveloper() }

        }

        LOGGER.info("It's 1AM, let's go to sleep.")
    }
}

suspend fun main() {
 CooperativeScheduling.startUp()
}