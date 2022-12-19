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
        maxId = _maxId
        page = _page.toInt()
        call.respond(HttpStatusCode.OK, "Scrapping restarted")
        restart()
    }
    get("/set-details"){
        val params = call.parameters
        val _cfrToken1 = params["c_token1"]
        val _userId1 = params["user_id1"]
        val _sessionId1 = params["session1"]

        val _cfrToken2 = params["c_token2"]
        val _userId2 = params["user_id2"]
        val _sessionId2 = params["session2"]
        _cfrToken1?.let {
            cfrToken1 = it
        }
        _userId1?.let {
            ds_userId1 = it
        }
        _sessionId1?.let {
            sessionId1 = it
        }
        _cfrToken2?.let {
            cfrToken2 = it
        }
        _userId2?.let {
            ds_userId2 = it
        }
        _sessionId2?.let {
            sessionId2 = it
        }
        call.respond(HttpStatusCode.OK, "Details set")
    }
}