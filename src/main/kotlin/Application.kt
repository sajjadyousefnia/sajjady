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
                    val myres = Gson().fromJson(resText, ExampleDataClass::class.java)
                    call.respond(myres.hello)

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

}

