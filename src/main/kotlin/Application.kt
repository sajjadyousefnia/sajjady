package com.example

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
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

val port = System.getenv("PORT")?.toInt() ?: 23567

object MyClass {
    @JvmStatic
    fun main(args: Array<String>) {
        embeddedServer(Netty, port) {
            routing {
                get("/") {
                    call.respond("morteza farhadi")
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

