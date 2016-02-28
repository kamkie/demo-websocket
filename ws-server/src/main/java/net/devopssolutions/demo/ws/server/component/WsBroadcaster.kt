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
class WsBroadcaster {
    private val log = org.slf4j.LoggerFactory.getLogger(WsBroadcaster::class.java)

    private val sendersExecutor = LoggingThreadPoolExecutor(10, 20, 2, TimeUnit.MINUTES, ArrayBlockingQueue<Runnable>(10000))

    @Autowired
    private lateinit var wsServer: WsServer

    fun broadcastAsync(action: () -> ByteBuffer) {
        sendersExecutor.execute {
            val payload = action()
            wsServer.getAllSessions().forEach {
                sendersExecutor.execute {
                    wsServer.getSession(it.id)?.apply {
                        synchronized(this) {
                            this.basicRemote.sendBinary(payload)
                        }
                    }
                }
            }
        }
    }

    fun sendToAllAsync(action: (Principal?) -> ByteBuffer) {
        wsServer.getAllSessions().forEach {
            sendersExecutor.execute {
                val payload = action(it.userPrincipal)
                wsServer.getSession(it.id)?.apply {
                    synchronized(this) {
                        this.basicRemote.sendBinary(payload)
                    }
                }
            }
        }
    }

    fun sendToIdAsync(sessionId: String, action: () -> ByteBuffer) {
        if (wsServer.getSession(sessionId) == null) {
            throw IOException("session not exist")
        }
        sendersExecutor.execute {
            val payload = action()
            val task = sessionId.to(payload)
            wsServer.getSession(task.first)?.apply {
                synchronized(this) {
                    this.basicRemote.sendBinary(task.second)
                }
            }
        }
    }
}
