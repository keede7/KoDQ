package com.kodq

import com.kodq.plugins.configureHTTP
import com.kodq.plugins.configureRouting
import com.kodq.plugins.configureSerialization
import io.ktor.server.application.*
import io.ktor.server.netty.*

fun main(args: Array<String>) = EngineMain.main(args)

fun Application.module() {
    configureSerialization()
    configureHTTP()
    configureRouting()
}
