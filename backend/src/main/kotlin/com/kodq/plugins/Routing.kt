package com.kodq.plugins

import com.kodq.routes.styleRoutes
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        styleRoutes()
    }
}
