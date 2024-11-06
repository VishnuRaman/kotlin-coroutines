package com.vishnuraman.coroutines

import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import kotlin.random.Random

/*
    Coroutines need a scope to run
    A coroutine scope
        - can launch new coroutines concurrently, e.g. launch, async
        - semantically blocks until all the coroutines inside finish
        - manages the lifecycle of coroutines
    Coroutine builders
        - launch: starts a new coroutine, gives back a Job handle
        - async: starts a new coroutine, gives back a "future" value
 */


object CoroutineBuilders {

    private val LOGGER = LoggerFactory.getLogger(this::class.java)

    suspend fun developer(index: Int) {
        LOGGER.info("[dev $index] I'm a developer. I need coffee.")
        delay(Random.nextLong(1000))
        LOGGER.info("[dev $index] I got coffee, let's get coding!")
    }

    suspend fun projectManager() {
        LOGGER.info("[PM] I'm a project manager. I need to get devs' progress.")
        delay(Random.nextLong(1000)) // can suspend the coroutine
        LOGGER.info("[PM] I checked progress, let's get micro managing!")
    }

    suspend fun startup() {
        LOGGER.info("It's 9AM, let's start")
        // COROUTINE SCOPE
        coroutineScope {
            // the ability to launch coroutines concurrently
            launch { developer(42) }
            launch { projectManager() }
            // manages lifecycle of coroutines
        } // will (semantically) block until ALL coroutines inside will finish

        LOGGER.info("It's 6PM, let's go home")

        coroutineScope {
            launch { developer(1) }
            launch { developer(2) }
        }

        LOGGER.info("It's 9PM, let's go home")
    }

    suspend fun globalStartup() {
        LOGGER.info("It's 9AM, let's start")
        val dev1 = GlobalScope.launch { developer(1) }
        val dev2 = GlobalScope.launch { developer(2) }
        // manually join coroutines
        dev1.join() // semantically blocking
        dev2.join()
        LOGGER.info("It's 6PM, let's go home")
    }

    // async - return a value out of a coroutine

    suspend fun developerCoding(index: Int): String {
        LOGGER.info("[dev $index] I'm a developer. I need coffee.")
        delay(Random.nextLong(1000))
        LOGGER.info("[dev $index] I got coffee, let's get coding!")
        return """
            fun main() {
                println("This is Kotlin!")
            }
        """.trimIndent()
    }

    suspend fun projectManagerEstimating(): Int {
        LOGGER.info("[PM] I'm a project manager. I need to get devs' progress.")
        delay(Random.nextLong(1000)) // can suspend the coroutine
        LOGGER.info("[PM] I checked progress, let's get micro managing!")
        return 12
    }

    data class Project(val code: String, val estimation: Int)

    suspend fun startUpValues(){
        LOGGER.info("It's 9AM, let's start")
        val project = coroutineScope {
            val deferredCode = async { developerCoding(42) }
            val deferredEstimation = async { projectManagerEstimating() }
            val code = deferredCode.await() // semantically blocking
            val estimation = deferredEstimation.await()
             Project(code, estimation)
        }
        LOGGER.info("It's 9PM, still going. We have the project $project")
    }
}



suspend fun main() {
    CoroutineBuilders.startUpValues()
}