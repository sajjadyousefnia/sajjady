package com.example

import com.google.gson.Gson
import io.ktor.application.*
import io.ktor.features.ContentNegotiation
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.application.*
import io.ktor.gson.gson
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import java.text.DateFormat
import java.time.DayOfWeek
import java.time.LocalTime

// this is true
val port = System.getenv("PORT")?.toInt() ?: 23567
// val port = 8080

object MyClass {
    @JvmStatic
    fun main(args: Array<String>) {
        embeddedServer(Netty, port) {
            routing {
                install(ContentNegotiation) {
                    gson {
                        setDateFormat(DateFormat.LONG)
                        setPrettyPrinting()
                    }
                }
                get("/{text}") {
                    val resText = call.request.queryParameters["requested"].toString()
                    val myres = Gson().fromJson(resText, FirstClass::class.java)
                    call.respond(myres.requested[0].data[0].repeats[0].startTime)

                    // val text = call.parameters["text"]?.toString()
                    // val responseText = call.parameters["text"]?.toString()
/*
                    if (responseText != null) {
                        call.respond(responseText)
                    }
*/
/*
                    println("Job started at ${LocalTime.now()}\r\n")

                    executeBranchingSearch()
                    ScheduledClass.all.sortedBy { it.start }.forEach {
                        call.respond("${it.name}- ${it.daysOfWeek.joinToString("/")} ${it.start.toLocalTime()}-${it.end.toLocalTime()}")
                    }*/
                }
                get("/hello") {
                    call.respond(HttpStatusCode.Accepted, "Hello")
                }
                get("random/{min}/{max}") {
                    val min = call.parameters["min"]?.toIntOrNull() ?: 0
                    val max = call.parameters["max"]?.toIntOrNull() ?: 10
                    val randomString = "${(min until max).shuffled().last()}"
                    call.respond(randomString)
                }
            }
        }.start(wait = true)
    }

    fun executeBranchingSearch() {

        // pre-constraints
        ScheduledClass.all.flatMap { it.slotsFixedToZero }.forEach { it.selected = 0 }

        // Try to encourage most "constrained" slots to be evaluated first
        val sortedSlots = Slot.all.asSequence().filter { it.selected == null }.sortedWith(
            compareBy(
                {
                    // prioritize slots dealing with recurrences
                    val dow = it.block.range.start.dayOfWeek
                    when {
                        dow == DayOfWeek.MONDAY && it.scheduledClass.recurrences == 3 -> -1000
                        dow != DayOfWeek.MONDAY && it.scheduledClass.recurrences == 3 -> 1000
                        dow in DayOfWeek.MONDAY..DayOfWeek.WEDNESDAY && it.scheduledClass.recurrences == 2 -> -500
                        dow !in DayOfWeek.MONDAY..DayOfWeek.WEDNESDAY && it.scheduledClass.recurrences == 2 -> 500
                        dow in DayOfWeek.THURSDAY..DayOfWeek.FRIDAY && it.scheduledClass.recurrences == 1 -> -300
                        dow !in DayOfWeek.THURSDAY..DayOfWeek.FRIDAY && it.scheduledClass.recurrences == 1 -> 300
                        else -> 0
                    }
                },
                { it.block.range.start }, // make search start at beginning of week
                { -it.scheduledClass.slotsNeededPerSession } // followed by class length,

            )
        ).toList()

        // this is a recursive function for exploring nodes in a branch-and-bound tree
        fun traverse(currentBranch: BranchNode? = null): BranchNode? {

            if (currentBranch != null && currentBranch.remainingSlots.isEmpty()) {
                return currentBranch
            }

            for (candidateValue in intArrayOf(1, 0)) {
                val nextBranch = BranchNode(candidateValue, currentBranch?.remainingSlots ?: sortedSlots, currentBranch)

                if (nextBranch.isSolution)
                    return nextBranch

                if (nextBranch.isContinuable) {
                    val terminalBranch = traverse(nextBranch)
                    if (terminalBranch?.isSolution == true) {
                        return terminalBranch
                    }
                }
            }
            return null
        }


        // start with the first Slot and set it as the seed
        // recursively traverse from the seed and get a solution
        val solution = traverse()

        solution?.traverseBackwards?.forEach { it.applySolution() } ?: throw Exception("Infeasible")
    }
}

