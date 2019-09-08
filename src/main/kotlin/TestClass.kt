package com.example

import com.example.MyClass.createABatchOfClasses
import com.google.gson.Gson
import io.ktor.application.call
import io.ktor.response.respond

fun main(args: Array<String>) {
val x = "{\n" +
        "  \"requested\": [\n" +
        "    {\n" +
        "      \"courseName\": \"math\",\n" +
        "      \"data\": [\n" +
        "        {\n" +
        "          \"day\": \"WEDNESDAY\",\n" +
        "          \"repeats\": [\n" +
        "            {\n" +
        "              \"startTime\": \"10:30\",\n" +
        "              \"endTime\": \"16:00\"\n" +
        "            }\n" +
        "          ]\n" +
        "        }\n" +
        "      ],\n" +
        "      \"recurrences\": 1\n" +
        "    },\n" +
        "    {\n" +
        "      \"courseName\": \"math2\",\n" +
        "      \"data\": [\n" +
        "        {\n" +
        "          \"day\": \"WEDNESDAY\",\n" +
        "          \"repeats\": [\n" +
        "            {\n" +
        "              \"startTime\": \"10:30\",\n" +
        "              \"endTime\": \"16:00\"\n" +
        "            }\n" +
        "          ]\n" +
        "        }\n" +
        "      ],\n" +
        "      \"recurrences\": 1\n" +
        "    }\n" +
        "  ]\n" +
        "}"
/*
    val x = "{\n" +
            "  \"requested\": [\n" +
            "    {\n" +
            "      \"courseName\": \"math\",\n" +
            "      \"data\": [\n" +
            "        {\n" +
            "          \"day\": \"WEDNESDAY\",\n" +
            "          \"repeats\": [\n" +
            "            {\n" +
            "              \"startTime\": \"08:00\",\n" +
            "              \"endTime\": \"10:00\"\n" +
            "            }\n" +
            "          ]\n" +
            "        },\n" +
            "        {\n" +
            "          \"day\": \"MONDAY\",\n" +
            "          \"repeats\": [\n" +
            "            {\n" +
            "              \"startTime\": \"8:00\",\n" +
            "              \"endTime\": \"10:00\"\n" +
            "            }\n" +
            "          ]\n" +
            "        },\n" +
            "        {\n" +
            "          \"day\": \"TUESDAY\",\n" +
            "          \"repeats\": [\n" +
            "            {\n" +
            "              \"startTime\": \"08:00\",\n" +
            "              \"endTime\": \"16:00\"\n" +
            "            }\n" +
            "          ]\n" +
            "        }\n" +
            "      ],\n" +
            "      \"recurrences\": 3\n" +
            "    }\n" +
            "  ]\n" +
            "}"
*/
    try {
        val resText = x
        val myres = Gson().fromJson(resText, FirstClass::class.java)
        val testValue = createABatchOfClasses(myres, 0)
       // println(testValue.size)
        // MyClass.executeBranchingSearch(myres)
        //  var resForRes = ""
/*
        ScheduledClass.all.sortedBy { it.start }.forEach {
            resForRes += (
                    "${it.name}- ${it.daysOfWeek.joinToString("/")}" +
                            " ${it.start.toLocalTime()}-${it.end.toLocalTime()}")
        }
*/
        //   println(resForRes)

    } catch (e: Exception) {

        println(e.toString() + "\n" + e.localizedMessage.toString())
    }
}