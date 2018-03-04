package net.devopssolutions.demo.ws.client.component

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.socket.*
import org.springframework.web.socket.adapter.NativeWebSocketSession
import org.springframework.web.socket.client.WebSocketConnectionManager
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.handler.AbstractWebSocketHandler
import java.io.IOException
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import javax.annotation.PreDestroy

@Component
open class WsConsumer : AbstractWebSocketHandler() {
    private val log = org.slf4j.LoggerFactory.getLogger(WsConsumer::class.java)
    private val ping = ByteBuffer.allocate(0)
    private val messagesCount = AtomicLong(0)
    private val connectionManager = createConnectionManager()
    private val messagesCountInSecond = AtomicLong(0)

    val sessionReference = AtomicReference<NativeWebSocketSession>()

    init {
        log.info("going to open ws connection")
        connect()
    }

    private fun connect(): Boolean {
        log.info("connecting ...")
        try {
            connectionManager.stop()
            connectionManager.start()
            return true
        } catch (e: Exception) {
            log.warn("couldn't connect", e)
            return false
        }
    }

    private fun createConnectionManager(): WebSocketConnectionManager =
            WebSocketConnectionManager(StandardWebSocketClient(), this, "ws://localhost:8080/ws")

    override fun handleTransportError(session: WebSocketSession?, exception: Throwable?) {
        log.warn("error in ws session: $session", exception)
    }

    override fun afterConnectionClosed(session: WebSocketSession?, closeStatus: CloseStatus?) {
        log.info("closing ws connection session: {}, closeReason: {}", session, closeStatus)
        sessionReference.compareAndSet(session as NativeWebSocketSession?, null)
    }

    override fun handlePongMessage(session: WebSocketSession?, message: PongMessage) {
        log.info("incoming onPong: {}", message)
    }

    override fun handleTextMessage(session: WebSocketSession?, message: TextMessage) {
        log.info("onTextMessage count: {}, message: {}", message.payloadLength, message.payload)
    }

    override fun handleBinaryMessage(session: WebSocketSession?, message: BinaryMessage) {
        val count = messagesCount.incrementAndGet()
        messagesCountInSecond.incrementAndGet()
        if (count % 50000 == 1L) {
            log.info("onBinaryMessage count: {}, message: {}", count, message)
        }
    }

    override fun afterConnectionEstablished(session: WebSocketSession) {
        log.info("opened ws connection session: {}, endpointConfig: {}", session, session.remoteAddress)
        messagesCount.set(0)
        sessionReference.compareAndSet(null, session as NativeWebSocketSession?)
    }

    @Scheduled(fixedRate = 1000)
    private fun countMessages() {
        val count = messagesCountInSecond.getAndSet(0)
        log.info("messagesCountInSecond: {}", count)
    }

    @Scheduled(fixedRate = 10_000)
    private fun sendPing() {
        val session = this.sessionReference.get()
        log.info("sending ping: {}", session != null)
        if (session != null) {
            try {
                session.sendMessage(PingMessage(ping))
            } catch (e: Exception) {
                this.sessionReference.set(null)
                log.warn("exception sending ping", e)
            }
        } else {
            connect()
        }
    }

    @PreDestroy
    private fun stop() {
        val session = this.sessionReference.get()
        if (session != null && session.isOpen) {
            try {
                session.close(CloseStatus.GOING_AWAY)
            } catch (e: IOException) {
                log.warn("exception closing session", e)
            }
        }
    }

}
