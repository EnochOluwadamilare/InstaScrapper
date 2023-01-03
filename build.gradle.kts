val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project

plugins {
    application
    kotlin("jvm") version "1.7.20"
    id("io.ktor.plugin") version "2.1.2"
    kotlin("plugin.serialization") version "1.7.20"
    id("com.google.cloud.tools.jib") version "3.2.1"
}

group = "com.cherrio"
version = "0.0.1"
application {
    mainClass.set("com.cherrio.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots")
    }
    mavenCentral()

}

jib {
    from{
        image = "mcr.microsoft.com/playwright/java:v1.27.0-focal"
    }
    to {
        auth {
            //aws ecr-public get-login-password --region us-east-1 | docker login --username AWS --password-stdin public.ecr.aws/a9a1h7h4
            //val registryUserName = project.findProperty("registryUserName") as String? ?: ""
            //val registryPassword = project.findProperty("registryPassword") as String? ?: ""
            username = "cherrio20"
            password = "Development@2020_"
        }
    }
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-netty-jvm:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktor_version")

    implementation("ch.qos.logback:logback-classic:$logback_version")

    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-client-java:$ktor_version")
    implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
    implementation("io.ktor:ktor-server-call-logging-jvm:2.1.2")
    implementation("io.github.cherrio-llc:sheets-db:0.0.1-SNAPSHOT")
    implementation(libs.jsoup)

    implementation("com.microsoft.playwright:playwright:1.27.0")

    testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}