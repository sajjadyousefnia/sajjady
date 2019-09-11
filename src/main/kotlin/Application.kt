package com.example

import com.google.gson.Gson
import io.ktor.application.*
import io.ktor.features.ContentNegotiation
import io.ktor.response.*
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.gson.gson
import java.text.DateFormat


//

import io.ktor.routing.post

// this is true
val port = System.getenv("PORT")?.toInt() ?: 23567
// val port = 8080
var scheduledClasses = ArrayList<ScheduledClass>()
var teachersFilledTimesList = arrayListOf<TeachersTotalFilledTimes>()
var groupsTotalFilledTimesList = arrayListOf<GroupsTotalFilledTimes>()
var lastId = 0
var result = arrayListOf<ArrayList<ScheduledClass>>()

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
                        lastId = 0
                        result.clear()
                        val resText = call.request.queryParameters["requested"].toString()
                        val myres = Gson().fromJson(resText, FirstClass::class.java)
                        myres.generalList.teachersNames.forEach {
                            val teacherTimes: ArrayList<JustTimeDataCLass> = arrayListOf()
                            it.openDays.forEach {
                                teacherTimes.add(JustTimeDataCLass(it.startTime, it.endTime, it.dayName))
                            }
                            teachersFilledTimesList.add(
                                TeachersTotalFilledTimes(
                                    teacherName = it.teacherName,
                                    times = teacherTimes
                                )
                            )
                        }
                        for (groupsCounter in 0 until myres.generalList.entriesYears.size) {
                            for (classCounter in 0 until myres.generalList.classesCount) {
                                doSchedule(
                                    myres.generalList.courseGroups,
                                    groupsCounter, call
                                )

                            }
                        }

                        var resultForPrint = ""
                        result.forEach {
                            resultForPrint += it.toString()
                        }
                        call.respond(resultForPrint)
                    } catch (e: Exception) {
                        call.respond(e.toString() + "\n" + e.localizedMessage.toString() + "\n" + e.printStackTrace())
                    }

                }
            }
        }
        es.start(wait = true)

    }

    private suspend fun doSchedule(
        courseGroups: java.util.ArrayList<CourseGroupsDataClass>,
        groupsCounter: Int,
        call: ApplicationCall
    ) {

        createScheduledClasses(courseGroups, call)
        filterByTeacher(courseGroups[groupsCounter], call)
        filterByEducationGroup(courseGroups[groupsCounter])
        executeBranchingSearch(call)
        result.add(ScheduledClass.all)
    }


    private fun filterByEducationGroup(courseGroups: CourseGroupsDataClass) {
        Slot.all.filter {
            !isInGroup(it, courseGroups.presentedCourses, courseGroups.courseYear)
        }.forEach { it.selected = 0 }
    }

    private fun isInGroup(slot: Slot, courseGroups: ArrayList<CourseDataClass>, entranceYear: Int): Boolean {
        courseGroups.forEach {
            groupsTotalFilledTimesList.filter { it.groupYear == entranceYear }.forEach {
                it.times.forEach {
                    if (it.dayName == slot.block.range.start.dayOfWeek.name) {
                        if (checkOneIsBetweenTwoByHourAndMinutes(
                                firstStartTimeMinute =
                                slot.block.timeRange.start.minute.toString(),
                                firstStartTimeHour = slot.block.timeRange.start.hour.toString(),
                                firstEndTimeMinute = slot.block.timeRange.endInclusive.minute.toString(),
                                firstEndTimeHour = slot.block.timeRange.endInclusive.hour.toString(),
                                secondStartTimeMinute = it.startTime.split(":")[1],
                                secondStartTimeHour = it.startTime.split(":")[0],
                                secondEndTimeMinute = it.endTime.split(":")[1],
                                secondEndTimeHour = it.endTime.split(":")[0]
                            )
                        ) {
                            return false
                        }

                    }
                }
            }
        }
        return true
    }

    private fun filterByTeacher(
        courseGroups: CourseGroupsDataClass,
        call: ApplicationCall
    ) {
        Slot.all.filter {
            !isTeacher(it, courseGroups.presentedCourses)
        }.forEach { it.selected = 0 }
    }

    private fun isTeacher(slot: Slot, courseGroups: ArrayList<CourseDataClass>): Boolean {
        courseGroups.forEach {
            if (it.courseName == slot.scheduledClass.name) {
                val teacherName = it.teacher
                val teacherTimes = teachersFilledTimesList.findLast {
                    it.teacherName == teacherName
                }!!.times
                teacherTimes.forEach {
                    if (it.dayName == slot.block.range.start.dayOfWeek.name) {
                        if (checkOneIsBetweenTwoByHourAndMinutes(
                                firstEndTimeHour = slot.block.timeRange.start.hour.toString(),
                                firstEndTimeMinute = slot.block.timeRange.endInclusive.minute.toString(),
                                firstStartTimeHour = slot.block.timeRange.start.hour.toString(),
                                firstStartTimeMinute = slot.block.timeRange.start.minute.toString(),
                                secondEndTimeHour = it.endTime.split(":")[0].toString(),
                                secondEndTimeMinute = it.endTime.split(":")[1].toString(),
                                secondStartTimeHour = it.startTime.split(":")[0].toString(),
                                secondStartTimeMinute = it.startTime.split(":")[1].toString()
                            )
                        ) return false
                    }
                }
            }
        }
        return true
    }

    private fun checkOneIsBetweenTwoByHourAndMinutes(
        firstStartTimeHour: String,
        firstEndTimeHour: String,
        secondStartTimeHour: String,
        secondEndTimeHour: String,
        firstStartTimeMinute: String,
        firstEndTimeMinute: String,
        secondStartTimeMinute: String,
        secondEndTimeMinute: String
    ): Boolean {
        if (firstStartTimeHour.toInt() > secondStartTimeHour.toInt() && firstEndTimeHour.toInt() < secondEndTimeHour.toInt()) {
            return true

        } else if (firstStartTimeHour.toInt() == secondStartTimeHour.toInt() && firstEndTimeHour.toInt() == secondEndTimeHour.toInt()) {
            if (secondEndTimeMinute.toInt().isAfter(firstEndTimeMinute.toInt())) {
                return true
            }
            if (firstStartTimeMinute.toInt().isAfter(secondStartTimeMinute.toInt())) {
                return true
            }
        } else if (firstStartTimeHour.toInt() > secondStartTimeHour.toInt() && firstEndTimeHour.toInt() == secondEndTimeHour.toInt()) {
            if (secondEndTimeMinute.toInt().isAfter(secondEndTimeMinute.toInt())) {
                return true
            }
        } else if (firstEndTimeHour.toInt() == secondStartTimeHour.toInt() && firstEndTimeHour.toInt() < secondEndTimeHour.toInt()) {
            if (firstStartTimeMinute.toInt().isAfter(secondStartTimeMinute.toInt())) {
                return true
            }
        }
        return false
    }

    private fun Int.isAfter(value: Int): Boolean {
        return (this >= value)
    }

    private fun createScheduledClasses(
        courseGroups: java.util.ArrayList<CourseGroupsDataClass>,
        call: ApplicationCall
    ) {
        scheduledClasses = ArrayList()
        courseGroups.forEach {
            it.presentedCourses.forEach {
                scheduledClasses.add(
                    ScheduledClass(
                        id = ++lastId,
                        name = it.courseName,
                        hoursLength = 1.5,
                        recurrenceGapDays = 0,
                        recurrences = it.recurrences
                    )
                )
            }
        }


    }


}


private fun resetTimeLimitations() = Slot.all.forEach { it.selected = null }


suspend fun executeBranchingSearch(call: ApplicationCall) {
    // Slot.all.filter { !isExisting(myres, it) }.forEach { it.selected = 0 }
    ScheduledClass.all.flatMap { it.slotsFixedToZero }.forEach { it.selected = 0 }
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

/*
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
*/

// classes


/*
    fun createABatchOfClasses(input: FirstClass, i: Int): Boolean {

        scheduledClasses = arrayListOf<ScheduledClass>()
        //  Slot.all.forEach { it.selected = null }
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
*/




