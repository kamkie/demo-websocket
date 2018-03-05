package net.devopssolutions.demo.ws.client.component

import mu.KLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.CloseStatus
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.WebSocketSession
import org.springframework.web.reactive.socket.client.WebSocketClient
import reactor.core.Disposable
import reactor.core.publisher.EmitterProcessor
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.net.URI
import java.time.Duration
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import javax.annotation.PreDestroy

@Component
open class WsConsumer(
        private val webSocketClient: WebSocketClient,
        private val wsProducer: WsProducer
) {
    companion object : KLogging()

    private val messagesCount = AtomicLong(0)
    private val messagesCountInSecond = AtomicLong(0)
    private val subscriptionReference = AtomicReference<Disposable?>()

    private fun connect() {
        logger.info("connecting ...")
        val subscription = webSocketClient
                .execute(URI("ws://localhost:8080/ws"), this::handle)
                .subscribe()
        if (subscriptionReference.compareAndSet(null, subscription)) {
            messagesCount.set(0)

            logger.info("subscription set")
        } else {
            logger.warn("another session is connected")
            subscription.dispose()
        }
    }

    private fun handle(session: WebSocketSession): Mono<Void> {
        logger.info("opened ws connection session: {}, endpointConfig: {}", session, session.handshakeInfo)

        val emitterProcessor = EmitterProcessor.create<WebSocketMessage>(1000, false)
        val sink = emitterProcessor.sink(FluxSink.OverflowStrategy.BUFFER)
        buildPinger(session).subscribe { sink.next(it) }
        wsProducer.sendHello(session).subscribe { sink.next(it) }
        wsProducer.sendFlood(session).subscribe { sink.next(it) }

        return session.send(emitterProcessor.subscribeOn(Schedulers.newParallel("foo", 4)))
                .mergeWith(createReceiver(session, sink).subscribeOn(Schedulers.newParallel("bar", 4)).then())
                .then()
                .doOnError { exception -> afterConnectionClosed(session, CloseStatus.SERVER_ERROR, exception) }
                .doOnSuccess { afterConnectionClosed(session, CloseStatus.GOING_AWAY) }
                .doOnCancel {
                    logger.warn("will close session")
                    session.close().block()
                }
                .doOnSubscribe { logger.info("starting {}", it) }
    }

    private fun createReceiver(session: WebSocketSession, sink: FluxSink<WebSocketMessage>)
            : Flux<WebSocketMessage> = session.receive().doOnNext { message -> handleMessage(session, sink, message) }

    private fun buildPinger(session: WebSocketSession): Flux<WebSocketMessage> = Flux.interval(Duration.ofSeconds(30))
            .map { session.pingMessage { it.allocateBuffer(0) } }
            .doOnNext { logger.info("sending ping") }

    private fun afterConnectionClosed(session: WebSocketSession?, closeStatus: CloseStatus?, exception: Throwable? = null) {
        logger.info("ws connection closed, session: {}, closeReason: {}", session, closeStatus, exception)
    }

    private fun handleMessage(
            session: WebSocketSession,
            sink: FluxSink<WebSocketMessage>,
            message: WebSocketMessage) = when (message.type) {
        WebSocketMessage.Type.PING -> handlePingMessage(session, sink, message)
        WebSocketMessage.Type.TEXT -> handleTextMessage(session, message)
        WebSocketMessage.Type.BINARY -> handleBinaryMessage(session, message)
        WebSocketMessage.Type.PONG -> handlePongMessage(session, message)
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
        val count = messagesCount.incrementAndGet()
        messagesCountInSecond.incrementAndGet()
        if (count % 100_000 == 1L) {
            logger.info("onBinaryMessage count: {}, message: {}, length: {}", count, message, message.payload.readableByteCount())
        }
    }

    @Scheduled(fixedRate = 1_000)
    private fun countMessages() {
        val count = messagesCountInSecond.getAndSet(0)
        logger.info("messagesCountInSecond: {}", count)
    }

    @Scheduled(fixedRate = 5_000)
    private fun reConnect() {
        val disposable = this.subscriptionReference.updateAndGet {
            if (it?.isDisposed == true) null else it
        }
        if (disposable == null) {
            connect()
        }
    }

    @PreDestroy
    private fun stop() {
        subscriptionReference.getAndSet(null)?.dispose()
    }

}
