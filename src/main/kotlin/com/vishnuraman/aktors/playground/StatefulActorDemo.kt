package com.vishnuraman.aktors.playground

import com.vishnuraman.aktors.ActorSystem
import com.vishnuraman.aktors.Behavior
import com.vishnuraman.aktors.Behaviors
import org.slf4j.LoggerFactory

object WordCounter {
    operator fun invoke(): Behavior<String> = Behaviors.setup { ctx ->
        ctx.log.info("Setting up")
        var total = 0

        Behaviors.receiveMessage { msg ->
            val newCount = msg.split(" ").size
            total += newCount
            ctx.log.info("received new message, updated count to $total")
            Behaviors.same()
        }
    }
}

object WordCounterStateless {
    private val log = LoggerFactory.getLogger(this::class.java)

    operator fun invoke(): Behavior<String> =
        active(0)

    private fun active(currentCount: Int): Behavior<String> =
        Behaviors.receive { ctx, msg ->
            val newCount = msg.split(" ").size
            val newTotal = currentCount + newCount
            ctx.log.info("received new message, updated count to $newTotal")
            active(newTotal)
        }
}

object StatefulActorDemo {
    suspend fun main() {
        ActorSystem.app(WordCounterStateless(), "WordCounterSystem") { guardian ->
            guardian `!` "This is an actor framework on top of coroutines"
            guardian `!` "Coroutines rock"
        }
    }
}

suspend fun main() {
    StatefulActorDemo.main()
}