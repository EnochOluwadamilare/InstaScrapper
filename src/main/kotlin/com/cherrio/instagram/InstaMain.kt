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
import java.nio.file.Paths
import java.util.*
import kotlin.io.path.Path
import kotlin.io.path.readText
import kotlin.random.Random
import kotlin.time.Duration.Companion.minutes

var maxId = ""
var page = 0
val tag = "asoebi"

val sheetDb = SheetsDb {
    bearerToken =
        "ya29.a0AeTM1idDgIb_hy9Fv_tJ16dlS2pUAjCwiugdWPblYOhlAEvm3yNRYvh4wuCpzLx_CaCyaqeqCUnrw_Ec8C2uE2r6VTygWzwB8tYm3c4jBVcyLXKG8PIRvTxFexqHgBXCVu2hHmiZQLv1Ka_BpiznlQxaA5mz-x0nTgaCgYKAeISAQASFQHWtWOmyqT0Rt1DeXUGJzm1EoNiXw0169"
    sheetId = "1YmBiVCmYn2fn15wmmy_Ex6aOGNyC5wv991vTAZkZby8"
}
var table = sheetDb.getTable<Asoebi>()

var credentials = listOf<Credentials>()
var index = 0

val userAndIds = mutableListOf<UserAndId>()
var times = 0
val userAgents = setOf(
    "Mozilla/5.0 (Linux; Android 12; SM-S906N Build/QP1A.190711.020; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/80.0.3987.119 Mobile Safari/537.36",
    "Mozilla/5.0 (Linux; Android 10; SM-G996U Build/QP1A.190711.020; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Mobile Safari/537.36",
    "Mozilla/5.0 (Linux; Android 12; Pixel 6 Build/SD1A.210817.023; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/94.0.4606.71 Mobile Safari/537.36",
    "Mozilla/5.0 (Linux; Android 10; Google Pixel 4 Build/QD1A.190821.014.C2; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/78.0.3904.108 Mobile Safari/537.36",
    "Mozilla/5.0 (Linux; Android 8.0.0; Pixel 2 Build/OPD1.170811.002; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/59.0.3071.125 Mobile Safari/537.36",
    "Mozilla/5.0 (Linux; Android 6.0.1; Nexus 6P Build/MMB29P) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.83 Mobile Safari/537.36",
    "Mozilla/5.0 (Linux; Android 10; HTC Desire 21 pro 5G) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/85.0.4183.127 Mobile Safari/537.36",
    "Mozilla/5.0 (Linux; Android 6.0; HTC One X10 Build/MRA58K; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/61.0.3163.98 Mobile Safari/537.36",
    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36"
)
var userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36"
var cooky: Cookies? = null

suspend fun shuffleUserAgent(){
    userAgent = userAgents.shuffled().first()
    delay(5.minutes)
}

suspend fun restart(){
    while (true) {
        val usersAndId = getOtherPages()
        val users = usersAndId.get()
        users.forEach {
                table.create(it.map())
        }
        println("Done with page $page")
        delay(1.minutes)
    }
}

suspend fun refreshCookie(){
    val railway = System.getenv("RAILWAY")
    cooky = if (railway != null){
        val loginResponse = client.get("http://instascrapper-env.eba-yjypcynj.us-east-1.elasticbeanstalk.com/login").bodyAsText()
        println("Login Response")
        println(loginResponse)
        loginResponse.toCookies()
    }else{
        login().toCookies()
    }
}

suspend fun List<UserAndId>.get() = coroutineScope {
    val users = map { async { getUserDetails(it.id) } }.awaitAll()
    return@coroutineScope users
}

suspend fun getUserDetails(userId: String):User {
    delay(1000)
    val cookie = cooky!!.cookies.joinToString("; ") { "${it.name}=${it.value}" }

    val response = client.get("https://nt5j3qu02h.execute-api.us-east-1.amazonaws.com/scrapper/user-details/$userId") {
        header("x-ig-app-id", "936619743392459")
        header(
            "cookie", cookie
        )
        header("x-csrftoken", cooky!!.cookies.find { it.name == "csrftoken" }!!.value)
//        header("x-requested-with","XMLHttpRequest")
//        header("sec-ch-ua-platform", "macOS")
//        header("sec-ch-ua", """Not?A_Brand";v="8", "Chromium";v="108", "Brave";v="108""")
//        userAgent(userAgent)
    }
    return if (!response.status.isSuccess()){
        val error = response.bodyAsText()
        println(error)
        refreshCookie()
        getUserDetails(userId)
    }else {
        try {
            val userResponse = response.body<UserResponse>()
            userResponse.user
        }catch (e: Exception){
            println(response.bodyAsText())
            getUserDetails(userId)
        }

    }
}

fun String.toCookies() = json.decodeFromString<Cookies>(this)


suspend fun runEveryRandomSeconds(block: suspend () -> User): User {
    val random = Random
    val delay = random.nextLong(3,16)
    delay(delay * 1000L)
    return block()
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
        println(error)
        refreshCookie()
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

fun incrementIndex(){
    if (index == credentials.size - 1) {
        index = 0
    } else {
        index++
    }
}
fun List<Credentials>.dropAt(pos: Int){
    println("Dropping ${get(index).name}'s account")
    credentials = filterIndexed { index, _ -> pos != index }
}

fun User.map() =
    Asoebi(
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
    val appId: String,
    val name: String,
    val deviceId: String
)

@Serializable
data class Asoebi(
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
