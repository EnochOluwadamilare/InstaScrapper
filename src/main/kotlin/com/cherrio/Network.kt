package com.cherrio

import com.cherrio.plugins.client
import com.cherrio.sheetsdb.client.json
import com.cherrio.sheetsdb.database.*
import com.cherrio.sheetsdb.init.SheetsDb
import com.cherrio.sheetsdb.init.getTable
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import org.jsoup.Jsoup
import kotlin.random.Random
import kotlin.time.ExperimentalTime


@OptIn(ExperimentalTime::class)
//fun main() {
////    runBlocking {
////        val sheetDb = SheetsDb {
////            bearerToken = "ya29.a0AeTM1iftmkE4GRJQQwurwZ_TB_VPazrcrPspFj77TEWetI4daen3eYYRNJ4_w6WfTUrMIc9g_Qtt08BYvahukkWp_tBmoWzMdSoNSjBMim9LjxgliPceinszWinBhjaOTO8e9PGc-D8A3EQw8gd8FWZEeS6kvIgaCgYKAUgSARESFQHWtWOmdl2dFss_4CA2b19AOK5nrA0166"
////            sheetId = "1px2g6GUDISA3NzadFWMexJFco0CKaP1bKI-4mBqG7uM"
////        }
////        val table = sheetDb.getTable<Oyo>()
////
////        repeat(15){
////            val page = it.inc()
////            val companies = getCompanyByLocation("ibadan",page)
////                .map {
////                    async { getCompanyDetails(it, "Ibadan") }
////                }.awaitAll()
////            companies.forEach { company ->
////                table.create(company)
////            }
////            println("Done for page $page")
////        }
////
////        println("Done")
////
////
////    }
//
//}

//https://www.vpngate.net/api/iphone/




suspend fun getCompanyDetails(list: Pair<String, String>, area: String): Oyo {
    val response = client.get("https://www.businesslist.com.ng${list.second}").bodyAsText()
    val parsed = Jsoup.parse(response).select(".info")
    val companyManager = parsed.find { it.text().contains("Company manager") }?.text()?.replace("Company manager", "")
    val sector = parsed.select(".product .product_name").map { prod -> prod.text() }.ifEmpty { listOf("None") }
    var address = ""
    var phone1 = ""
    var phone2 = ""
    var website = ""
    parsed.select(".text").forEach {
        val text = it.text()
        if (!text.startsWith("Send")) {
            when {
                address.isEmpty() -> address = text.replace("View Map", "")
                (text.startsWith("+") || text.startsWithDigit()) && phone1.isEmpty() -> phone1 = text
                (text.startsWith("+") || text.startsWithDigit()) && phone1 != text -> phone2 = text
                text.startsWith("http") && website.isEmpty() -> website = text
            }
        } else return@forEach
    }
    val phones = listOf(phone1, phone2)
    println("Gotten for: ${list.first} Contact: $companyManager, Address: $address, Phones: $phones")
    return Oyo(
        name = list.first,
        address = address,
        phones = phones,
        sectors = sector,
        website = website.ifEmpty { "None" },
        contactName = companyManager.ifNull { "None" },
        area = area
    )
}

suspend fun getCompanyByLocation(location: String, page: Int = 1): List<Pair<String, String>> {
    val response = client.get("https://www.businesslist.com.ng/location/$location/$page").bodyAsText()
    val parsed = Jsoup.parse(response).select(".company")
    return parsed.map {
        val link = it.select("a").attr("href")
        val name = it.select("a").attr("title")
        Pair(name, link)
    }.filter { it.first.isNotEmpty() }
}


@Serializable
data class Oyo(
    val name: String,
    val address: String = "",
    val phones: List<String> = emptyList(),
    val sectors: List<String> = emptyList(),
    val website: String = "",
    val contactName: String = "",
    val area: String
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