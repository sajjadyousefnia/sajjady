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
var teachersFreeTimesList = arrayListOf<TeachersTotalFreeTimesDataClass>()
var teachersAssignedTimesList = arrayListOf<TeachersTotalAssignedTimesDataClass>()
var groupsTotalAssignedTimesList = arrayListOf<GroupsTotalFilledTimes>()
var lastId = 0
var numberOfClasses = 0
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
                        numberOfClasses = 0
                        lastId = 0
                        result.clear()
                        val resText = call.request.queryParameters["requested"].toString()
                        val myres = Gson().fromJson(resText, FirstClass::class.java)
                        myres.generalList.teachersNames.forEach {
                            val teacherTimes: ArrayList<JustTimeDataCLass> = arrayListOf()
                            it.openDays.forEach {
                                teacherTimes.add(JustTimeDataCLass(it.startTime, it.endTime, it.dayName))
                            }
                            teachersFreeTimesList.add(
                                TeachersTotalFreeTimesDataClass(
                                    teacherName = it.teacherName,
                                    times = teacherTimes
                                )
                            )
                        }
                        numberOfClasses = myres.generalList.classesCount
                        for (groupsCounter in 0 until myres.generalList.entriesYears.size) {
                            for (classCounter in 0 until myres.generalList.classesCount) {
                                doSchedule(
                                    myres.generalList.courseGroups,
                                    groupsCounter, call, classCounter
                                )
                            }
                            excludeEducationalGroup(myres.generalList.entriesYears[groupsCounter])
                        }

                        var resultForPrint = ""
                        result.forEach {
                            resultForPrint += it.toString()
                        }
                        call.respond(resultForPrint)


/*
                        ScheduledClass.all.sortedBy { it.start }.forEach {
                            call.respond("${it.name}- ${it.daysOfWeek.joinToString("/")} ${it.start.toLocalTime()}-${it.end.toLocalTime()}")
                        }
*/


                    } catch (e: Exception) {
                        call.respond(e.toString() + "\n" /*+ e.localizedMessage.toString() + "\n" + e.printStackTrace()*/)
                    }
                }
            }
        }
        es.start(wait = true)

    }

    private fun excludeTeachersTimes(courseGroups: CourseGroupsDataClass) {
        courseGroups.presentedCourses.forEach {
            val teacherName = it.teacher
            val courseName = it.courseName
            val aimedScheduledClass = ScheduledClass.all.findLast {
                it.name == courseName
            }
            if (aimedScheduledClass != null) {
                teachersAssignedTimesList.add(
                    TeachersTotalAssignedTimesDataClass(
                        teacherName = teacherName,
                        time = JustTimeDataCLass(
                            aimedScheduledClass.start.hour.toString() + ":" + aimedScheduledClass.start.minute.toString(),
                            aimedScheduledClass.end.hour.toString() + ":" + aimedScheduledClass.end.minute.toString(),
                            aimedScheduledClass.start.dayOfWeek.name
                        )
                    )
                )
            }

        }
    }

    private fun excludeEducationalGroup(entryYear: Int) {
        val groupExcluceTimes = arrayListOf<JustTimeDataCLass>()
        result[result.lastIndex].forEach {
            groupExcluceTimes.add(
                JustTimeDataCLass(
                    it.start.hour.toString() + ":" + it.start.minute.toString(),
                    it.end.hour.toString() + ":" + it.end.minute.toString(),
                    it.start.dayOfWeek.name
                )
            )
        }
        groupsTotalAssignedTimesList.add(GroupsTotalFilledTimes(entryYear, groupExcluceTimes))

    }

    private suspend fun doSchedule(
        courseGroups: java.util.ArrayList<CourseGroupsDataClass>,
        groupsCounter: Int,
        call: ApplicationCall,
        classCounter: Int
    ) {
        createScheduledClasses(courseGroups, call)
        filterByTeacher(courseGroups[groupsCounter], call)
        filterByEducationGroup(courseGroups[groupsCounter])
        executeBranchingSearch(call, classCounter)
        excludeTeachersTimes(courseGroups[groupsCounter])
        result.add(ScheduledClass.all)
    }


    private fun filterByEducationGroup(courseGroups: CourseGroupsDataClass) {
        Slot.all.filter {
            !isInGroup(it, courseGroups.presentedCourses, courseGroups.courseYear)
        }.forEach { it.classIsSelected = 0 }
    }

    private fun isInGroup(slot: Slot, courseGroups: ArrayList<CourseDataClass>, entranceYear: Int): Boolean {
        courseGroups.forEach {
            groupsTotalAssignedTimesList.filter { it.groupYear == entranceYear }.forEach {
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
        }.forEach { it.classIsSelected = 0 }
    }

    private fun isTeacher(slot: Slot, courseGroups: ArrayList<CourseDataClass>): Boolean {
        courseGroups.forEach {
            if (it.courseName == slot.scheduledClass.name) {
                val teacherName = it.teacher
                val teacherTimes = teachersFreeTimesList.findLast {
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
                        ) return true
                    }
                }
            }
        }
        return false
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


private fun resetTimeLimitations() = Slot.all.forEach { it.classIsSelected = null }


suspend fun executeBranchingSearch(call: ApplicationCall, classCounter: Int) {
    // Slot.all.filter { !isExisting(myres, it) }.forEach { it.classIsSelected = 0 }
    ScheduledClass.all.flatMap { it.slotsFixedToZero }.forEach { it.classIsSelected = 0 }
    // Try to encourage most "constrained" slots to be evaluated first
    val sortedSlots = Slot.all.asSequence().filter { it.classIsSelected == null }./*sortedWith(
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
    var value = ""
    val solution = traverse()
    solution?.traverseBackwards?.forEach {
        value += it.toString()
        it.applySolution()
    } ?: throw Exception("Infeasible")


}








