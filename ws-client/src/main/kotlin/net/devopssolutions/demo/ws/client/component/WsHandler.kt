package net.devopssolutions.demo.ws.client.component

import mu.KLogging
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketSession
import org.springframework.web.reactive.socket.client.WebSocketClient
import reactor.core.publisher.Mono
import reactor.core.publisher.SignalType
import reactor.util.Loggers
import java.net.URI
import java.time.Duration
import java.util.logging.Level
import javax.annotation.PreDestroy

val excludeOnNextAndRequest = SignalType.values().asSequence()
        .minus(SignalType.REQUEST)
        .minus(SignalType.ON_NEXT)
        .toList().toTypedArray()

@Component
class WsHandler(
        webSocketClient: WebSocketClient,
        private val wsConsumer: WsConsumer,
        private val wsProducer: WsProducer
) {
    companion object : KLogging()

    private val subscription = webSocketClient
            .execute(URI("ws://localhost:8080/ws"), ::registerHandlers)
            .retryWhen { it.flatMap { Mono.delay(Duration.ofSeconds(5)) } }
            .log()
            .subscribe()
    private val messageCountLogger = wsProducer.logMessagesCount(wsConsumer.messagesCountInSecond)
            .retry()
            .subscribe()

    private fun registerHandlers(session: WebSocketSession): Mono<Void> {
        logger.info("opened ws connection session: {}, endpointConfig: {}", session, session.handshakeInfo)

        val (emitter, sink) = wsProducer.buildSender(session)

        return session.send(emitter)
                .mergeWith(wsConsumer.createReceiver(session, sink).then())
                .log(Loggers.getLogger("session"), Level.INFO, true)
                .then()
    }

    @PreDestroy
    private fun stop() {
        subscription.dispose()
        messageCountLogger.dispose()
    }

}
