package de.heiserer

import io.ktor.websocket.*
import kotlinx.coroutines.channels.Channel
import java.util.concurrent.ConcurrentHashMap

object VideoSessionManager {
    private val sessions = ConcurrentHashMap<String, VideoSession>()

    fun startSession(sessionId: String): VideoSession {
        val session = VideoSession(sessionId)
        sessions[sessionId] = session
        return session
    }

    fun getSession(sessionId: String): VideoSession? {
        return sessions[sessionId]
    }

    fun endSession(sessionId: String) {
        sessions.remove(sessionId)
    }
}

class VideoSession(val sessionId: String) {
    val progressChannel = Channel<String>()

    fun removeWebSocketSession(socket: WebSocketSession) {
        // Clean up resources when WebSocket disconnects
        progressChannel.close()
    }
}