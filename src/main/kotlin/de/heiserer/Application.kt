package de.heiserer

import de.heiserer.plugins.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.websocket.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import io.ktor.server.thymeleaf.*
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver


fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    val rtspUrl = "rtsps://10.0.0.1:7441/mstHSxlikG8CrMfh?enableSrtp"
    val outputFolder = "images"

    install(Thymeleaf) {
        setTemplateResolver(ClassLoaderTemplateResolver().apply {
            prefix = "templates/"
            suffix = ".html"
            characterEncoding = "utf-8"
        })
    }

    install(WebSockets) {
    }

    launch {
        while (true) {
            ImageToolKit.captureImage(rtspUrl, outputFolder)
            delay(5000)
        }
    }


    configureRouting() // Existing routing configuration
}


