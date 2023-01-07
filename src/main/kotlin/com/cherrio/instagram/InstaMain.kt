package com.cherrio.instagram

import com.cherrio.ifNull
import com.cherrio.instagram.models.*
import com.cherrio.plugins.client
import com.cherrio.sheetsdb.client.json
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
import kotlinx.serialization.decodeFromString
import java.util.*
import kotlin.random.Random
import kotlin.time.Duration.Companion.minutes

var maxId = ""
var page = 0
var tag = "asoebi"

val sheetDb = SheetsDb {
    sheetId = "1YmBiVCmYn2fn15wmmy_Ex6aOGNyC5wv991vTAZkZby8"
}
val table = sheetDb.getTable<Customers>()
var cooky: Cookies? = null

suspend fun restart(tag: String?, _maxId: String, _page: Int){
    page = _page
    maxId = _maxId
    while (true) {
        val usersAndId = getOtherPages()
        val users = usersAndId.get()
        users.forEach {
            table.create(it.map(), tag)
        }
        println("Done with page $page")
        delay(5.minutes)
    }
}

suspend fun refreshCookie(userId: String = "", state: Boolean = false): Cookies?{
    println("Refreshing...")
    val railway = System.getenv("RAILWAY")
    return if (railway != null){
        val loginResponse = client.get("http://instascrapper-env.eba-yjypcynj.us-east-1.elasticbeanstalk.com/login"){
            url{
                if (userId.isNotEmpty()) {
                    parameters.append("state", state.toString())
                }
                if (userId.isNotEmpty()) {
                url {
                    parameters.append("user_id", userId)
                }
            }
            }
        }
        if (loginResponse.status.isSuccess()){
            println(loginResponse)
            loginResponse.bodyAsText().toCookies()
        }else{
            delay(5.minutes)
            refreshCookie()
        }

    }else{
        login().toCookies()
    }
}

suspend fun List<UserAndId>.get() = coroutineScope {
    return@coroutineScope map { async { getUserDetails(it.id) } }.awaitAll()
}

suspend fun getUserDetails(userId: String):User {
    val cookie = cooky!!.cookies.joinToString("; ") { "${it.name}=${it.value}" }

    //https://nt5j3qu02h.execute-api.us-east-1.amazonaws.com/scrapper/user-details/$userId
    //https://i.instagram.com/api/v1/users/$userId/info/
    val response = client.get("https://nt5j3qu02h.execute-api.us-east-1.amazonaws.com/scrapper/user-details/$userId") {
        header("x-ig-app-id", "936619743392459")
        header(
            "cookie", cookie
        )
        header("x-csrftoken", cooky!!.cookies.find { it.name == "csrftoken" }!!.value)
    }
    return if (!response.status.isSuccess()){
        val error = response.bodyAsText()
        checkPointOrRefresh(error)
        getUserDetails(userId)
    }else {
        try {
            val userResponse = response.body<UserResponse>()
            userResponse.user
        }catch (e: Exception){
            checkPointOrRefresh(response.bodyAsText())
            getUserDetails(userId)
        }

    }
}

fun String.toCookies() = json.decodeFromString<Cookies>(this)


fun runEveryRandomSeconds(): Long {
    val random = Random
    val delay = random.nextLong(3,16)
    return delay * 1000L
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
    val cookie = cooky!!.cookies.joinToString("; ") { "${it.name}=${it.value}" }
    println(cookie)
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
        header("x-ig-app-id", "936619743392459")
        header(
            "cookie", cookie
        )
        header("x-csrftoken", cooky!!.cookies.find { it.name == "csrftoken" }!!.value)

    }
    return if (!response.status.isSuccess()){
        val error = response.bodyAsText()
        checkPointOrRefresh(error)
        getOtherPages()
    }else {
        val body = response.body<Recent>()
        maxId = body.nextMaxId
        page = body.nextPage
        println("PAGE: $page")
        println("MAX_ID: $maxId")
        body.sections.map {
            it.layoutContent.medias.map { mediaXX ->
                UserAndId(mediaXX.media.user.pk, mediaXX.media.user.username)
            }.distinct()
        }.flatten().distinct()
    }
}

suspend fun checkPointOrRefresh(error: String){
    println("checkPointOrRefresh: $error")
    when{
        error.contains("checkpoint_required") ->{
            cooky = refreshCookie(userId = cooky!!.cookies.find { it.name == "ds_user_id"}!!.value)
        }
        error.contains("spam") -> {
            delay(20.minutes)
        }
        error.contains("require_login") ->{
            cooky = refreshCookie()
        }
        else -> {
            cooky = refreshCookie()
        }
    }
}


fun User.map() =
    Customers(
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
        url = externalUrl.ifNull { "None" },
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
    val appId: String,
    val name: String,
    val deviceId: String
)

@Serializable
data class Customers(
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
    val bio: String = "None",
    @SerialName("email")
    val email: String,
    @SerialName("country_code")
    val countryCode: String = "None",
    @SerialName("phone")
    val phone: String = "None",
    @SerialName("is_business")
    val isBusiness: String,
    @SerialName("page_name")
    val pageName: String,
    @SerialName("whatsapp_number")
    val whatsappNumber: String = "None",
    @SerialName("url")
    val url: String = "None",
    @SerialName("address")
    val address: String = "None",
    @SerialName("city")
    val city: String? = "None",
)

@Serializable
data class Cookies(
    @SerialName("cookies")
    val cookies: List<Cooky>,
    @SerialName("origins")
    val origins: List<Origin>
)

@Serializable
data class Cooky(
    @SerialName("name")
    val name: String,
    @SerialName("value")
    val value: String,
    @SerialName("domain")
    val domain: String,
    @SerialName("path")
    val path: String,
    @SerialName("expires")
    val expires: Double,
    @SerialName("httpOnly")
    val httpOnly: Boolean,
    @SerialName("secure")
    val secure: Boolean,
    @SerialName("sameSite")
    val sameSite: String
)

@Serializable
data class Origin(
    @SerialName("origin")
    val origin: String,
    @SerialName("localStorage")
    val localStorage: List<LocalStorage>
)

@Serializable
data class LocalStorage(
    @SerialName("name")
    val name: String,
    @SerialName("value")
    val value: String
)
