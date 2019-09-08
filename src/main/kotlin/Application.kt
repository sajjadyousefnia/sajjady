package com.example

import com.google.gson.Gson
import io.ktor.application.*
import io.ktor.features.ContentNegotiation
import io.ktor.response.*
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.gson.gson
import io.ktor.routing.HttpHeaderRouteSelector
import java.text.DateFormat


//

import io.ktor.routing.post
import io.ktor.server.engine.EngineAPI
import io.ktor.server.engine.addShutdownHook
import io.ktor.server.engine.stopServerOnCancellation
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.runBlocking

// this is true
val port = System.getenv("PORT")?.toInt() ?: 23567
// val port = 8080
public lateinit var scheduledClasses: ArrayList<ScheduledClass>

object MyClass {
    @JvmStatic
    fun main(args: Array<String>) {

        val es = embeddedServer(Netty, port) {

            routing {
                install(ContentNegotiation) {
                    gson {
                        setDateFormat(DateFormat.LONG)
                        setPrettyPrinting()
                    }
                }
                post("/{text}") {
                    try {
                        val resText = call.request.queryParameters["requested"].toString()
                        val myres = Gson().fromJson(resText, FirstClass::class.java)
                        log.debug(resText)
                       if( createABatchOfClasses(myres, 0)
                        executeBranchingSearch(myres, this@embeddedServer)
                        var resForRes = ""
                        ScheduledClass.all!!.sortedBy { it.start }.forEach {
                            resForRes += (
                                    "${it.name}- ${it.daysOfWeek.joinToString("/")}" +
                                            " ${it.start.toLocalTime()}-${it.end.toLocalTime()}")
                        }
                        call.respond(resForRes)
                        // log.debug(scheduledClasses.toString())
                    } catch (e: Exception) {
                        call.respond {
                            var tempString = ""
                            scheduledClasses!!.forEach {
                                tempString += it
                            }
                            return@respond tempString
                        }
                        // call.respond(e.toString() + "\n" + e.localizedMessage.toString())

                        // log.debug(scheduledClasses.toString())
                    }

                }
/*
                get("/hello") {
                    call.respond(HttpStatusCode.Accepted, "Hello")
                }
*/
/*
                get("random/{min}/{max}") {
                    val min = call.parameters["min"]?.toIntOrNull() ?: 0
                    val max = call.parameters["max"]?.toIntOrNull() ?: 10
                    val randomString = "${(min until max).shuffled().last()}"
                    call.respond(randomString)
                }
*/
            }
        }
        es.start(wait = true)

    }

    fun executeBranchingSearch(
        myres: FirstClass,
        pipelineContext: Application
    ) {

        Slot.all.filter { !isExisting(myres, it) }.forEach { it.selected = 0 }
        ScheduledClass.all!!.flatMap { it.slotsFixedToZero }.forEach { it.selected = 0 }

        // Try to encourage most "constrained" slots to be evaluated first
        val sortedSlots = Slot.all.asSequence().filter { it.selected == null }./*sortedWith(
*//*
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
*//*
        ).*/toList()

        /* var slotsVaue = ""
         Slot.all.forEach { slotsVaue += (it.block.toString() + " \n " + it.scheduledClass.toString() + " \n ") }
         call!!.respond(
             slotsVaue
         )*/
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
        // scheduledClasses.clear()
    }

    private fun isExisting(firstClass: FirstClass, slot: Slot): Boolean {

        firstClass.requested.forEach {
            if (it.courseName == slot.scheduledClass.name) {
                it.data.forEach {
                    if (it.day == slot.block.range.start.dayOfWeek.name) {
                        it.repeats.forEach {
                            if (slot.block.timeRange.start.hour > it.startTime.split(":")[0].toInt() && slot.block.timeRange.endInclusive.hour < it.endTime.split(
                                    ":"
                                )[0].toInt()
                            ) {
                                return true
                            } else if (slot.block.timeRange.start.hour == it.startTime.split(":")[0].toInt()) {
                                if (slot.block.timeRange.start.minute >= it.startTime.split(":")[1].toInt()) {
                                    return true
                                } else if (slot.block.timeRange.endInclusive.minute <= it.endTime.split(":")[1].toInt()) {
                                    return true
                                }

                            }
                        }
                    }
                }
                return false
            }
        }
        return true
    }

    // classes


    fun createABatchOfClasses(input: FirstClass, i: Int):Boolean {
       // scheduledClasses = null
        scheduledClasses = arrayListOf<ScheduledClass>()
        input.requested.forEach {
            val courseName = it.courseName
            it.data.forEach {
                it.repeats.forEach {
                    scheduledClasses!!.add(ScheduledClass(i + 1, courseName, 1.5, 1, 0))
                }
            }
        }
        return true
    }


}

