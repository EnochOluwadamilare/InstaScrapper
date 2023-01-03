package com.cherrio.servers

import com.cherrio.instagram.begin
import com.cherrio.instagram.login
import com.cherrio.instagram.maxId
import com.cherrio.instagram.page
import com.cherrio.sheetsdb.init.SheetsDb
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Application.railwayServerRouting(){
    routing {
        route("/aws"){
            railway()
        }
    }
}

fun Route.railway(){
    get("/restart"){
        val params = call.parameters
        val maxId = params["max_id"] ?: ""
        val page = params["page"] ?: "2"
    }

    get("/init"){
        val params = call.parameters
        val tag = params["tag"]?: ""
        call.respond(HttpStatusCode.OK,"Ok")
        login()
        begin(tag)
    }

}