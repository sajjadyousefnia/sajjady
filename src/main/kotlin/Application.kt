package com.example


//

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.GsonConverter
import io.ktor.gson.gson
import io.ktor.http.ContentType
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.util.pipeline.PipelineContext
import io.netty.handler.codec.http.HttpServerCodec
import org.slf4j.event.Level
import java.time.LocalTime

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
val outputList = arrayListOf<Slot>()
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
                        // call.response.header("sasasas", 2019)

                        // call.respond(call.request.queryParameters["requested"].toString())
                        //  call.respond(call.receive<GeneralList>().toString())

                        val myres = parseJson(call.request.queryParameters["requested"].toString())
                        resetValues()
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
        var pointer = 0
        val motherCourses = myres.generalList.courseGroups.flatMap { it.presentedCourses }
        for (classes in 1..myres.generalList.classesCount) {
            classesLoop@ for (counter in pointer + 2 until motherCourses.size) {
                try {
                    // scheduledClasses.clear()
                    // Slot.all.forEach { it.selected = null }
                    val listForTest = motherCourses.subList(pointer, counter)
                    createScheduledClasses(listForTest, classes, pipelineContext)

                    applyPrimaryTeachersNotUsableTimes(myres.generalList.teachersNames)
                    // applyTeachersLimitations()
                    // pipelineContext.context.application.log.trace("\n ${ScheduledClass.all} + hasan ${Slot.all.filter { it.selected == null }.size} \n")

                    executeBranchingSearch(pipelineContext)
                    // saveTeachersLimitations()
                    // outputList.removeIf { it.scheduledClass.classRoom == classes }
                    pipelineContext.application.log.trace(outputList.toString() + "tracing")
                    outputList.clear()
                    outputList.addAll(Slot.all.filter { it.selected == 1 })
                    Slot.all.forEach { it.selected = null }
                } catch (e: Exception) {
                    pipelineContext.application.log.trace(e.message.toString() + "counter is $counter and pointer is $pointer and slot size is ${Slot.all.filter { it.scheduledClass.teacher != "zahmatkesh" }.size}")
                    // pipelineContext.application.log.trace()
                    // pipelineContext.context.respond("counter is $counter and pointer is $pointer")
                    pointer = counter + 1
                    // pipelineContext.context.respond(e.toString())
                    break@classesLoop
                }
                continue@classesLoop
            }
            //   pipelineContext.context.respond(motherCourses.size.toString())
            //  outputList.addAll(Slot.all.filter { it.selected == 1 })
        }

/*
        for (classesCounter in 0 until myres.generalList.classesCount) {
            try {
                for (coursesCounter in currentCourseIndex until motherCourses.size) {
                    scheduledClasses = ArrayList()
                    val listForTest = motherCourses.subList(currentCourseIndex, motherCourses.size - 1)
                    createScheduledClasses(listForTest)
                    applyPrimaryTeachersNotUsableTimes(motherCourses, myres.generalList.teachersNames)

                    executeBranchingSearch()
                }
                outputList.addAll(Slot.all.filter { it.selected == 1 })
                scheduledClasses = ArrayList()
                Slot.all.forEach { it.selected = null }
            } catch (e: Exception) {
                outputList.addAll(Slot.all.filter { it.selected == 1 })
                scheduledClasses = ArrayList()
                Slot.all.forEach { it.selected = null }
                continue
            }
        }
*/

        //   outputList.forEach { outPutString += (it.slots[0].block) };

/*
        outputList.forEach {
            outPutString += ("${it.name}- ${it.daysOfWeek.joinToString("/")} ${it.start.toLocalTime()}-${it.end.toLocalTime()}")
        }
*/
        pipelineContext.context.respond(outputList.toString() + "sajjad")
    }

    private fun excludeNewTimesFoTeachers() {
        Slot.all.filter { it.selected == 1 }
            .forEach {
                teachersAssignedTimesList.add(
                    TeachersTotalAssignedTimesDataClass(
                        teacherName = it.scheduledClass.teacher,
                        time = JustTimeDataCLass(
                            startTime = it.block.timeRange.start.hour.toString() + ":" + it.block.timeRange.start.minute.toString(),
                            endTime = it.block.timeRange.endInclusive.hour.toString() + ":" + it.block.timeRange.endInclusive.minute.toString(),
                            dayName = it.block.range.start.dayOfWeek.name
                        )
                    )
                )
            }


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

    private fun saveTeachersLimitations() {
        Slot.all.filter { it.selected == 1 }.forEach {
            val teacher = it.scheduledClass.teacher
            teachersAssignedTimesList.add(
                TeachersTotalAssignedTimesDataClass(
                    teacherName = teacher,
                    time = JustTimeDataCLass
                        (
                        startTime = it.block.timeRange.start.hour.toString() + ":" +
                                it.block.timeRange.start.minute.toString()
                        , endTime = it.block.timeRange.endInclusive.hour.toString() + ":" +
                            it.block.timeRange.endInclusive.minute.toString(),
                        dayName = it.block.range.start.dayOfWeek.name
                    )

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

/*
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
*/

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
            if (!isListExisting) {
                it.selected = 0
            }
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
            // executeBranchingSearch(pipelineContext)
            currentClass++
        } catch (e: Exception) {
            break
        }
    }
}

private fun applyPrimaryTeachersNotUsableTimes(
    teachersNames: ArrayList<TeachersInfoDataClass>
) {
    Slot.all.forEach { it.selected = 0 }

    Slot.all.filter {
        val teacherName = it.scheduledClass.teacher
        val dayName = it.block.range.start.dayOfWeek.name
        val slot = it
        val teacherDataClass = teachersNames.findLast { it.teacherName == teacherName }

        teacherDataClass!!.openDays.any {
            (it.dayName == dayName && ((
                    LocalTime.of(
                        slot.block.timeRange.start.hour,
                        slot.block.timeRange.start.minute
                    )
                    ).isAfter(
                LocalTime.of(
                    it.startTime.split(":")[0].toInt(),
                    it.startTime.split(":")[1].toInt()
                )
            ) || (it.startTime.split(":")[0].toInt() == slot.block.timeRange.start.hour &&
                    it.startTime.split(":")[1].toInt() == slot.block.timeRange.start.minute)  /*  LocalTime.of(
                it.startTime.split(":")[0].toInt(),
                it.startTime.split(":")[1].toInt()
            ).equals(
                LocalTime.of(
                    slot.block.timeRange.start.hour,
                    slot.block.timeRange.start.hour
                )
            )*/) && (((
                    LocalTime.of(
                        slot.block.timeRange.endInclusive.hour,
                        slot.block.timeRange.endInclusive.minute
                    ).isBefore(
                        LocalTime.of(
                            it.endTime.split(":")[0].toInt(),
                            it.endTime.split(":")[1].toInt()
                        )
                    ) ||
                            it.endTime.split(":")[0].toInt() == slot.block.timeRange.endInclusive.hour &&
                            it.endTime.split(":")[1].toInt() == slot.block.timeRange.endInclusive.minute
                    ))))
        }
    }.forEach { it.selected = null }

/*
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
*/
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

/*
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
*/

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

private suspend fun createScheduledClasses(
    courseGroups: List<CourseDataClass>,
    classes: Int,
    pipelineContext: PipelineContext<Unit, ApplicationCall>
) {
/*
    if (courseGroups.size > 1) {
        pipelineContext.context.respond(courseGroups.toString())
    }
*/

    // ScheduledClass.all.clear()
    //scheduledClasses = ArrayList()
    // ScheduledClass.all.clear()
    scheduledClasses = arrayListOf<ScheduledClass>()
    courseGroups.forEach {
        val courseValue = it
        ScheduledClass.all.add(
            ScheduledClass(
                id = ++lastId,
                name = courseValue.courseName,
                hoursLength = 1.5,
                recurrenceGapDays = 0,
                recurrences = courseValue.recurrences
                , teacher = courseValue.teacher, classRoom = classes
            )
        )

    }
    // Slot.all.forEach { it.selected = null }
/*
    if (scheduledClasses.size > 1) {
        pipelineContext.context.respond(scheduledClasses.toString())
    }
*/


}


suspend fun executeBranchingSearch(pipelineContext: PipelineContext<Unit, ApplicationCall>) {
    // pipelineContext.context.respond(scheduledClasses.toString())
    // Slot.all.filter { !isExisting(myres, it) }.forEach { it.selected = 0 }
    pipelineContext.context.application.log.trace("slots number is ${Slot.all.size}")
    // ScheduledClass.all.flatMap { it.slotsFixedToZero }.forEach { it.selected = 0 }
    // pipelineContext.context.application.log.trace("\n ${ScheduledClass}")
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








