package net.devopssolutions.demo.ws.client.component

import org.glassfish.tyrus.client.ClientManager
import org.glassfish.tyrus.container.jdk.client.JdkClientContainer
import org.glassfish.tyrus.core.CloseReasons
import org.glassfish.tyrus.ext.client.java8.SessionBuilder
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.io.IOException
import java.net.URI
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import javax.annotation.PreDestroy
import javax.websocket.*

@Component
class WsConsumer {
    private val log = org.slf4j.LoggerFactory.getLogger(WsConsumer::class.java)
    private val clientManager = ClientManager.createClient(JdkClientContainer::class.java.name)
    private val sessionBuilder = createSessionBuilder()
    private val ping = ByteBuffer.allocate(0)
    private val messagesCount = AtomicLong(0)
    private val messagesCountInSecond = AtomicLong(0)

    val session = AtomicReference<Session>()

    init {
        log.info("going to open ws connection")
        connect()
    }

    private fun connect(): Boolean {
        try {
            this.session.set(sessionBuilder.connect())
            return true
        } catch (e: Exception) {
            this.session.set(null)
            log.warn("couldn't connect", e)
            return false
        }
    }

    private fun createSessionBuilder(): SessionBuilder = SessionBuilder(clientManager)
            .uri(URI("ws://localhost:8080/ws"))
            .onOpen { session, endpointConfig -> onOpen(session, endpointConfig) }
            .onError { session, throwable -> onError(session, throwable) }
            .onClose { session, closeReason -> onClose(session, closeReason) }
            .messageHandler(PongMessage::class.java, MessageHandler.Whole<PongMessage> { onPong(it) })// bug in tyrus
            .messageHandler(ByteBuffer::class.java) { onMessage(it) }
            .messageHandler(String::class.java) { onMessage(it) }

    private fun onPong(pongMessage: PongMessage) {
        log.info("incoming onPong: {}", pongMessage)
    }

    private fun onMessage(message: ByteBuffer) {
        val count = messagesCount.incrementAndGet()
        messagesCountInSecond.incrementAndGet()
        if (count % 50000 == 1L) {
            log.info("onBinaryMessage count: {}, message: {}", count, message)
        }
    }

    private fun onMessage(message: String) {
        log.info("onTextMessage message: {}", message)
    }

    private fun onOpen(session: Session, endpointConfig: EndpointConfig) {
        log.info("opened ws connection session: {}, endpointConfig: {}", session, endpointConfig)
        messagesCount.set(0)
    }

    private fun onClose(session: Session, closeReason: CloseReason) {
        log.info("closing ws connection session: {}, closeReason: {}", session, closeReason)
    }

    private fun onError(session: Session, throwable: Throwable) {
        log.warn("error in ws session: " + session, throwable)
    }

    @Scheduled(fixedRate = 1000)
    private fun countMessages() {
        val count = messagesCountInSecond.getAndSet(0)
        log.info("messagesCountInSecond: {}", count)
    }

    @Scheduled(fixedRate = 10000)
    private fun sendPing() {
        val session = this.session.get()
        log.info("sending ping: {}", session != null)
        if (session != null) {
            try {
                session.basicRemote.sendPing(ping)
            } catch (e: Exception) {
                this.session.set(null)
                log.warn("exception sending ping", e)
            }
        } else {
            connect()
        }
    }

    @PreDestroy
    private fun stop() {
        val session = this.session.get()
        if (session != null && session.isOpen) {
            try {
                session.close(CloseReasons.GOING_AWAY.closeReason)
            } catch (e: IOException) {
                log.warn("exception closing session", e)
            }
        }
    }

}
