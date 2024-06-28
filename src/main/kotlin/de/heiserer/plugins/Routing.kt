package de.heiserer.plugins

import de.heiserer.ImageToolKit
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File

fun Application.configureRouting() {
    routing {
        get("/") {
            val videoFolder = File("output")
            val fileFilter = { it: File -> it.name.endsWith(".mp4") }
            val videoFiles = videoFolder.listFiles(fileFilter)
            val downloadLinks = videoFiles?.map { "/download/${it.name}" }
            println(downloadLinks)

            val html = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <title>Download MP4 Files</title>
                </head>
                <body>
                    <h1>Downloadable MP4 Files</h1>
                    <ul>
                    """ +
                    (downloadLinks?.joinToString("") { "<li><a href=\"$it\">$it</a></li>" } ?: "") +
                    """
                    </ul>
                </body>
                </html>
            """
            call.respondText(html, contentType = ContentType.Text.Html)
            //ImageToolKit.createVideoFromImages("images/28_06_2024", "28_06_2024.mp4")
        }

        // Add a route for downloading individual files (same as before)
        get("/download/{fileName}") {
            val fileName = call.parameters["fileName"] ?: return@get call.respond(HttpStatusCode.NotFound)
            val file = File("output", fileName) // Replace with actual path if needed
            if (file.exists()) {
                call.respondFile(file)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}


