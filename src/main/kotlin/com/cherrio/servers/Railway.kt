package com.cherrio.servers

import com.cherrio.instagram.*
import com.cherrio.sheetsdb.init.SheetsDb
import io.ktor.http.*
import io.ktor.server.application.*
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
}