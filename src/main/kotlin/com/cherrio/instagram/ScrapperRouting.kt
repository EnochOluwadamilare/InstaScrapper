package com.cherrio.instagram

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
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
        maxId = _maxId
        page = _page.toInt()
        call.respond(HttpStatusCode.OK, "Scrapping restarted")
        restart()
    }
    get("/set-details"){
       credentials = call.receive<List<Credentials>>()
        call.respond(HttpStatusCode.OK, "Details set")
    }
}