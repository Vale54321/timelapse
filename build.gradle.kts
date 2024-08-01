val kotlin_version: String by project
val logback_version: String by project

plugins {
    kotlin("jvm") version "2.0.0"
    id("io.ktor.plugin") version "2.3.11"
}

group = "de.heiserer"
version = "0.0.1"

application {
    mainClass.set("de.heiserer.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    testImplementation("io.ktor:ktor-server-tests-jvm")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    implementation("io.ktor:ktor-server-websockets")

    implementation("io.ktor:ktor-server-thymeleaf")
    implementation("org.thymeleaf:thymeleaf:3.1.2.RELEASE")

    implementation ("com.jcraft:jsch:0.1.55")
    implementation("org.slf4j:slf4j-api:1.7.32")

    implementation("de.heiserer:sftp:1.0-SNAPSHOT")
}
