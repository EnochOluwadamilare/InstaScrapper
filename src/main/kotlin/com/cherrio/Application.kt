package com.cherrio

import com.cherrio.instagram.scrapperRouting
import io.ktor.server.application.*
import com.cherrio.plugins.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import java.time.Duration

fun main(args: Array<String>): Unit =
        io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    configureRouting()
    configureLogging()
    scrapperRouting()
    launch {
        while (true){
            refreshGoogleToken()
            delay(Duration.ofMinutes(45))
        }
    }

}

