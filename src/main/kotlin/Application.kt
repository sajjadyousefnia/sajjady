package com.example

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.ktor.application.*
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.callIdMdc
import io.ktor.gson.gson
import io.ktor.http.ContentType
import io.ktor.request.receive
import io.ktor.request.receiveParameters
import io.ktor.response.*
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.routing.Routing
import io.ktor.routing.get


//

import io.ktor.routing.post
import io.ktor.util.pipeline.PipelineContext
import org.slf4j.event.Level
import java.lang.reflect.Modifier
import java.text.DateFormat
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.GsonConverter
import io.ktor.request.queryString
import io.ktor.routing.*
import io.ktor.util.filter
import io.netty.handler.codec.http.HttpServerCodec

// this is true
val port = System.getenv("PORT")?.toInt() ?: 23567
// val port = 8080
var scheduledClasses = ArrayList<ScheduledClass>()
var teachersFreeTimesList = arrayListOf<TeachersTotalFreeTimesDataClass>()
var teachersAssignedTimesList = arrayListOf<TeachersTotalAssignedTimesDataClass>()
// var groupsTotalAssignedTimesList = arrayListOf<GroupsTotalFilledTimes>()
val roomsFilledTimes = arrayListOf<RoomsDatalasses>()
var lastId = 0
val groupFilledTimes = arrayListOf<GroupDataClass>()
var numberOfClasses = 0
var result = arrayListOf<ArrayList<ScheduledClass>>()
var currentClass = 0
val outputList = mutableListOf<Slot>()
val gson: Gson = GsonBuilder().setPrettyPrinting().create()

object MyClass {
    @JvmStatic
    fun main(args: Array<String>) {
        val es = embeddedServer(Netty, port, configure = {
            httpServerCodec = { HttpServerCodec(409600, 819200, 819200) }
        }) {
            routing {
                // install(CallLogging)
                install(CallLogging) {
                    level = Level.TRACE
                    callIdMdc("X-Request-ID")
                }
                install(DefaultHeaders)
                // This feature enables compression automatically when accepted by the client.
                install(Compression)
                // Automatic '304 Not Modified' Responses
                install(ConditionalHeaders)
                // Supports for Range, Accept-Range and Content-Range headers
                install(PartialContent)
                // For each GET header, adds an automatic HEAD handler (checks the headers of the requests
                // without actually getting the payload to be more efficient about resources)
                install(AutoHeadResponse)
                install(ContentNegotiation) {
                    gson {

                        setLenient()
                        register(ContentType.Application.Json, GsonConverter())
                        setPrettyPrinting()
                    }
                }
                get("/{text}") {
                    try {
                        resetValues()
                        // call.response.header("sasasas", 2019)

                       // call.respond(call.request.queryParameters["requested"].toString())
                        //  call.respond(call.receive<GeneralList>().toString())

                        val myres = parseJson(call.request.queryParameters["requested"].toString())
                       // call.respond(myres.toString())
                        numberOfClasses = myres.generalList.classesCount

                        startToSchedule(this, myres)

                    } catch (e: Exception) {
                        call.respond(e.toString() + "\n" /*+ e.localizedMessage.toString() + "\n" + e.printStackTrace()*/)
                    }
                }
            }
        }
        es.start(wait = true)
    }

    private fun resetValues() {
        teachersAssignedTimesList.clear()
        roomsFilledTimes.clear()
        groupFilledTimes.clear()
        numberOfClasses = 0
        outputList.clear()
        lastId = 0
        result.clear()
    }

    private suspend fun startToSchedule(
        pipelineContext: PipelineContext<Unit, ApplicationCall>,
        myres: FirstClass
    ) {
        applyPrimaryTeachersNotUsableTimes(myres)
        var currentCourseIndex = 0
        var pastScheuledSlots = listOf<Slot>()
        var pastList = listOf<CourseDataClass>()
        val motherCourses = myres.generalList.courseGroups.flatMap { it.presentedCourses }.toMutableList()
        pipelineContext.application.log.trace(motherCourses.toString())
        for (classesCounter in 0 until myres.generalList.classesCount) {
            try {
                // pipelineContext.context.respond("sajjad")
                for (coursesCounter in currentCourseIndex until motherCourses.size) {
                    val listForTest = motherCourses.subList(currentCourseIndex, motherCourses.size)
                    createScheduledClasses(listForTest)
                    applyTeachersLimitations()
                    applyRoomLimitations()
                    applyGroupLimitations(listForTest)
                    executeBranchingSearch()
                    pastScheuledSlots = Slot.all
                    // pipelineContext.context.respond(pastScheuledSlots)
                    pipelineContext.context.respond(
                        Slot.all.filter { it.selected == 1 }.toString()
                    )
                    extractList(pastScheuledSlots, pastList)
                    saveTeachersLimitations(pastList)
                    saveRoomLimitations(classesCounter, pastList)
                    saveGroupLimitations(pastList)
                }
                outputList.addAll(Slot.all.filter { it.selected == 1 })
                scheduledClasses.clear()
                Slot.all.forEach { it.selected = null }
            } catch (e: Exception) {
                outputList.addAll(Slot.all.filter { it.selected == 1 })
                scheduledClasses.clear()
                Slot.all.forEach { it.selected = null }
            }
        }

        var outPutString = ""
        outputList.forEach {
            outPutString += ("\n" + it.toString() + "\n")
        }
        //   outputList.forEach { outPutString += (it.slots[0].block) };

/*
        outputList.forEach {
            outPutString += ("${it.name}- ${it.daysOfWeek.joinToString("/")} ${it.start.toLocalTime()}-${it.end.toLocalTime()}")
        }
*/
        pipelineContext.context.respond(outPutString)
    }


    private fun saveGroupLimitations(pastList: List<CourseDataClass>) {
        pastList.forEach {
            groupFilledTimes.add(
                GroupDataClass(
                    it.groupYear,
                    JustTimeDataCLass(it.startTime, it.endTime, it.dayname)
                )
            )
        }
    }

    private fun saveTeachersLimitations(pastList: List<CourseDataClass>) {
        pastList.forEach {
            teachersAssignedTimesList.add(
                TeachersTotalAssignedTimesDataClass(
                    it.teacher,
                    JustTimeDataCLass(it.startTime, it.endTime, it.dayname)
                )
            )
        }

    }

    private fun saveRoomLimitations(classNumber: Int, pastList: List<CourseDataClass>) {
        pastList.forEach {
            roomsFilledTimes.add(
                RoomsDatalasses(
                    classNumber, JustTimeDataCLass(
                        startTime = it.startTime,
                        dayName = it.dayname,
                        endTime = it.endTime
                    )
                )
            )
        }


    }

    private fun applyGroupLimitations(listForTest: MutableList<CourseDataClass>) {
        Slot.all.forEach {
            val teacherValue = it
            val isListExisting = groupFilledTimes.filter {
                teacherValue.block.range.start.dayOfWeek.name == it.data.dayName
            }.filter {
                checkOneIsBetweenTwoByHourAndMinutes(
                    firstStartTimeHour = teacherValue.block.range.start.hour.toString(),
                    firstStartTimeMinute = teacherValue.block.range.start.minute.toString(),
                    firstEndTimeHour = teacherValue.block.range.endInclusive.hour.toString(),
                    firstEndTimeMinute = teacherValue.block.range.endInclusive.minute.toString(),
                    secondStartTimeHour = it.data.startTime.split(":")[0],
                    secondStartTimeMinute = it.data.startTime.split(":")[1],
                    secondEndTimeHour = it.data.endTime.split(":")[0],
                    secondEndTimeMinute = it.data.endTime.split(":")[1]
                )
            }.isNullOrEmpty()
            if (!isListExisting)
                it.selected = 0
        }


    }

    private fun applyRoomLimitations(
    ) {
        Slot.all.forEach {
            val teacherValue = it
            val isListExisting = roomsFilledTimes.filter {
                teacherValue.block.range.start.dayOfWeek.name == it.data.dayName
            }.filter {
                checkOneIsBetweenTwoByHourAndMinutes(
                    firstStartTimeHour = teacherValue.block.range.start.hour.toString(),
                    firstStartTimeMinute = teacherValue.block.range.start.minute.toString(),
                    firstEndTimeHour = teacherValue.block.range.endInclusive.hour.toString(),
                    firstEndTimeMinute = teacherValue.block.range.endInclusive.minute.toString(),
                    secondStartTimeHour = it.data.startTime.split(":")[0],
                    secondStartTimeMinute = it.data.startTime.split(":")[1],
                    secondEndTimeHour = it.data.endTime.split(":")[0],
                    secondEndTimeMinute = it.data.endTime.split(":")[1]
                )
            }.isNullOrEmpty()
            if (!isListExisting)
                it.selected = 0
        }

    }

    private fun applyTeachersLimitations() {
        Slot.all.forEach {
            val teacherValue = it
            val isListExisting = teachersAssignedTimesList.filter {
                teacherValue.block.range.start.dayOfWeek.name == it.time.dayName
            }.filter {
                checkOneIsBetweenTwoByHourAndMinutes(
                    firstStartTimeHour = teacherValue.block.range.start.hour.toString(),
                    firstStartTimeMinute = teacherValue.block.range.start.minute.toString(),
                    firstEndTimeHour = teacherValue.block.range.endInclusive.hour.toString(),
                    firstEndTimeMinute = teacherValue.block.range.endInclusive.minute.toString(),
                    secondStartTimeHour = it.time.startTime.split(":")[0],
                    secondStartTimeMinute = it.time.startTime.split(":")[1],
                    secondEndTimeHour = it.time.endTime.split(":")[0],
                    secondEndTimeMinute = it.time.endTime.split(":")[1]
                )
            }.isNullOrEmpty()
            if (!isListExisting)
                it.selected = 0
        }
    }
}

private fun extractList(
    pastScheuledSlots: List<Slot>,
    pastList: List<CourseDataClass>
) {
    pastScheuledSlots.filter { it.selected == 1 }.forEach {
        pastList.filter { it.courseName == it.courseName }[0].apply {
            dayname = it.block.range.start.dayOfWeek.toString()
            endTime =
                it.block.timeRange.endInclusive.hour.toString() + ":" + it.block.timeRange.endInclusive.minute.toString();
            startTime = it.block.timeRange.start.hour.toString() + ":" + it.block.timeRange.start.minute.toString();
        }
    }
}


private fun startToScheduleForCurrentYear(myres: FirstClass) {
    for (courseCounter in 0 until myres.generalList.courseGroups.size) {
        try {
            scheduledClasses = ArrayList()
            executeBranchingSearch()
            currentClass++
        } catch (e: Exception) {
            break
        }
    }
}

private fun applyPrimaryTeachersNotUsableTimes(myres: FirstClass) {
    myres.generalList.courseGroups.forEach {
        it.presentedCourses.forEach {
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
}

private fun calculateTeachersLimitations(myres: FirstClass) {
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

}

private fun parseJson(resText: String): FirstClass {
    return gson.fromJson(resText, FirstClass::class.java)

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

/*
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
*/

/*
    private suspend fun doSchedule(
    courseGroups: java.util.ArrayList<CourseGroupsDataClass>,
    groupsCounter: Int,
    call: ApplicationCall
) {
    // createScheduledClasses(courseGroups, call)
    filterByTeacher(courseGroups[groupsCounter], call)
    filterByEducationGroup(courseGroups[groupsCounter])
    for (roomNumber in 0..numberOfClasses) {


    }
    excludeTeachersTimes(courseGroups[groupsCounter])
    result.add(ScheduledClass.all)
}
*/


/*
   private fun filterByEducationGroup(courseGroups: CourseGroupsDataClass) {
    Slot.all.filter {
        !isInGroup(it, courseGroups.presentedCourses, courseGroups.courseYear)
    }.forEach { it.selected = 0 }
}
*/

/*
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
*/

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

private fun checkThereIsClashBetweenTwoTime(
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
    courseGroups: MutableList<CourseDataClass>
) {
    // ScheduledClass.all.clear()
    courseGroups.forEach {
        val courseValue = it
        scheduledClasses.add(
            ScheduledClass(
                id = ++lastId,
                name = it.courseName,
                hoursLength = 1.5,
                recurrenceGapDays = 0,
                recurrences = it.recurrences
                , teacher = courseValue.teacher

            )
        )
    }
    // Slot.all.forEach { it.selected = null }

}


fun executeBranchingSearch() {
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
}








