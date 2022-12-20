package com.cherrio.instagram

import com.cherrio.ifNull
import com.cherrio.instagram.models.*
import com.cherrio.plugins.client
import com.cherrio.sheetsdb.database.create
import com.cherrio.sheetsdb.init.SheetsDb
import com.cherrio.sheetsdb.init.getTable
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Duration.Companion.minutes

var maxId = ""
var page = 0
val tag = "abeokutavendors"

val sheetDb = SheetsDb {
    bearerToken =
        "ya29.a0AeTM1idDgIb_hy9Fv_tJ16dlS2pUAjCwiugdWPblYOhlAEvm3yNRYvh4wuCpzLx_CaCyaqeqCUnrw_Ec8C2uE2r6VTygWzwB8tYm3c4jBVcyLXKG8PIRvTxFexqHgBXCVu2hHmiZQLv1Ka_BpiznlQxaA5mz-x0nTgaCgYKAeISAQASFQHWtWOmyqT0Rt1DeXUGJzm1EoNiXw0169"
    sheetId = "1YmBiVCmYn2fn15wmmy_Ex6aOGNyC5wv991vTAZkZby8"
}
var table = sheetDb.getTable<AbeokutaVendors>()

var credentials = listOf<Credentials>()
var index = 0


suspend fun restart(){
    while (true) {
        val users2 = getOtherPages()
        val userDetails = users2.get()
        userDetails.forEach {
            table.create(it.map())
        }
        println("Page $page done....")
        println("Delaying for 2 minutes")
        delay(2.minutes)
    }
}

suspend fun List<UserAndId>.get() = coroutineScope {
    map {
        async { getUserDetails(it.id) }
    }.awaitAll()
}

suspend fun getUserDetails(userId: String):User {
    val credentials = credentials[index]
    delay(1000)
    val response = client.get("https://i.instagram.com/api/v1/users/$userId/info/") {
        header("x-ig-app-id", credentials.appId)
        header(
            "cookie", "sessionid=${credentials.sessionId}; csrftoken=${credentials.crfToken}; ds_user_id=${credentials.userId}"
        )
    }
    return if (!response.status.isSuccess()){
        val error = response.bodyAsText()
        println(error)
        throw Exception(error)
    }else {
        incrementIndex()
        val userResponse = response.body<UserResponse>()
        userResponse.user
    }
}

//private suspend fun getFirstPage(): List<UserAndId> {
//    val response = client.get("https://www.instagram.com/api/v1/tags/web_info") {
//        url {
//            parameters.append("tag_name", tag)
//        }
//        header("x-ig-app-id", appId)
//        header(
//            "cookie",
//            "sessionid=$sessionId1; ds_user_id=47362721982"
//        )
//        header("accept", "*/*")
//    }
//    //println(response.bodyAsText())
//    val tagResponse = response.body<TagResponse>()
//    maxId = tagResponse.data.recent.nextMaxId
//    page = tagResponse.data.recent.nextPage
//    println("PAGE: $page")
//    println("MAX_ID: $maxId")
//    return tagResponse.data.recent.sections.map {
//        it.layoutContent.medias.map { mediaXX ->
//            UserAndId(mediaXX.media.user.pk, mediaXX.media.user.username)
//        }.distinct()
//    }.flatten().distinct()
//
//    //return emptyList()
//}

suspend fun getOtherPages(): List<UserAndId> {
    val credentials = credentials[index]
    val response = client.submitForm(
        url = "https://www.instagram.com/api/v1/tags/$tag/sections/",
        formParameters = Parameters.build {
            append("include_persistent", "0")
            append("max_id", maxId)
            append("page", page.toString())
            append("surface", "grid")
            append("tab", "recent")
        }
    ){
        header("x-ig-app-id", credentials.appId)
        header(
            "cookie",
            "sessionid=${credentials.sessionId}; ds_user_id=${credentials.userId}; csrftoken=${credentials.crfToken}"
        )
        header("x-csrftoken", credentials.crfToken)
    }
    return if (!response.status.isSuccess()){
        val error = response.bodyAsText()
        if (error.contains("Oops, an error occurred.")){
            delay(5000)
            getOtherPages()
        }else{
            println(error)
            throw Exception(error)
        }
    }else {
        incrementIndex()
        val body = response.body<Recent>()
        maxId = body.nextMaxId
        page = body.nextPage
        println("PAGE: $page")
        println("MAX_ID: $maxId")
        println("Index: $index")
        body.sections.map {
            it.layoutContent.medias.map { mediaXX ->
                UserAndId(mediaXX.media.user.pk, mediaXX.media.user.username)
            }.distinct()
        }.flatten().distinct()
    }
}

fun incrementIndex(){
    if (index == 5)
        index = 0
    else index++
}

fun User.map() =
    AbeokutaVendors(
        username = username,
        profileUrl = "https://www.instagram.com/$username",
        fullName = fullName,
        mediaCount = mediaCount,
        followers = followerCount,
        following = followingCount,
        bio = biography,
        email = publicEmail.ifNull { "None" },
        countryCode = publicPhoneCountryCode.ifNull { "None" },
        phone = contactPhoneNumber.ifNull { "None" },
        isBusiness = if (isBusiness) "Business" else "Personal",
        pageName = pageName?: "None",
        whatsappNumber = whatsappNumber?: "None",
        url = externalUrl,
        address = addressStreet.ifNull { "None" },
        city = cityName.ifNull { "None" }
    )

data class UserAndId(
    val id: String,
    val username: String
)
@Serializable
data class Credentials(
    val sessionId: String,
    val userId: String,
    val crfToken: String,
    val appId: String
)

@Serializable
data class AbeokutaVendors(
    @SerialName("username")
    val username: String,
    @SerialName("profile_url")
    val profileUrl: String,
    @SerialName("full_name")
    val fullName: String,
    @SerialName("media_count")
    val mediaCount: Int,
    @SerialName("followers")
    val followers: Int,
    @SerialName("following")
    val following: Int,
    @SerialName("bio")
    val bio: String,
    @SerialName("email")
    val email: String,
    @SerialName("country_code")
    val countryCode: String,
    @SerialName("phone")
    val phone: String,
    @SerialName("is_business")
    val isBusiness: String,
    @SerialName("page_name")
    val pageName: String,
    @SerialName("whatsapp_number")
    val whatsappNumber: String,
    @SerialName("url")
    val url: String,
    @SerialName("address")
    val address: String,
    @SerialName("city")
    val city: String,
)

