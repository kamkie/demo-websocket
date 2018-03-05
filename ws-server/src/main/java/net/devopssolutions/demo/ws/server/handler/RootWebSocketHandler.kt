package net.devopssolutions.demo.ws.server.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import mu.KLogging
import net.devopssolutions.demo.ws.rpc.RpcMessage
import net.devopssolutions.demo.ws.server.component.RpcMethodDispatcher
import net.devopssolutions.demo.ws.server.component.WsProducers
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono
import reactor.core.publisher.SignalType
import reactor.util.Loggers
import java.util.logging.Level
import java.util.zip.GZIPInputStream

val excludeOnNext = SignalType.values().filter { it != SignalType.ON_NEXT }.toTypedArray()
val excludeOnNextAndRequest = SignalType.values().asSequence()
        .minus(SignalType.REQUEST)
        .minus(SignalType.ON_NEXT)
        .toList().toTypedArray()

@Component
class RootWebSocketHandler(
        private val rpcMethodDispatcher: RpcMethodDispatcher,
        private val wsProducers: WsProducers,
        private val objectMapper: ObjectMapper
) : WebSocketHandler {
    companion object : KLogging()


    override fun handle(session: WebSocketSession): Mono<Void> {
        logger.info("opened ws connection session: {}, endpointConfig: {}", session, session.handshakeInfo)

        val (emitter, sink) = wsProducers.buildSender(session)

        return session.send(emitter)
                .mergeWith(createReceiver(session, sink).then())
                .log(Loggers.getLogger("session"), Level.INFO, true)
                .then()
    }

    fun createReceiver(session: WebSocketSession, sink: FluxSink<WebSocketMessage>)
            : Flux<WebSocketMessage> {
        return session.receive()
                .log(Loggers.getLogger("receiver"), Level.INFO, true, *excludeOnNext)
                .doOnNext { message ->
                    when (message.type) {
                        WebSocketMessage.Type.PING -> handlePingMessage(session, sink, message)
                        WebSocketMessage.Type.TEXT -> handleTextMessage(session, sink, message)
                        WebSocketMessage.Type.BINARY -> handleBinaryMessage(session, sink, message)
                        WebSocketMessage.Type.PONG -> handlePongMessage(session, sink, message)
                    }
                }
    }

    private fun handlePingMessage(session: WebSocketSession, sink: FluxSink<WebSocketMessage>, message: WebSocketMessage) {
        logger.info("incoming Ping: {} will respond with pong", message)
        sink.next(session.pongMessage { it.allocateBuffer(0) })
    }

    private fun handlePongMessage(session: WebSocketSession, sink: FluxSink<WebSocketMessage>, message: WebSocketMessage) {
        logger.info { "incoming Pong: $message on session: $session, sink.isCancelled: ${sink.isCancelled}" }
    }

    private fun handleTextMessage(session: WebSocketSession, sink: FluxSink<WebSocketMessage>, message: WebSocketMessage) {
        val payload = message.payloadAsText
        logger.info { "incoming TextMessage: $payload with length: ${message.payload.readableByteCount()} on session: $session, sink.isCancelled: ${sink.isCancelled}" }
        val decodedMessage = objectMapper.readValue<RpcMessage<Any, Any>>(payload)
        rpcMethodDispatcher.dispatch(session, decodedMessage).subscribe { sink.next(it) }
    }

    private fun handleBinaryMessage(session: WebSocketSession, sink: FluxSink<WebSocketMessage>, message: WebSocketMessage) {
        val readableByteCount = message.payload.readableByteCount()
        val gzipInputStream = GZIPInputStream(message.payload.asInputStream())
        val decodedMessage = objectMapper.readValue<RpcMessage<Any, Any>>(gzipInputStream)
        logger.info { "incoming BinaryMessage with length: $readableByteCount on session: $session, sink.isCancelled: ${sink.isCancelled}, message: $decodedMessage" }
        rpcMethodDispatcher.dispatch(session, decodedMessage).subscribe { sink.next(it) }
    }
}
