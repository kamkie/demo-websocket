package net.devopssolutions.demo.ws.server.component

import net.devopssolutions.demo.ws.server.util.LoggingThreadPoolExecutor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.nio.ByteBuffer
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct
import javax.websocket.RemoteEndpoint

@Component
class WsBroadcaster {
    private val log = org.slf4j.LoggerFactory.getLogger(WsBroadcaster::class.java)

    private val sendersExecutor = LoggingThreadPoolExecutor(10, 20, 2, TimeUnit.MINUTES, ArrayBlockingQueue<Runnable>(10000))
    private val messagesQueue = ArrayBlockingQueue<ByteBuffer>(Int.MAX_VALUE / 4);

    @Autowired
    private lateinit var wsServer: WsServer

    @PostConstruct
    fun init() {
        sendersExecutor.execute {
            var remote: RemoteEndpoint.Basic? = null
            while (true) {
                try {
                    if (remote != null) {
                        remote.sendBinary(messagesQueue.take())
                    } else {
                        val sessions = wsServer.getAllSessions().toList()
                        if (sessions.size > 0) {
                            remote = sessions[0].basicRemote
                        }
                    }
                } catch (e: Exception) {
                    remote = null
                    log.warn("exception broadcasting message", e)
                }
            }
        }
    }

    fun broadcastAsync(payload: ByteBuffer) {
        wsServer.getAllSessions().forEach { session ->
            sendersExecutor.execute {
                try {
                    session.basicRemote.sendBinary(payload)
                } catch (throwable: Throwable) {
                    log.warn("exception broadcasting message to session: " + session, throwable)
                }
            }
        }
    }

    fun broadcastQueue(payload: ByteBuffer) {
        messagesQueue.put(payload)
    }

    fun broadcast(payload: ByteBuffer) {
        wsServer.getAllSessions().forEach { session ->
            try {
                session.basicRemote.sendBinary(payload)
            } catch (throwable: Throwable) {
                log.warn("exception broadcasting message to session: " + session, throwable)
            }
        }
    }

    fun sendToId(sessionId: String, payload: ByteBuffer) {
        val session = wsServer.getSession(sessionId)
        if (session == null) {
            log.warn("session with id {} not founded", sessionId)
            return
        }
        try {
            session.basicRemote.sendBinary(payload)
        } catch (throwable: Throwable) {
            log.warn("exception sending message to session: " + session, throwable)
        }

    }

    fun sendToIdAsync(sessionId: String, payload: ByteBuffer) {
        sendersExecutor.execute { sendToId(sessionId, payload) }
    }

}

