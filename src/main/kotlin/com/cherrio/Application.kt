package com.cherrio

import com.cherrio.instagram.login
import com.cherrio.instagram.scrapperRouting
import io.ktor.server.application.*
import com.cherrio.plugins.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import kotlinx.serialization.json.Json
import java.nio.file.Paths
import java.time.Duration
import kotlin.io.path.exists
import kotlin.time.Duration.Companion.minutes

fun main(args: Array<String>): Unit = EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    configureSerialization()
    configureRouting()
    configureLogging()
    scrapperRouting()
    val railway = System.getenv("RAILWAY")
    if (railway != null) {
        launch {
            while (true) {
                refreshGoogleToken()
                delay(Duration.ofMinutes(45))
            }
        }
    }
    println("State exists: ${Paths.get("state.json").exists()}")
}

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
            prettyPrint = true
        })
    }

    routing {
        get("/json/kotlinx-serialization") {
            call.respond(mapOf("hello" to "world"))
        }
    }
}