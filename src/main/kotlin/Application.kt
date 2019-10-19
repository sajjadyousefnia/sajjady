package com.example


//

import calculations.CourseSchedule
import calculations.Lecture
import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
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
import org.optaplanner.core.api.solver.SolverFactory
import org.slf4j.event.Level
import java.time.LocalDateTime
import java.time.LocalTime

// this is true
private val port = System.getenv("PORT")?.toInt() ?: 23567
// val port = 8080
private var teachersAssignedTimesList = arrayListOf<TeachersTotalAssignedTimesDataClass>()
// var groupsTotalAssignedTimesList = arrayListOf<GroupsTotalFilledTimes>()
private val roomsFilledTimes = arrayListOf<RoomsDatalasses>()
private var lastId = 0
private val groupFilledTimes = arrayListOf<GroupDataClass>()
private var numberOfClasses = 0
private var currentClass = 0
private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
private var unsolvedCourseSchedule: CourseSchedule? = null

private var openTimes: ArrayList<TeachersInfoDataClass>? = null
private var openCourses: MutableList<Pair<String, String>>? = null

object MyClass {

    @JvmStatic
    fun main(args: Array<String>) {
        val es = embeddedServer(Netty, port, configure = {
            httpServerCodec = { HttpServerCodec(409600, 819200, 819200) }
        }) {
            routing {
                // install(CalltheoLogging)
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
                        startToNewSchedule(this, myres)
                        //  startToSchedule(this, myres)

                    } catch (e: Exception) {
                        call.respond(
                            e.toString() + "\n" + e.printStackTrace()

                            /*+ e.localizedMessage.toString() + "\n" + e.printStackTrace()*/
                        )
                    }
                }
            }
        }
        es.start(wait = true)
    }

    private suspend fun startToNewSchedule(pipelineContext: PipelineContext<Unit, ApplicationCall>, myres: FirstClass) {
        val all = calculateAll()
        val motherCourses = mutableListOf<CourseDataClass>()
        myres.generalList.courseGroups.forEach {
            it.presentedCourses.forEach {
                for (counter in 0 until it.recurrences)
                    motherCourses.add(it)
            }
        }

        // openTimes = myres.generalList.teachersNames
/*
        openCourses = motherCourses.flatMap { courseDataClass ->
            mutableListOf(
                courseDataClass.groupYear.toString() to
                        courseDataClass.teacher
            ).toMutableList()
        }.toMutableList()
*/
        unsolvedCourseSchedule = CourseSchedule()
        unsolvedCourseSchedule!!.totalJson = myres
        for (i in motherCourses) {
            unsolvedCourseSchedule!!.lectureList.add(Lecture())
        }
        unsolvedCourseSchedule!!.daysList.addAll(
            setOf(
                "SATURDAY" to true,
                "SUNDAY" to true,
                "MONDAY" to true,
                "TUESDAY" to true,
                "WEDNESDAY" to true,
                "THURSDAY" to true
            )
        )
        all.filterNot { it.start.toLocalTime().isBefore(LocalTime.of(8, 0)) }
            .forEach { unsolvedCourseSchedule!!.periodList.add((it to true)) }

        for (counter in 0 until myres.generalList.classes.size) {
            unsolvedCourseSchedule!!.roomList.add(myres.generalList.classes[counter] to true)
        }
        // myres.generalList.entriesYears.forEach { unsolvedCourseSchedule!!.entriesList.add(it.toString() to true) }

        motherCourses.distinctBy { it.groupYear }.map { it.groupYear }
            .forEach { unsolvedCourseSchedule!!.entriesList.add(it to true) }



        myres.generalList.teachersNames.map { it.teacherName }
            .forEach { unsolvedCourseSchedule!!.teachersList.add(it to true) }
        val solverFactory = SolverFactory.createFromXmlResource<CourseSchedule>("courseScheduleSolverConfiguration.xml")

        val solver = solverFactory.buildSolver()
        val solvedCourseSchedule = solver.solve(unsolvedCourseSchedule)
        var valueForPrint = ""
        solvedCourseSchedule.lectureList.forEach { valueForPrint += it.day.toString() + it.teacher.toString() + it.entry.toString() + it.period.toString() + it.roomNumber.toString() }

        val listForExport = solvedCourseSchedule.lectureList.flatMap {
            mutableListOf(
                jsonObject(
                    "day" to it.day.first,
                    "teacher" to it.teacher.first,
                    "entry" to jsonArray(it.entry.first),
                    "room" to it.roomNumber.first,
                    "start" to it.period.first.start.toLocalTime().toString(),
                    "end" to it.period.first.endInclusive.toLocalTime().toString()
                )
            )
        }


        val daysOfWeek = listOf("SATURDAY", "SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY")
        val lengthComparator = Comparator { o1: JsonObject, o2: JsonObject ->
            val result = daysOfWeek.indexOf(o1.get("day").asString) - daysOfWeek.indexOf(o2.get("day").asString)
            if (result != 0) {
                return@Comparator result
            } else {
                -(o1.get("start").asString.replace(":", "").toInt()
                        -
                        o1.get("start").asString.replace(":", "").toInt())

            }
        }
        // listForExport.toSortedSet(lengthComparator)
        pipelineContext.call.respond(Gson().toJson(listForExport.sortedWith(lengthComparator)))
    }

    private fun calculateAll(): MutableList<ClosedRange<LocalDateTime>> {
        val all = generateSequence(operatingDates.start.atStartOfDay()) { dt ->
            dt.plusMinutes(90).takeIf { it.plusMinutes(90) <= operatingDates.endInclusive.atTime(18, 30) }
        }.map { it..it.plusMinutes(90) }.toMutableList()
        // all.forEach { println("${it}\n") }
        val tempALl = mutableListOf<ClosedRange<LocalDateTime>>()
        for (counter in 1..5) {
            val newAll = mutableListOf<ClosedRange<LocalDateTime>>()

            all.filter {
                LocalTime.of(it.endInclusive.hour, it.endInclusive.minute).plusMinutes((15 * counter).toLong())
                    .isBefore(LocalTime.of(18, 30))
            }.forEach {
                val startValue = it.start.plusMinutes((15 * counter).toLong())
                val endValue = it.endInclusive.plusMinutes((15 * counter).toLong())
                newAll.add(startValue..endValue)
            }
            tempALl.addAll(newAll)
        }
        all.addAll(tempALl)
        return all
    }

    private fun resetValues() {
        teachersAssignedTimesList.clear()
        roomsFilledTimes.clear()
        groupFilledTimes.clear()
        numberOfClasses = 0
        lastId = 0
    }

    private fun parseJson(resText: String): FirstClass {
        return gson.fromJson(resText, FirstClass::class.java)

    }


}




























