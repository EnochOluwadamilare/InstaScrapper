package com.cherrio

import com.cherrio.instagram.sheetDb
import com.cherrio.instagram.table
import com.cherrio.plugins.Resource
import com.cherrio.plugins.client
import com.cherrio.plugins.doNetworkCall
import com.cherrio.sheetsdb.init.getTable
import io.ktor.client.call.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.server.application.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


suspend fun refreshGoogleToken(){
    val token = getRefreshToken()
    sheetDb.setBearerToken(token)
}


private suspend fun getRefreshToken(): String{
    val request = doNetworkCall {
        val response = client.submitForm("https://www.googleapis.com/oauth2/v4/token",
            formParameters = Parameters.build {
                append("grant_type", "refresh_token")
                append("clientId", "414805296428-detqeq7urb2krmr8dicu4b1p0th9ncil.apps.googleusercontent.com")
                append("client_secret", "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")
                append("refresh_token", "1//03pDmZ6uCcbZ_CgYIARAAGAMSNwF-L9IrmRTBa8TtiJHKWEG5HY-f6h3DtOcB6X8t9JIqmOW60dNLXoBiOZdZ-mrBIgFWyIqZNBI")
            },
            encodeInQuery = true
        ){
            method = HttpMethod.Post
        }
        Resource.Success(response.body<ResponseGD>())
    }
    return if (request.isSuccessful){
        println(request.data!!.accessToken.substring(0,10))
        request.data.accessToken
    }else{
        println("Couldn't get token")
        throw IllegalStateException("Couldn't get token")
    }

}

@Serializable
data class ResponseGD(
    @SerialName("access_token") val accessToken: String
)