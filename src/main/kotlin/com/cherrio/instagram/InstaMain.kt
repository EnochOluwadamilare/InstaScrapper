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

var maxId = "QVFBOWNHeldZMWNfUlh2OHRFb0c5UGhxWTZBemdQbUM4d1BnRjBjV245eVNzcmxEczVwSW11OVhYbFRHc21NOXZTS25Xc3c3bEpSbEkxMzFJRjRTXzhHYg=="
var page = 186
val tag = "ibadanvendors"
var cfrToken = "55BUlSmVsTZexURxbsQrHeegdulzpRnq"

var ds_userId = ""

var sessionId = "56978350990%3AKI4VrU7DrgfEnp%3A27%3AAYd-HYbQCyWvOpg9WSTkDbUujQMWvPnnIzHzU0rO2g"
val appId = "936619743392459"

var profileSessionId = "47362721982%3AhHbk20BmZ9i9pD%3A25%3AAYfp8DDIhKbN0EpFhk4rDD7gy4yQdbiov3_mW17igA"

val sheetDb = SheetsDb {
    bearerToken =
        "ya29.a0AeTM1idDgIb_hy9Fv_tJ16dlS2pUAjCwiugdWPblYOhlAEvm3yNRYvh4wuCpzLx_CaCyaqeqCUnrw_Ec8C2uE2r6VTygWzwB8tYm3c4jBVcyLXKG8PIRvTxFexqHgBXCVu2hHmiZQLv1Ka_BpiznlQxaA5mz-x0nTgaCgYKAeISAQASFQHWtWOmyqT0Rt1DeXUGJzm1EoNiXw0169"
    sheetId = "1YmBiVCmYn2fn15wmmy_Ex6aOGNyC5wv991vTAZkZby8"
}
var table = sheetDb.getTable<IbadanVendors>()

//fun main() = runBlocking {
//    val sheetDb = SheetsDb {
//        bearerToken =
//            "ya29.a0AeTM1idDgIb_hy9Fv_tJ16dlS2pUAjCwiugdWPblYOhlAEvm3yNRYvh4wuCpzLx_CaCyaqeqCUnrw_Ec8C2uE2r6VTygWzwB8tYm3c4jBVcyLXKG8PIRvTxFexqHgBXCVu2hHmiZQLv1Ka_BpiznlQxaA5mz-x0nTgaCgYKAeISAQASFQHWtWOmyqT0Rt1DeXUGJzm1EoNiXw0169"
//        sheetId = "1YmBiVCmYn2fn15wmmy_Ex6aOGNyC5wv991vTAZkZby8"
//    }
//    val table = sheetDb.getTable<LagosVendors>()
////    val users = getFirstPage()
////    val userDetails = users.map {
////        async { getUserDetails(it.id) }
////    }.awaitAll()
////
////    println(userDetails.map { it.publicEmail })
////
////    userDetails.forEach {
////        table.create(it.map())
////    }
////    println("First page done....")
////
////    //next pages
////
//    while (true) {
//        val users2 = getOtherPages()
//        val userDetails = users2.get()
////        users2.forEach {
////            val user = getUserDetails(it.id)
////            table.create(user.map())
////        }
//        userDetails.forEach {
//            table.create(it.map())
//        }
//        println("Page $page done....")
//        println("Delaying for 2 minutes")
//        delay(2.minutes)
//    }
//
//    //val u = getOtherPages()
//}

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
    delay(1000)
    val response = client.get("https://i.instagram.com/api/v1/users/$userId/info/") {
        header("x-ig-app-id", appId)
        header(
            "cookie", "sessionid=$sessionId"
        )
    }
    if (!response.status.isSuccess()){
        println(response.bodyAsText())
    }

    val userResponse = response.body<UserResponse>()
    return userResponse.user
}

private suspend fun getFirstPage(): List<UserAndId> {
    val response = client.get("https://www.instagram.com/api/v1/tags/web_info") {
        url {
            parameters.append("tag_name", tag)
        }
        header("x-ig-app-id", appId)
        header(
            "cookie",
            "sessionid=$sessionId; ds_user_id=47362721982"
        )
        header("accept", "*/*")
    }
    //println(response.bodyAsText())
    val tagResponse = response.body<TagResponse>()
    maxId = tagResponse.data.recent.nextMaxId
    page = tagResponse.data.recent.nextPage
    println("PAGE: $page")
    println("MAX_ID: $maxId")
    return tagResponse.data.recent.sections.map {
        it.layoutContent.medias.map { mediaXX ->
            UserAndId(mediaXX.media.user.pk, mediaXX.media.user.username)
        }.distinct()
    }.flatten().distinct()

    //return emptyList()
}

suspend fun getOtherPages(): List<UserAndId> {
    //delay(1000)
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
        header("x-ig-app-id", appId)
        header(
            "cookie",
            "sessionid=$sessionId; ds_user_id=$ds_userId; csrftoken=$cfrToken"
        )
        header("x-csrftoken", cfrToken)
    }
    //println(response.bodyAsText())
    if (!response.status.isSuccess()){
        println(response.bodyAsText())
    }
    val body = response.body<Recent>()
    cfrToken = response.setCookie().find { it.name == "csrftoken" }?.value.ifNull { cfrToken }
    maxId = body.nextMaxId
    page = body.nextPage
    println("PAGE: $page")
    println("MAX_ID: $maxId")
    return body.sections.map {
        it.layoutContent.medias.map { mediaXX ->
            UserAndId(mediaXX.media.user.pk, mediaXX.media.user.username)
        }.distinct()
    }.flatten().distinct()
    //return emptyList()
}

fun User.map() =
    IbadanVendors(
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
data class IbadanVendors(
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

