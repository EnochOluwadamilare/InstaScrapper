package com.cherrio

import com.cherrio.instagram.Asoebi
import com.cherrio.plugins.client
import com.cherrio.sheetsdb.client.json
import com.cherrio.sheetsdb.database.*
import com.cherrio.sheetsdb.init.SheetsDb
import com.cherrio.sheetsdb.init.getTable
import com.microsoft.playwright.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject


suspend fun dowork(){
//    val sheetDb = SheetsDb {
//        bearerToken =
//            "ya29.a0AX9GBdUrpuzzvPEtAOeya61DwiCIq-qUuX1L4M7JFGwMlJuUsilwQEasn_HbYXmhC1aE7rqk0otmBYk2b5FlCUiRj-jcnbETtDUaDDESzNZa9-Da0uI-2mXXmhqPC8WxsIAONEo1_pO2bCFrC6EVyMAVMSsg1T3E0QaCgYKAdgSAQASFQHUCsbCGh-6tDY2426U3e_V9rGfCw0169"
//        sheetId = "1YmBiVCmYn2fn15wmmy_Ex6aOGNyC5wv991vTAZkZby8"
//    }
//    val table = sheetDb.getTable<MasterList>()
//    val customers = table.get()
//    val customersTable = sheetDb.getTable<Customers>()
//
//    val unique = customers.distinctBy { it.username }
//    println("Unique: ${unique.size}")
//    unique.forEach {
//        customersTable.create(it.map())
//    }

    coroutineScope {
        val requests = (1..1_000_000).map { async { login(it) } }.awaitAll()
        println(requests)
    }


}


suspend fun login(requestId: Int): String{
    println(requestId)
    val response = client.post("https://api.axocheck.com/api/v1/user/login"){
        setBody(Login("takenya_mccray0pmv@associated.eh", "calv435@"))
        contentType(ContentType.Application.Json)
    }
    return response.bodyAsText()
}

@Serializable
data class Login(
    @SerialName("email")
    val email: String,
    @SerialName("password")
    val password: String
)

fun MasterList.map()= Customers(username, profileUrl, fullName, mediaCount, followers, following, bio, email, countryCode, phone, isBusiness, pageName)

@Serializable
data class MasterList(
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



fun String?.ifNull(block: () -> String): String {
    return this ?: block()
}

fun String.startsWithDigit(): Boolean {
    return get(0).isDigit()
}

inline fun <reified T> getP1(): Set<String> {
    val params = T::class.java.declaredFields.map { it.name }
    //val ann = T::class.java.declaredAnnotations.map { it.annotationClass.simpleName }
    return params.toSet()
}

inline fun <reified T> getP2(): Set<String> {
    val f = T::class.java.getDeclaredConstructor().newInstance()
    return json.encodeToJsonElement(f).jsonObject.keys
}

@Serializable
data class User(
    val id: Int,
    @SerialName("Question")
    val question: String? = null,
    @SerialName("A")
    val optionA: String? = null,
    @SerialName("B")
    val optionB: String? = null,
    @SerialName("C")
    val optionC: String? = null,
    @SerialName("D")
    val optionD: String? = null,
    @SerialName("E")
    val optionE: String? = null,
    @SerialName("Answer")
    val answer: String? = null,
    @SerialName("Comment")
    val comment: String? = null,
)