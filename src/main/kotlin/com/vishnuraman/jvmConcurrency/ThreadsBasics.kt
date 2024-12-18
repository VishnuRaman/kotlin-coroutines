package com.vishnuraman.jvmConcurrency

import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import kotlin.concurrent.thread

object ThreadsBasics {

    // Thread = independent unit of execution

    // Thread = data structure (maps to OS threads)
    // Runnable = piece of code to run

    val takingTheBus = Runnable {
        println("getting in the bus")
        (0..10).forEach {
            println("${it * 10}% done")
            Thread.sleep(300)
        }
        println("Getting off the bus, I'm done!")
    }

    fun runThread() {
        val thread = Thread(takingTheBus)
        // thread is just data
        thread.start() // the code runs independently
    }

    fun runMultipleThreads() {
        val takingBus = Thread(takingTheBus)
        val listeningToPodcast = thread(start = false) {   // same as Thread(Runnable{...})
            println("Personal development")
            Thread.sleep(2000)
            println("I'm  a new person now!")
        } // also starts the thread!

        // start the threads
        takingBus.start() // exception if you start a thread multiple times
        listeningToPodcast.start()

        // join threads = block until they finish
        takingBus.join()
        listeningToPodcast.join()
    }

    // interuption
    val scrollingSM = thread(start = false) {
        while (true) {
            try {
                println("Scrolling my SM")
                Thread.sleep(1000)
            } catch (e: InterruptedException) {
                println("time to stop")
                return@thread // non-local return
            }

        }
    }

    fun demoInterruption() {
        scrollingSM.start()
        // block it after 5 seconds
        Thread.sleep(5000)
        scrollingSM.interrupt() // throws InterruptedException on that thread!
        scrollingSM.join()
    }

    // executors and futures
    fun demoExecutorsFutures() {
        // thread pool
        val executor = Executors.newFixedThreadPool(8)
        executor.submit {
            for (i in 1..100) {
                println("Counting to $i")
                Thread.sleep(100)
            }
        }

        // make a thread return a value  = Future
        val future: Future<Int> = executor.submit(
            Callable { // will be run on one of the threads
                println("computing the meaning of life")
                Thread.sleep(3000)
                42
            }
        )

        println("the meaning of life is ${future.get()}") // get() blocks the calling thread until the future is done
        // similar to join() on the thread

        // shut down an Executor -> call it explicitly
        executor.shutdown() // wait for all tasks to be done, no new tasks may be submitted
     }

    @JvmStatic
    fun main(args: Array<String>) {
        // main thread
        demoExecutorsFutures()

    }
}