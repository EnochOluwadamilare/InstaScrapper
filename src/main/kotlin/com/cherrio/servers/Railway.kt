package com.cherrio.servers

import com.cherrio.instagram.*
import com.cherrio.sheetsdb.init.SheetsDb
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.nio.file.Paths


fun Application.railwayServerRouting(){
    routing {
        railway()
        aws()
    }
}

fun Route.railway(){
    get("/restart"){
        val params = call.parameters
        val maxId = params["max_id"] ?: ""
        val page = params["page"] ?: "2"
        val tag = params["tag"]
        call.respond(HttpStatusCode.OK,"Ok")
        restart(tag,maxId,page.toInt())
    }
    post("/credentials"){
        val body = call.receive<String>()
        cooky = body.toCookies()
        call.respond(HttpStatusCode.OK)
    }
    get("/next-tag"){
        val params = call.parameters
        val page = params["page"] ?: "2"
        val tag = params["tag"]!!
        setDetails(tag,page)
        call.respond(HttpStatusCode.OK,"Ok")
    }
}

fun Route.aws(){
    get("/login"){
        val response = login()
        if (response.isEmpty()){
            call.respond(HttpStatusCode.BadRequest, "An error occurred")
        }else {
            call.respond(HttpStatusCode.OK, response)
        }
    }
    get("/status"){
        call.respondFile(Paths.get("index.html").toFile())
    }
    post("/set-agent"){
        val body = call.receive<String>()
        userAgents = body.split("\n")
        call.respond(HttpStatusCode.OK)
    }
}