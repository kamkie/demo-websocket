package net.devopssolutions.demo.ws.client.component

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KLogging
import net.devopssolutions.demo.ws.rpc.RpcMessage
import net.devopssolutions.demo.ws.rpc.RpcMethods
import net.devopssolutions.demo.ws.rpc.RpcType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.EmitterProcessor
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import reactor.util.Loggers
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import java.util.concurrent.atomic.AtomicLong
import java.util.logging.Level
import java.util.zip.GZIPOutputStream

@Component
class WsProducer(
        private val objectMapper: ObjectMapper
) {
    companion object : KLogging()

    fun buildSender(session: WebSocketSession): Pair<Flux<WebSocketMessage>, FluxSink<WebSocketMessage>> {
        val emitterProcessor = EmitterProcessor.create<WebSocketMessage>(100)
        val sink = emitterProcessor.sink(FluxSink.OverflowStrategy.BUFFER)

        val ping = sendPing(session).subscribe { sink.next(it) }
        val hello = sendHello(session).subscribe { sink.next(it) }
        val flood = sendFlood(session).subscribe { sink.next(it) }

        val emitter = emitterProcessor
                .doFinally {
                    ping.dispose()
                    hello.dispose()
                    flood.dispose()
                }
                .log(Loggers.getLogger("emitter"), Level.INFO, true)
        return Pair(emitter, sink)
    }

    fun sendHello(session: WebSocketSession): Flux<WebSocketMessage> {
        return Flux.interval(Duration.ofSeconds(5), Duration.ofSeconds(10))
                .map { helloMessage(it) }
                .map { message -> buildTextMessage(session, message) }
                .doOnNext { logger.info("sending new hello message") }
                .log(Loggers.getLogger("sendHello"), Level.INFO, true, *excludeOnNextAndRequest)
    }

    fun sendFlood(session: WebSocketSession): Flux<WebSocketMessage> {
        return Flux.interval(Duration.ofSeconds(10), Duration.ofSeconds(30))
                .map { floodMessage() }
                .map { message -> buildBinaryMessage(session, message) }
                .doOnNext { logger.info("sending new flood message") }
                .log(Loggers.getLogger("sendFlood"), Level.INFO, true, *excludeOnNextAndRequest)
    }

    fun sendPing(session: WebSocketSession): Flux<WebSocketMessage> {
        return Flux.interval(Duration.ofSeconds(30))
                .map { session.pingMessage { it.allocateBuffer(0) } }
                .doOnNext { WsConsumer.logger.info("sending ping") }
                .log(Loggers.getLogger("sendPing"), Level.INFO, true)
    }

    fun logMessagesCount(messagesCountInSecond: AtomicLong): Flux<Long> {
        return Flux.interval(Duration.ofSeconds(1))
                .doOnNext {
                    val count = messagesCountInSecond.getAndSet(0)
                    if (count > 0) {
                        WsConsumer.logger.info("messagesCountInSecond: {}", count)
                    }
                }
    }

    private fun helloMessage(number: Long): RpcMessage<String, Unit> = RpcMessage(
            id = UUID.randomUUID().toString(),
            created = LocalDateTime.now(ZoneOffset.UTC),
            method = RpcMethods.HELLO.method,
            type = RpcType.REQUEST,
            params = "word $number")

    private fun floodMessage(): RpcMessage<Int, Unit> = RpcMessage(
            id = UUID.randomUUID().toString(),
            created = LocalDateTime.now(ZoneOffset.UTC),
            method = RpcMethods.FLOOD.method,
            type = RpcType.REQUEST,
            params = 1_000_000)

    private fun buildBinaryMessage(session: WebSocketSession, message: RpcMessage<*, Unit>): WebSocketMessage =
            session.binaryMessage {
                val buffer = it.allocateBuffer()
                objectMapper.writeValue(GZIPOutputStream(buffer.asOutputStream()), message)
                buffer
            }

    private fun buildTextMessage(session: WebSocketSession, message: RpcMessage<*, Unit>): WebSocketMessage =
            session.textMessage(objectMapper.writeValueAsString(message))

}
