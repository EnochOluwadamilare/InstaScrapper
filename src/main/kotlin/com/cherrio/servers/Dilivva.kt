package com.cherrio.servers

import com.cherrio.instagram.sheetDb
import com.cherrio.plugins.client
import com.cherrio.servers.dilivva.*
import com.cherrio.sheetsdb.database.create
import com.cherrio.sheetsdb.init.SheetsDb
import com.cherrio.sheetsdb.init.getTable
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


//fun main() = runBlocking {
//    listOf(
//        "Tue, Apr 4, 2023 7:36 PM",
//        "Wed, Apr 19, 2023 3:15 PM",
//        "Wed, Apr 26, 2023 9:57 AM",
//        "Mon, May 1, 2023 8:00 AM",
//        "Mon, May 1, 2023 8:01 AM"
//    ).forEach {
//        //val time = it.toInstant()
//        //val isGreater = time > today
//
//    }
//
//    val to = "5Fri, May 5, 2023 1:20 PM;"
//
//    println("Is today: ${to.clean()} ")
//
//}

private val inputFormat = DateTimeFormatter.ofPattern("E, MMM d, yyyy h:mm a", Locale.US)
private var beginDate = "Tue, May 23, 2023 6:45 AM"
private var today = beginDate.clean().toInstant() //now.toInstant(TimeZone.currentSystemDefault())
private const val googleSheetId = "1tHpaIP3ZZhZiuFhyF-GkxESQbsXexGQpkzXL66h_zfE"
val dilivvaSheetsDb = SheetsDb {
    sheetId = googleSheetId
}
val bearerToken: String = System.getenv("TOKEN")
suspend fun checkNewUsers(){
    val table = dilivvaSheetsDb.getTable<Sender>()
    val response = client.get("https://api.dilivva.com/api/v1/admin/users?perPage=50&page=1&sort=desc"){
        bearerAuth(bearerToken)
    }
    if (response.status.isSuccess()) {
        val dilivvaUser = response.body<DilivvaUser>()
        val newUsers = dilivvaUser.data.users.filter(::filter)
        println("New users are: ${newUsers.size}")
        if (newUsers.isEmpty()) return
        val senders = newUsers.map { mapToSender(it) }
        senders.forEach {
            table.create(it)
        }
        beginDate = senders.first().createdAt
        today = beginDate.toInstant()
        println("New begin date: $beginDate")
    }else{
        println(response.bodyAsText())
    }
}

suspend fun mapToSender(user: User): Sender{
    val businessName = coroutineScope { async { getUserProfile(user.uuid) } }.await()
    val sender = toSender(user)
    return sender.addBusinessName(businessName)
}
private suspend fun getUserProfile(userId: String): String?{
    val response = client.get("https://api.dilivva.com/api/v1/admin/users/$userId"){
        bearerAuth(bearerToken)
    }
    if (!response.status.isSuccess()) return null
    val user = response.body<DilivvaApiResponse>()
    if (user.data.business == null) return null
    return user.data.business.businessName
}

fun filter(user: User): Boolean {
    val userRegistered = user.createdAt.toInstant()
    return userRegistered > today
}
fun String.toInstant(): Instant{
    val localDateTime = LocalDateTime.parse(this, inputFormat)
    return localDateTime.toKotlinLocalDateTime().toInstant(TimeZone.UTC)
}
fun String.clean(): String{
    var cleanString = this
    if (!cleanString.first().isLetter()) {
        cleanString = cleanString.substring(1)
    }
    if (!cleanString.last().isLetter()) {
        cleanString = cleanString.substring(0, length - 2)
    }
    return cleanString
}
