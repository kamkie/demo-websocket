package net.devopssolutions.demo.ws.server.component

import net.devopssolutions.demo.ws.server.util.LoggingThreadPoolExecutor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import javax.websocket.Session

@Component
class WsBroadcaster {
    private val sendersExecutor = LoggingThreadPoolExecutor(10, 20, 2, TimeUnit.MINUTES, ArrayBlockingQueue<Runnable>(10000))

    @Autowired
    private lateinit var wsServer: WsServer

    fun broadcastAsync(action: Consumer<Session>) {
        wsServer.allSessions.forEach { session ->
            sendersExecutor.execute {
                try {
                    action.accept(session)
                } catch (throwable: Throwable) {
                    log.warn("exception broadcasting message to session: " + session, throwable)
                }
            }
        }
    }

    fun sendToId(sessionId: String, action: Consumer<Session>) {
        val session = wsServer.getSession(sessionId)
        if (session == null) {
            log.warn("session with id {} not founded", sessionId)
            return
        }
        try {
            action.accept(session)
        } catch (throwable: Throwable) {
            log.warn("exception sending message to session: " + session, throwable)
        }

    }

    fun sendToIdAsync(sessionId: String, action: Consumer<Session>) {
        sendersExecutor.execute { sendToId(sessionId, action) }
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(WsBroadcaster::class.java)
    }
}
