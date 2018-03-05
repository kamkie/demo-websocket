package net.devopssolutions.demo.ws.client.component

import mu.KLogging
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import reactor.util.Loggers
import java.util.concurrent.atomic.AtomicLong
import java.util.logging.Level


@Component
class WsConsumer {
    companion object : KLogging()

    val messagesCountInSecond = AtomicLong(0)

    fun createReceiver(session: WebSocketSession, sink: FluxSink<WebSocketMessage>)
            : Flux<WebSocketMessage> {
        return session.receive()
                .log(Loggers.getLogger("receiver"), Level.INFO, true, *excludeOnNextAndRequest)
                .doOnNext { message ->
                    when (message.type) {
                        WebSocketMessage.Type.PING -> handlePingMessage(session, sink, message)
                        WebSocketMessage.Type.TEXT -> handleTextMessage(session, sink, message)
                        WebSocketMessage.Type.BINARY -> handleBinaryMessage(session, sink, message)
                        WebSocketMessage.Type.PONG -> handlePongMessage(session, sink, message)
                    }
                }
                .doOnComplete { throw RuntimeException("force restart :P") }
    }

    private fun handlePingMessage(session: WebSocketSession, sink: FluxSink<WebSocketMessage>, message: WebSocketMessage) {
        logger.info("incoming Ping: {} will respond with pong", message)
        sink.next(session.pongMessage { it.allocateBuffer(0) })
    }

    private fun handlePongMessage(session: WebSocketSession, sink: FluxSink<WebSocketMessage>, message: WebSocketMessage) {
        logger.info { "incoming Pong: $message on session: $session, sink.isCancelled: ${sink.isCancelled}" }
    }

    private fun handleTextMessage(session: WebSocketSession, sink: FluxSink<WebSocketMessage>, message: WebSocketMessage) {
        logger.info { "incoming TextMessage: ${message.payloadAsText} with length: ${message.payload.readableByteCount()} on session: $session, sink.isCancelled: ${sink.isCancelled}" }
    }

    private fun handleBinaryMessage(session: WebSocketSession, sink: FluxSink<WebSocketMessage>, message: WebSocketMessage) {
        logger.debug { "incoming BinaryMessage with length: ${message.payload.readableByteCount()} on session: $session, sink.isCancelled: ${sink.isCancelled}" }
        messagesCountInSecond.incrementAndGet()
    }

}
