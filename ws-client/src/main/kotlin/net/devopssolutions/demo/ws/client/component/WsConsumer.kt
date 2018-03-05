package net.devopssolutions.demo.ws.client.component

import mu.KLogging
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.WebSocketSession
import org.springframework.web.reactive.socket.client.WebSocketClient
import reactor.core.publisher.*
import reactor.util.Loggers
import java.net.URI
import java.time.Duration
import java.util.concurrent.atomic.AtomicLong
import java.util.logging.Level
import javax.annotation.PreDestroy

@Component
class WsConsumer(
        webSocketClient: WebSocketClient,
        private val wsProducer: WsProducer
) {
    companion object : KLogging()

    private val messagesCountInSecond = AtomicLong(0)
    private val subscription = webSocketClient
            .execute(URI("ws://localhost:8080/ws"), this::handle)
            .retryWhen { it.flatMap { Mono.delay(Duration.ofSeconds(5)) } }
            .log()
            .subscribe()
    private val messageCountLogger = wsProducer.logMessagesCount(messagesCountInSecond)
            .retry()
            .subscribe()

    private fun handle(session: WebSocketSession): Mono<Void> {
        logger.info("opened ws connection session: {}, endpointConfig: {}", session, session.handshakeInfo)

        val (emitter, sink) = buildSender(session)

        return session.send(emitter)
                .mergeWith(createReceiver(session, sink).then())
                .log(Loggers.getLogger("session"), Level.INFO, true)
                .then()
    }

    private fun buildSender(session: WebSocketSession): Pair<Flux<WebSocketMessage>, FluxSink<WebSocketMessage>> {
        val emitterProcessor = EmitterProcessor.create<WebSocketMessage>(1000)
        val sink = emitterProcessor.sink(FluxSink.OverflowStrategy.BUFFER)

        val ping = wsProducer.sendPing(session).subscribe { sink.next(it) }
        val hello = wsProducer.sendHello(session).subscribe { sink.next(it) }
        val flood = wsProducer.sendFlood(session).subscribe { sink.next(it) }

        val emitter = emitterProcessor
                .doFinally {
                    ping.dispose()
                    hello.dispose()
                    flood.dispose()
                }
                .log(Loggers.getLogger("emitter"), Level.INFO, true)
        return Pair(emitter, sink)
    }

    private fun createReceiver(session: WebSocketSession, sink: FluxSink<WebSocketMessage>)
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

    @PreDestroy
    private fun stop() {
        subscription.dispose()
        messageCountLogger.dispose()
    }

}
