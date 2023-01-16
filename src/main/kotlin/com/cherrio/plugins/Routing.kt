package com.cherrio.plugins

import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

fun Application.configureRouting() {
    val file = File("./onelink/index.html")
    routing {
        get("/welcome") {
            val header = call.request.headers
            header.forEach { s, strings ->
                println("$s: $strings")
            }
            call.respondText("User-Agent: [Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.1 Safari/605.1.15]\n")
        }

        static("/"){
            staticRootFolder = File("onelink")
            files(".")
        }
        get("/"){
            call.respond(HttpStatusCode.OK,"Welcome")
        }
        get("/link/{id}"){
            call.respondFile(file)
        }

        post("/"){
            val data = call.receive<String>()
            println(data)
            call.respond(HttpStatusCode.OK)
        }

    }
}
@Serializable
data class Data(
    val data: String
)
