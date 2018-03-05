//package net.devopssolutions.demo.ws.server.component
//
//import mu.KLogging
//import net.devopssolutions.demo.ws.server.util.LoggingThreadPoolExecutor
//import org.springframework.stereotype.Component
//import reactor.core.publisher.Mono
//import java.io.IOException
//import java.nio.ByteBuffer
//import java.security.Principal
//import java.util.concurrent.ArrayBlockingQueue
//import java.util.concurrent.TimeUnit
//
//@Component
//open class WsBroadcaster(
//        private val wsServer: WsServer
//) {
//    companion object : KLogging()
//
//    private val sendersExecutor = LoggingThreadPoolExecutor(16, 32, 2, TimeUnit.MINUTES, ArrayBlockingQueue<Runnable>(1000000))
//
//    fun broadcastAsync(action: () -> ByteBuffer) {
//        sendersExecutor.execute {
//            val payload = action()
//            wsServer.getAllSessions().forEach {
//                sendersExecutor.execute {
//                    sendPayload(it.id, payload)
//                }
//            }
//        }
//    }
//
//    fun sendToAllAsync(action: (Mono<Principal>) -> ByteBuffer) {
//        wsServer.getAllSessions().forEach {
//            sendersExecutor.execute {
//                val payload = action(it.handshakeInfo.principal)
//                sendPayload(it.id, payload)
//            }
//        }
//    }
//
//    fun sendToIdAsync(sessionId: String, action: () -> ByteBuffer) {
//        if (wsServer.getSession(sessionId) == null) {
//            throw IOException("session not exist")
//        }
//        sendersExecutor.execute {
//            val payload = action()
//            sendPayload(sessionId, payload)
//        }
//    }
//
//    private fun sendPayload(sessionId: String, payload: ByteBuffer) {
//        wsServer.getSession(sessionId).apply {
//                this.basicRemote.sendBinary(payload)
//        }
//    }
//}
