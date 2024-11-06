package com.vishnuraman.coroutines

import kotlinx.coroutines.*
import org.slf4j.LoggerFactory

object SuspendFunctions {

    private val LOGGER = LoggerFactory.getLogger(this::class.java)

    suspend fun takeTheBus() { // this code can run on a coroutine
        LOGGER.info("Getting in the bus")
        (0..10).forEach {
            LOGGER.info("${it * 10}% done")
            delay(300) // yielding point - coroutine that runs this code can be suspended
            // cooperative scheduling

        }
        LOGGER.info("Arrived at the destination")
    }

    // suspend functions CANNOT be run from regular functions

    // continuation = state of the code at the point of coroutine is suspended
    suspend fun demoSuspendedCoroutine() {
        LOGGER.info("Starting to run some code")

        val resumedComputation = suspendCancellableCoroutine { continuation ->
            LOGGER.info("This runs when I'm suspended")
            continuation.resumeWith(Result.success(42))
        } // yielding point - coroutine is SUSPENDED


        LOGGER.info("This prints after resuming the coroutine: $resumedComputation")
    }

    // CPS - continuation passing style with an additional Continuation argument
    // can only be called by other suspend functions
    // offer cooperative scheduling at yielding/suspension points, e.g. delays
    // suspend functions compile to functions with Continuation as last argument

    // suspend function values (lambdas)
    val suspendLambda: suspend (Int) -> Int = { it + 1 }
    // (Int) -> Int and `suspend` (Int) -> Int are DIFFERENT TYPES

    val increment: suspend Int.() -> Int = { this + 1 }
    suspend fun suspendLambdaDemo() {
        LOGGER.info("Suspend call: ${suspendLambda(2)}")
        val four = 3.increment()
        LOGGER.info("Suspend lambda with receivers: $four")
    }


    // TODO: why does it not work with a fun main here?
//    @JvmStatic ----> public static void main(String[], Continuation) -> not a signature the JVM expects as an
//   entrypoint for an application
//    suspend fun main(args: Array<String>) {
//        takeTheBus()
//    }
}

suspend fun main(args: Array<String>) {
    SuspendFunctions.suspendLambdaDemo()
}