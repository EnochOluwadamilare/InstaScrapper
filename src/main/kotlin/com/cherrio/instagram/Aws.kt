package com.cherrio.instagram

import com.cherrio.instagram.models.UserResponse
import com.cherrio.plugins.client
import com.cherrio.sheetsdb.client.json
import com.cherrio.sheetsdb.database.create
import com.microsoft.playwright.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import java.nio.file.Paths
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.math.log
import kotlin.random.Random

var index = 2

val creds = listOf(
    Triple("jazzedayo@gmail.com","Ayodele4_","47362721982"),
    Triple("cherrio.llc@gmail.com","Ayodele4_","56978350990"),
    Triple("elizabethy.keen@gmail.com","Keen12_","57221602232"),
    Triple("donaldy.ressler@gmail.com","Donald12_","57201108496"),
    Triple("ray.redd.reddington@gmail.com","Raymond12_","57232760704"),
    Triple("oaks224@gmail.com","Ayodele4_","56822524662")
)

var userAgents =  Paths.get("user-agents.txt").readText().split("\n")
//var userAgents =  listOf<String>()
//fun main(){
//    login()
//}

fun login(userId: String = ""): String {
    var html = ""
    var success = false
    val credentials = if (userId.isNotEmpty()){
        sendNotification(creds.find { it.third == userId }!!.first)
        creds.filter { it.third != userId }
    }else creds

    val (email, password) = credentials[index]
    println("Using: $email")
    try {
        Playwright.create().use { playwright ->
            val browser: Browser = playwright.chromium().launch(BrowserType.LaunchOptions().setHeadless(false))
            val context = browser.newContext()
            val page: Page = context.newPage()

            page.onResponse {
                if (!it.ok()){
                    sendNotification("Error logging in for $email. With error: ${it.statusText()} code: ${it.status()}, url: ${it.url()}")
                }
            }

            page.route("**/*"){
                val headers= it.request().headers().toMutableMap()
                headers["user-agent"] = userAgents[randomIndex()]
                it.resume(Route.ResumeOptions().setHeaders(headers))
            }

            page.navigate("https://www.instagram.com/")
            println(page.title())

            println("Logging in")
            html = page.content()
            page.locator("[name='username']").fill(email)
            page.locator("[name='password']").fill(password)
            page.locator("[type='submit']").click()

//        page.getByText("Save information").first().click()
//
//        page.onPopup {
//            it.getByText("Not Now").first().click()
//        }

//            page.getByText("Search").first().click()
//            page.locator("[placeholder='Search']").fill("#lagosvendors")

            page.waitForResponse(
                { response: Response ->
                    response.url().contains("api/v1/feed/timeline") && response.status() == 200
                }
            ) {}

            context.storageState(BrowserContext.StorageStateOptions().setPath(Paths.get("state.json")))

            println("Done")
            success = true
        }
    }catch (e: TimeoutError){
        incrementIndex(credentials.size)
        println(e.localizedMessage)
        Paths.get("index.html").writeText(html)
    }
    incrementIndex(credentials.size)
    return if (success) Paths.get("state.json").readText() else{
        sendNotification("Not logging in again, $email")
        ""
    }
}

fun incrementIndex(size: Int){
    if (index == size - 1) {
        index = 0
    } else {
        index++
    }
}

fun sendNotification(email: String) = runBlocking{
    val url = "https://hooks.slack.com/services/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"
    val requestBody = """
        Lead-NOTIFICATION
        $email
    """
    val request = client.post(url){
        contentType(ContentType.Application.Json)
        setBody(SlackRequest(requestBody))
    }
    println("Notification: $requestBody with status: ${request.status == HttpStatusCode.OK}")
}

suspend fun begin(tag: String) {
    Playwright.create().use { playwright ->
        val browser: Browser = playwright.chromium().launch(BrowserType.LaunchOptions().setHeadless(false))
        val context = browser.newContext(Browser.NewContextOptions().setStorageStatePath(Paths.get("state.json")))
        val page: Page = context.newPage()
        page.onResponse {
            if (it.url().contains("api/v1/users")){
                val user = json.decodeFromString<UserResponse>(it.text()).user
                runBlocking {
                    table.create(user.map(), tag)
                }
            }
        }
        println("Before navigate")
        page.navigate("https://www.instagram.com/explore/tags/${tag.lowercase()}/")

        //First post
        val pas = page.locator("[class='_aagu']").first().click()

        nextUser(isFirst = true, page = page)

    }
}

var nextDone: Boolean = false
suspend fun nextUser(isFirst: Boolean = false, page: Page){
    if (!isFirst) {
        println("Next")
        //Next post (Button)
        if (nextDone){
            //Click next button
            page.locator("[class='_abl-']").nth(1).click()
        }else {
            //Click next button with no back button
            page.locator("[class='_abl-']").first().click()
        }
        nextDone = true
    }
    //Get user details
    page.locator("[class='x78zum5']").first().hover()

    page.waitForResponse(
        { response: Response ->
            response.url().contains("api/v1/users") && response.status() == 200
        }
    ) {}

    delay(1000)
    nextUser(page = page)

}

fun randomIndex(): Int {
    val random = Random
    return random.nextInt(0, 1000)
}

@Serializable
private data class SlackRequest(
    val text: String
)