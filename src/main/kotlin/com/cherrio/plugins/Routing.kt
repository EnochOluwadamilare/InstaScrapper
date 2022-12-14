package com.cherrio.plugins

import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import kotlinx.serialization.Serializable
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
        get("/link/{id}"){
            call.respondFile(file)
        }
        get("/"){
            call.respondText("fun String.clean(): Set<String>{\n" +
                    "    val stopwords = setOf(\"limited\",\"nigeria\",\"plc\",\"ltd\",\"company\",\"nig\",\"international\",\"int\",\"investment\")\n" +
                    "    return replace(Regex(\"[0-9]\"),\"\")\n" +
                    "        .replace(Regex(\"[:/;&^%\$#@!]\"),\" \")\n" +
                    "        .split(Regex(\"\\\\s\")).asSequence()\n" +
                    "        .map { it.replace(Regex(\"[^A-Za-z]\"),\"\").lowercase() }\n" +
                    "        .filter { it.length >= 3 && it !in stopwords }\n" +
                    "        .distinct()\n" +
                    "        .toSet()\n" +
                    "}")
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
