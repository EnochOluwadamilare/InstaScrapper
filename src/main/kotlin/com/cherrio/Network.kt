package com.cherrio

import com.cherrio.sheetsdb.client.json
import com.cherrio.sheetsdb.database.*
import com.microsoft.playwright.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject

interface MyInterface{

    companion object: MyInterface
    fun then(myInterface: MyInterface): MyInterface{
        println(this === myInterface)
        list.add(myInterface)
        return myInterface
    }
}

val MyInterface.list by lazy { mutableListOf<MyInterface>() }

class Pad(string: String): MyInterface{

}
class Bag(string: String): MyInterface{

}
fun MyInterface.padd(string: String): MyInterface{
    return then(Pad(string))
}
fun MyInterface.bag(string: String): MyInterface{
    return then(Bag(string))
}

fun button(myInterface: MyInterface = MyInterface){
    println(myInterface.list.size)
}
//fun main(){
//   button(MyInterface.padd("15.dp").bag("Joseph"))
//    button(MyInterface.padd("80").bag("Keys"))
//    println(MyInterface.list)
//}

@Serializable
data class Login(
    @SerialName("email")
    val email: String,
    @SerialName("password")
    val password: String
)


fun String?.ifNull(block: () -> String): String {
    return if (isNullOrEmpty()) block() else this
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