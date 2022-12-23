package com.cherrio.plugins

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.ProxyBuilder.http
import io.ktor.client.engine.cio.*
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.network.sockets.*
import io.ktor.serialization.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import java.net.Proxy

val client = HttpClient(Java){
    BrowserUserAgent()
    install(ContentNegotiation){
        json(json = Json { 
            ignoreUnknownKeys = true
            isLenient = true
            prettyPrint = true
        })
    }
    engine {
        proxy = ProxyBuilder.http("http://50.233.228.147:8080/")
    }
}

inline fun <T> doNetworkCall(block: () -> Resource<T>): Resource<T> {
    return try {
        block()
    }catch (e: ServerResponseException){
        Resource.Failure(e.localizedMessage ?: "An error occurred", e.response.status.value)
    }catch (e: ClientRequestException){
        Resource.Failure(e.localizedMessage ?: "An error occurred", e.response.status.value)
    }catch (e: ResponseException){
        Resource.Failure(e.localizedMessage ?: "An error occurred", e.response.status.value)
    }catch (e: JsonConvertException){
        println("Error: ${e.cause}")
        Resource.Failure(e.localizedMessage ?: "An error occurred", 101)
    }
}

sealed class Resource <T> (val isSuccessful: Boolean,
                           val data: T? = null,
                           val errorCode: Int? = null,
                           val error: String? = null){
    data class Success<T> (val response: T): Resource<T>(data = response, isSuccessful = true)
    data class Failure<T>(val cause: String, val code: Int): Resource<T>(error = cause, isSuccessful = false, errorCode = code)
}