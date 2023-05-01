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
import java.net.InetAddress
import java.util.*

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
//    val localhost: InetAddress = InetAddress.getLocalHost()
//    println("Ip=${localhost.hostAddress}")
//    getAllIp()
//}

class MutableState<T>(initialValue: T) {
    private var value: T = initialValue
    private val listeners = mutableListOf<(T) -> Unit>()

    fun get(): T = value

    fun set(newValue: T) {
        if (newValue != value) {
            value = newValue
            listeners.forEach { it(newValue) }
        }
    }

    fun observe(observer: (T) -> Unit) {
        listeners.add(observer)
        observer(value)
    }
}

interface UIComponent {
    fun render()
}

class Composable(val invoke: () -> Unit)

class Label(private val text: String, private val onTextChanged: (Label) -> Unit) : UIComponent {
    override fun render() {
        println("Label: $text")
    }

    init {
        onTextChanged(this)
    }
}

class Renderer(private val root: UIComponent) : UIComponent {
    override fun render() {
        root.render()
    }
}

class Row : UIComponent {
    private val children = mutableListOf<UIComponent>()

    operator fun UIComponent.unaryPlus() {
        children.add(this)
    }

    override fun render() {
        children.forEach {
            it.render()
        }
    }
}

fun row(init: Row.() -> Unit): Row {
    val row = Row()
    row.init()
    return row
}

fun main() {
    val labelState = MutableState("Hello")

    val ui = row {
        + Label(labelState.get()) {
            it.render()
        }
    }

//    val renderer = Renderer(ui)
//
//    renderer.render()

    labelState.set("Goodbye")
    ui
}






fun getAllIp(): List<String> {
    val result: MutableList<String> = ArrayList()
    val localhost = InetAddress.getLocalHost()
    // this code assumes IPv4 is used
    // this code assumes IPv4 is used
    val ip = localhost.address

    for (i in 1..254) {
        ip[3] = i.toByte()
        val address = InetAddress.getByAddress(ip)
        if (address.isReachable(1000)) {
            println("$address machine is turned on and can be pinged")
            println("Name: ${address.hostName}")
        }
    }
    return result
}

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