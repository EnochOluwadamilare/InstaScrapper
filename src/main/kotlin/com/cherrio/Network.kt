package com.cherrio

import com.cherrio.sheetsdb.client.json
import com.cherrio.sheetsdb.database.*
import com.microsoft.playwright.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject


//fun main()= runBlocking{
//    val sheetDb = SheetsDb {
//        bearerToken =
//            "ya29.a0AX9GBdVxEpavvtoVrgDlBe9Amn_GmS-9MCYGkg6nHow60eYrM3-9tNWcXHg-0glkW_2ad6Y0nqoyb_ucEmecMnA5McSISjsMnzfpxpAWRZ3PEH5iGbNXbfWloFrMJnu0t7hPEC4RRy41jtz2NGg5QQkqP5hX0eCSHQaCgYKAc8SAQASFQHUCsbCxdg_VixTvtXyVMAqP-QGSA0169"
//        sheetId = "1YmBiVCmYn2fn15wmmy_Ex6aOGNyC5wv991vTAZkZby8"
//    }
//    val table = sheetDb.getTable<IbadanVendors>()
//    val customers = table.get()
//    println(customers.distinctBy { it.username }.filter { it.mediaCount >= 50 && it.followers >= 500 }.map { it.email }.size)
//}




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