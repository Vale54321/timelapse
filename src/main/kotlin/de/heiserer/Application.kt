package de.heiserer

import de.heiserer.fileManager.SftpFileManager
import de.heiserer.plugins.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.websocket.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import io.ktor.server.thymeleaf.*
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    val rtspUrl = "rtsps://10.0.0.1:7441/mstHSxlikG8CrMfh?enableSrtp"
    val outputFolder = createTempDirectory().path

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
        val fileManager = SftpFileManager(
        sshHost = "nas.heiserer.lan",
        sshPort =305,
        username = "timelapse",
        password = "E!46UZ!Ho7ac3gz!mvYv.wyz",
        baseDirectory = "output"
    )

        while(true) {
            launch {
                val image = ImageToolKit.captureImage(rtspUrl, outputFolder)

                val dateFormatFolderName = SimpleDateFormat("dd_MM_yyyy")
                val timestampFolder = "images"+ File.separator + dateFormatFolderName.format(Date())

                fileManager.saveFile(image, timestampFolder)
                image.delete()
            }
            delay(5000)
        }
    }

    configureRouting() // Existing routing configuration
}


