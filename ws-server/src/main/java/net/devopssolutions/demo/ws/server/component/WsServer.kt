package net.devopssolutions.demo.ws.server.component

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import mu.KLogging
import net.devopssolutions.demo.ws.model.User
import net.devopssolutions.demo.ws.server.util.LoggingThreadPoolExecutor
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.security.Principal
import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.zip.GZIPInputStream
import javax.websocket.*

@Component
open class WsServer(
        private val rpcMethodDispatcher: RpcMethodDispatcher,
        private val objectMapper: ObjectMapper
) : Endpoint() {
    companion object : KLogging()

    private val ping = ByteBuffer.allocate(0)
    private val sessions = ConcurrentHashMap<String, Session>()
    private val handlersExecutor = LoggingThreadPoolExecutor(10, 20, 2, TimeUnit.MINUTES, ArrayBlockingQueue<Runnable>(200))

    fun getAllSessions(): Collection<Session> = sessions.flatMapTo(HashSet(), { it.value.openSessions })

    fun getSession(id: String): Session? {
        return sessions[id]
    }

    fun onBinaryMessage(message: InputStream, session: Session) {
        logger.info("onBinaryMessage id: {}, message: {}", session.id, message)
        try {
            val node = objectMapper.readTree(GZIPInputStream(message))
            handlersExecutor.execute { dispatchMessage(session.id, node, session.userPrincipal) }
        } catch (e: Exception) {
            logger.warn("exception handling ws message session: " + session.id, e)
        }
    }

    fun onTextMessage(message: String, session: Session) {
        logger.info("onTextMessage id: {}, message: {}", session.id, message)
        handlersExecutor.execute {
            try {
                val node = objectMapper.readTree(message)
                dispatchMessage(session.id, node, session.userPrincipal)
            } catch (e: Exception) {
                logger.warn("exception handling ws message session: " + session.id, e)
            }
        }
    }

    private fun dispatchMessage(sessionId: String, node: JsonNode, principal: Principal?) {
        val id = node.get("id").asText()
        val method = node.get("method").asText()
        val params = node.get("params")
        rpcMethodDispatcher.handle(sessionId, id, method, params, principal ?: User("anonymous"))
    }

    fun onPong(message: PongMessage, session: Session) {
        logger.info("onPong id: {}, message: {}", session.id, message)
    }

    override fun onOpen(session: Session, config: EndpointConfig) {
        logger.info("onOpen id: {}", session.id)

        session.maxIdleTimeout = 10000
        session.maxBinaryMessageBufferSize = 100000000
        session.maxTextMessageBufferSize = 1000000
        session.addMessageHandler(PongMessage::class.java) { message -> onPong(message, session) }
        session.addMessageHandler(String::class.java) { message -> onTextMessage(message, session) }
        session.addMessageHandler(InputStream::class.java) { message -> onBinaryMessage(message, session) }

        sessions.put(session.id, session)
    }

    override fun onClose(session: Session, closeReason: CloseReason) {
        logger.info("onClose id: {} closeReason: {}", session.id, closeReason)
        sessions.remove(session.id, session)
    }

    override fun onError(session: Session, exception: Throwable) {
        logger.info("onError id: " + session.id, exception)
        sessions.remove(session.id, session)
        try {
            session.close(CloseReason(CloseReason.CloseCodes.CLOSED_ABNORMALLY, exception.message))
        } catch (e: IOException) {
            logger.warn("exception closing session id: " + session.id, e)
        }
    }

    @Scheduled(fixedRate = 10_000)
    fun sendPings() {
        logger.info("sending pings: {}", sessions.size)
        getAllSessions().forEach { this.sendPing(it) }
    }

    private fun sendPing(session: Session) {
        logger.info("sending ping sessionId: {}, session {}", session.id, session)
        try {
            session.basicRemote.sendPing(ping)
        } catch (e: Exception) {
            val remove = sessions.remove(session.id, session)
            logger.warn("exception sending ping, remove session " + remove, e)
        }
    }

}
