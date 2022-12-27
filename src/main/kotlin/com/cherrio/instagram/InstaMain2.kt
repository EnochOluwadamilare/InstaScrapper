package com.cherrio.instagram

import com.cherrio.instagram.models.UserResponse
import com.cherrio.sheetsdb.client.json
import com.cherrio.sheetsdb.database.create
import com.microsoft.playwright.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import java.nio.file.Paths


fun login() {
    Playwright.create().use { playwright ->
        val browser: Browser = playwright.chromium().launch(BrowserType.LaunchOptions().setHeadless(true))
        val context = browser.newContext()
        val page: Page = context.newPage()
        page.navigate("https://www.instagram.com/")
        println(page.title())

        println("Logging in")
        page.locator("[name='username']").fill("jazzedayo@gmail.com")
        page.locator("[name='password']").fill("Ayodele4_")
        page.locator("[type='submit']").click()

        page.getByText("Save information").first().click()

        page.onPopup {
            it.getByText("Not Now").first().click()
        }

        page.getByText("Search").first().click()
        page.locator("[placeholder='Search']").fill("#lagosvendors")

        context.storageState(BrowserContext.StorageStateOptions().setPath(Paths.get("state.json")))
    }
}

suspend fun begin(tag: String) {
    Playwright.create().use { playwright ->
        val browser: Browser = playwright.chromium().launch(BrowserType.LaunchOptions().setHeadless(true))
        val context = browser.newContext(Browser.NewContextOptions().setStorageStatePath(Paths.get("state.json")))
        val page: Page = context.newPage()
        page.onResponse {
            if (it.url().contains("api/v1/users")){
                val user = json.decodeFromString<UserResponse>(it.text()).user
                runBlocking {
                    table.create(user.map())
                }
            }
        }
        println("Before navigate")
        page.navigate("https://www.instagram.com/explore/tags/$tag/")

        //First post
        page.locator("[class='_aagu']").first().click()

        nextUser(isFirst = true, page = page)



//        println(page.title())
//         while (!page.locator("span",Page.LocatorOptions().setHasText("End")).isVisible){
//             page.mouse().wheel(0.0, 100.0)
//         }

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
    page.locator("[class='xt0psk2']").first().hover()

    page.waitForResponse(
        { response: Response ->
            response.url().contains("api/v1/users") && response.status() == 200
        }
    ) {}

    delay(3000)
    nextUser(page = page)

}