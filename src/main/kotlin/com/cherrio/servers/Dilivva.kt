package com.cherrio.servers

import com.cherrio.instagram.sheetDb
import com.cherrio.plugins.client
import com.cherrio.servers.dilivva.DilivvaUser
import com.cherrio.servers.dilivva.Sender
import com.cherrio.servers.dilivva.User
import com.cherrio.servers.dilivva.toSender
import com.cherrio.sheetsdb.database.create
import com.cherrio.sheetsdb.init.SheetsDb
import com.cherrio.sheetsdb.init.getTable
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
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
//        val time = it.toInstant()
//        val isGreater = time > today
//        println("Is today: $isGreater ")
//    }
//
//}

private val inputFormat = DateTimeFormatter.ofPattern("E, MMM d, yyyy h:mm a", Locale.US)
private var beginDate = "Thu, Apr 27, 2023 6:33 PM"
private var today = beginDate.toInstant() //now.toInstant(TimeZone.currentSystemDefault())
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
    println("Before: $beginDate")
    if (response.status.isSuccess()) {
        val dilivvaUser = response.body<DilivvaUser>()
        val newUsers = dilivvaUser.data.users.filter(::filter).map(::toSender)
        println("New users are: ${newUsers.size}")
        if (newUsers.isNotEmpty()) {
            newUsers.forEach {
                table.create(it)
            }
            beginDate = newUsers.first().createdAt
            today = beginDate.toInstant()
            println("New begin date: $beginDate")
        }
    }else{
        println(response.bodyAsText())
    }
}

fun filter(user: User): Boolean {
    val userRegistered = user.createdAt.toInstant()
    return userRegistered > today
}
fun String.toInstant(): Instant{
    val localDateTime = LocalDateTime.parse(this, inputFormat)
    return localDateTime.toKotlinLocalDateTime().toInstant(TimeZone.UTC)
}