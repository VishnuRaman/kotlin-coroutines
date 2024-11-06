package com.vishnuraman.advanced

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import org.slf4j.LoggerFactory
import kotlin.random.Random

/*
    Channels
    - Thread-safe concurrent queues used for inter-coroutine communication
    - Semantic blocking
        - upon ferception if there are no items
        - upon sending if the channel's buffer is full
    - Closing
        - sending more items is an error
        - receiving is an error, if channel is empty
    - Cancelling
        - closes channel & discards elements
    - Configuring
        - buffer capacity
        - buffer overflow behaviour
        - discarded elements behaviour
 */


object Channels {
    val LOGGER = LoggerFactory.getLogger(this::class.java)

    // channel  = concurrent queue
    data class StockPrice(val symbol: String, val price: Double, val timestamp: Long)

    val aChannel = Channel<StockPrice>()

    suspend fun pushStocks(channel: SendChannel<StockPrice>) {
        LOGGER.info("Trying to add an element")
        channel.send(StockPrice("APPL", 100.0, System.currentTimeMillis()))
        delay(Random.nextLong(1000))
        LOGGER.info("Trying to add an element")
        channel.send(StockPrice("GOOG", 200.0, System.currentTimeMillis()))
        delay(Random.nextLong(1000))
        LOGGER.info("Trying to add an element")
        channel.send(StockPrice("MSFT", 300.0, System.currentTimeMillis()))

        // when done, close the channel
        channel.close()
        // semantic blocking + suspension points
    }

    suspend fun readStocks(channel: ReceiveChannel<StockPrice>) {
        repeat(4) {

                // channel might be closed here
            val result = channel.receiveCatching() // can be a value, failed, closed
            val maybePrice = result.getOrNull()
            if (maybePrice != null)
                LOGGER.info("Received stock price: $maybePrice")


            // receiving is semantically blocking
            // receiving from a closed channel is an error
        }
    }

    /*
        Can use `channel.isClosedForReceive` but be careful that receiving right after might fail.
        Can use `channel.tryReceive` but it does NOT wait.
     */

    suspend fun stockMarketTerminal() =
        coroutineScope {
            val stockChannel = Channel<StockPrice>()

            launch {
                pushStocks(stockChannel)
            }

            launch { readStocks(stockChannel) }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun stockMarketNicer() =
        coroutineScope {
            val stocksChannel = produce {
                // launches a coroutine with a send()
                pushStocks(channel)
            } // will automatically close the channel

            launch {
                readStocks(stocksChannel)
            }
        }

    /*
        Customize a channel
        - optional capacity
     */
    suspend fun demoCustomizedChannels() =
        coroutineScope {
            val stocksChannel = Channel<StockPrice>(
                capacity = 2,
                onBufferOverflow = BufferOverflow.DROP_OLDEST
            ) // both read and write

            // producer
            launch {
                pushStocks(stocksChannel)
                // buffer items inside
                /*
                    if the buffer is full, any send() will either
                    - semantically block (default)
                    - drop oldest element in buffer
                    - drop the element which wants to get in (latest)
                */
            }

            // consumer
            launch {
                LOGGER.info("Taking a while for the consumer to start...")
                delay(5000)
                readStocks(stocksChannel)
            }
        }

    // closing = cannot send() any more elements, but can receive() any elements CURRENTLY in the channel
    // cancelling = closing + dropping all current elements in the channel
    /*
        onUndeliveredElement triggers if the channel has elements that are about to be discarded:
        - channel gets cancelled with elements inside
        - send() throws an error, e.g. if the channel is closed
        - receive() throws an error, e.g. if someone cancels the coroutine calling receive()
     */
    suspend fun demoOnUndelivered() =
        coroutineScope {
            val channel = Channel<StockPrice>(
                capacity = 10,
                onUndeliveredElement = { stockPrice ->
                    LOGGER.info("Just dropped: $stockPrice")
                }
            )

            val prices = listOf(
                StockPrice("AAPL", 100.0, System.currentTimeMillis()),
                StockPrice("GOOG", 789.0, System.currentTimeMillis()),
                StockPrice("MSFT", 78.0, System.currentTimeMillis()),
                StockPrice("AMZN", 1234.8, System.currentTimeMillis())
            )

            val producer = launch {
                for (price in prices) {
                    LOGGER.info("Sending: $price")
                    channel.send(price)
                    delay(200)
                }
            }

            val consumer = launch {
                repeat(2) {
                    val price = channel.receive()
                    LOGGER.info("Received: $price")
                    delay(1000)
                }
                channel.cancel() // close + drop all elements in the buffer
            }

            producer.join()
            consumer.join()
        }

}

suspend fun main() {
    Channels.demoOnUndelivered()
}