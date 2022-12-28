package com.cherrio.instagram

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

fun Application.scrapperRouting(){
    routing {
        restarting()
    }
}

fun Route.restarting(){
    get("/restart"){
        val params = call.parameters
        val _maxId = params["max_id"] ?: maxId
        val _page = params["page"] ?: page.toString()
        maxId = _maxId
        page = _page.toInt()
        call.respond(HttpStatusCode.OK, "Scrapping restarted")
        restart()

    }
    get("/refresh"){
        val username = call.parameters["user_id"] ?: ""
        refreshCookie(userId = username)
        call.respond(HttpStatusCode.OK,"Refreshed")
    }
    get("/set-details"){
       val creds = call.receive<Creds>()
        credentials = creds.credentials
        call.respond(HttpStatusCode.OK, "Details set")
    }
    get("/login"){
        val username = call.parameters["user_id"] ?: ""
        call.respond(HttpStatusCode.OK, login(username))
    }
    get("/begin"){
        index = 0
        call.respond(HttpStatusCode.OK, "Begin")
    }
}

@Serializable
data class Creds(
    val credentials: List<Credentials>
)