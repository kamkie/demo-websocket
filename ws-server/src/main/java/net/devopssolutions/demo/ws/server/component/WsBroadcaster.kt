package net.devopssolutions.demo.ws.server.component

import net.devopssolutions.demo.ws.server.util.LoggingThreadPoolExecutor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.nio.ByteBuffer
import java.security.Principal
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct

@Component
class WsBroadcaster {
    private val log = org.slf4j.LoggerFactory.getLogger(WsBroadcaster::class.java)

    private val sendersExecutor = LoggingThreadPoolExecutor(10, 20, 2, TimeUnit.MINUTES, ArrayBlockingQueue<Runnable>(10000))
    private val messagesQueue = ArrayBlockingQueue<Pair<String, ByteBuffer>>(100000);

    @Autowired
    private lateinit var wsServer: WsServer

    @PostConstruct
    fun init() {
        sendersExecutor.execute {
            while (true) {
                try {
                    val task = messagesQueue.take()
                    wsServer.getSession(task.first)?.apply {
                        this.basicRemote.sendBinary(task.second)
                    }
                } catch (e: Exception) {
                    log.warn("exception broadcasting message", e)
                }
            }
        }
    }

    fun broadcastAsync(action: () -> ByteBuffer) {
        sendersExecutor.execute {
            val payload = action()
            wsServer.getAllSessions().forEach {
                val task = it.id.to(payload)
                messagesQueue.put(task)
            }
        }
    }

    fun sendToAllAsync(action: (Principal?) -> ByteBuffer) {
        wsServer.getAllSessions().forEach {
            sendersExecutor.execute {
                val payload = action(it.userPrincipal)
                val task = it.id.to(payload)
                messagesQueue.put(task)
            }
        }
    }

    fun sendToIdAsync(sessionId: String, action: () -> ByteBuffer) {
        if (wsServer.getSession(sessionId) == null) {
            log.warn("session not exist")
            return
        }
        sendersExecutor.execute {
            val payload = action()
            val task = sessionId.to(payload)
            messagesQueue.put(task)
        }
    }
}
