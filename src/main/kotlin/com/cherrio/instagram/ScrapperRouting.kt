package com.cherrio.instagram

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.scrapperRouting(){
    routing {
        restarting()
    }
}

fun Route.restarting(){
    get("/restart"){
        val params = call.parameters
        val _maxId = params["max_id"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Provide max_ID")
        val _page = params["page"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Provide page")
        val _sessionId = params["session"]
        maxId = _maxId
        page = _page.toInt()
        _sessionId?.let {
            profileSessionId = it
        }
        call.respond(HttpStatusCode.OK, "Scrapping restarted")
        restart()
    }
}