package net.devopssolutions.demo.ws.client.component

import mu.KLogging
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import reactor.core.publisher.SignalType
import reactor.util.Loggers
import java.util.concurrent.atomic.AtomicLong
import java.util.logging.Level

@Component
class WsConsumer {
    companion object : KLogging()

    val messagesCountInSecond = AtomicLong(0)

    fun createReceiver(session: WebSocketSession, sink: FluxSink<WebSocketMessage>)
            : Flux<WebSocketMessage> = session.receive()
            .log(Loggers.getLogger("receiver"), Level.INFO, true, *SignalType.values().filter { it != SignalType.ON_NEXT }.toTypedArray())
            .doOnNext { message ->
                when (message.type) {
                    WebSocketMessage.Type.PING -> handlePingMessage(session, sink, message)
                    WebSocketMessage.Type.TEXT -> handleTextMessage(session, message)
                    WebSocketMessage.Type.BINARY -> handleBinaryMessage(session, message)
                    WebSocketMessage.Type.PONG -> handlePongMessage(session, message)
                }
            }

    private fun handlePingMessage(session: WebSocketSession, sink: FluxSink<WebSocketMessage>, message: WebSocketMessage) {
        logger.info("incoming Ping: {} will respond with pong", message)
        sink.next(session.pongMessage { it.allocateBuffer(0) })
    }

    private fun handlePongMessage(session: WebSocketSession, message: WebSocketMessage) {
        logger.info("incoming Pong: {}", message)
    }

    private fun handleTextMessage(session: WebSocketSession, message: WebSocketMessage) {
        logger.info("onTextMessage length: {}, message: {}", message.payload.readableByteCount(), message.payloadAsText)
    }

    private fun handleBinaryMessage(session: WebSocketSession, message: WebSocketMessage) {
        messagesCountInSecond.incrementAndGet()
    }

}
