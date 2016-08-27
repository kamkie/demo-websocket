package net.devopssolutions.demo.ws.server.component

import net.devopssolutions.demo.ws.server.util.LoggingThreadPoolExecutor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.IOException
import java.nio.ByteBuffer
import java.security.Principal
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit

@Component
open class WsBroadcaster {
    private val log = org.slf4j.LoggerFactory.getLogger(WsBroadcaster::class.java)

    private val sendersExecutor = LoggingThreadPoolExecutor(16, 32, 2, TimeUnit.MINUTES, ArrayBlockingQueue<Runnable>(1000000))

    @Autowired
    private lateinit var wsServer: WsServer

    fun broadcastAsync(action: () -> ByteBuffer) {
        sendersExecutor.execute {
            val payload = action()
            wsServer.getAllSessions().forEach {
                sendersExecutor.execute {
                    sendPayload(it.id, payload)
                }
            }
        }
    }

    fun sendToAllAsync(action: (Principal?) -> ByteBuffer) {
        wsServer.getAllSessions().forEach {
            sendersExecutor.execute {
                val payload = action(it.userPrincipal)
                sendPayload(it.id, payload)
            }
        }
    }

    fun sendToIdAsync(sessionId: String, action: () -> ByteBuffer) {
        if (wsServer.getSession(sessionId) == null) {
            throw IOException("session not exist")
        }
        sendersExecutor.execute {
            val payload = action()
            sendPayload(sessionId, payload)
        }
    }

    private fun sendPayload(sessionId: String, payload: ByteBuffer) {
        wsServer.getSession(sessionId)?.apply {
//            synchronized(this) {
            if (this.isOpen) {
                this.basicRemote.sendBinary(payload)
            }
//            }
        }
    }
}
