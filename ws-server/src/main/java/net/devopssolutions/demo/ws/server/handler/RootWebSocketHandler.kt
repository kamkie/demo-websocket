package net.devopssolutions.demo.ws.server.handler

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import mu.KLogging
import net.devopssolutions.demo.ws.rpc.RpcMessage
import net.jpountz.lz4.LZ4Factory
import org.nustaq.serialization.FSTConfiguration
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.CloseStatus
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.EmitterProcessor
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.Duration
import java.util.*
import java.util.zip.GZIPInputStream


@Component
class RootWebSocketHandler(
        private val floodHandler: FloodHandler,
        private val objectMapper: ObjectMapper
) : WebSocketHandler {
    companion object : KLogging()

    private val compressorLZ4Factory = LZ4Factory.fastestInstance()
    private val serializationConfig = FSTConfiguration.createDefaultConfiguration()

    override fun handle(session: WebSocketSession): Mono<Void> {
        logger.info("opened ws connection session: {}, endpointConfig: {}", session, session.handshakeInfo)

        val emitterProcessor = EmitterProcessor.create<WebSocketMessage>(100_000, false)
        val sink = emitterProcessor.sink(FluxSink.OverflowStrategy.DROP)
//        buildPinger(session).subscribe { sink.next(it) }

        return session.send(emitterProcessor/*.subscribeOn(Schedulers.newParallel("foo", 4))*/)
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

    private fun sendFloodMessages(session: WebSocketSession, sink: FluxSink<WebSocketMessage>, id: String, numberOfMessages: Int) {
        floodHandler.handle(id, numberOfMessages).mapToWebSocketMessage(session)
                .subscribeOn(Schedulers.newParallel("foo", 8))
                .subscribe { sink.next(it) }
    }

    private fun Flux<RpcMessage<Unit, String>>.mapToWebSocketMessage(session: WebSocketSession): Flux<WebSocketMessage> =
            this.map { message ->
                session.binaryMessage { dataBufferFactory ->
                    writeToBuffer(message, dataBufferFactory)
                }
            }

    private fun buildPinger(session: WebSocketSession): Flux<WebSocketMessage> = Flux.interval(Duration.ofSeconds(10))
            .map { session.pingMessage { it.allocateBuffer(0) } }
            .doOnNext { logger.info("sending ping") }

    private fun writeToBuffer(value: RpcMessage<*, *>, dataBufferFactory: DataBufferFactory): DataBuffer {
        val payload = serializationConfig.asByteArray(value)
        val compressor = compressorLZ4Factory.fastCompressor()
        val maxCompressedLength = compressor.maxCompressedLength(payload.size)
        val compressed = ByteArray(maxCompressedLength)
        val compressedLength = compressor.compress(payload, compressed)
        return dataBufferFactory.wrap(compressed)
    }

    private fun afterConnectionClosed(session: WebSocketSession?, closeStatus: CloseStatus?, exception: Throwable? = null) {
        logger.info("ws connection closed, session: {}, closeReason: {}", session, closeStatus, exception)
    }

    private fun handleMessage(
            session: WebSocketSession,
            sink: FluxSink<WebSocketMessage>,
            message: WebSocketMessage) = when (message.type) {
        WebSocketMessage.Type.PONG -> handlePongMessage(session, sink, message)
        WebSocketMessage.Type.PING -> handlePingMessage(session, sink, message)
        WebSocketMessage.Type.TEXT -> handleTextMessage(session, sink, message)
        WebSocketMessage.Type.BINARY -> handleBinaryMessage(session, sink, message)
    }

    private fun handlePingMessage(session: WebSocketSession, sink: FluxSink<WebSocketMessage>, message: WebSocketMessage) {
        logger.info("incoming Ping: {} will respond with pong", message)
        sink.next(session.pongMessage { it.allocateBuffer(0) })
    }

    private fun handlePongMessage(session: WebSocketSession, sink: FluxSink<WebSocketMessage>, message: WebSocketMessage) {
        logger.info("incoming Pong: {}", message)
    }

    private fun handleTextMessage(session: WebSocketSession, sink: FluxSink<WebSocketMessage>, message: WebSocketMessage) {
        logger.info("onTextMessage length: {}, message: {}", message.payload.readableByteCount(), message.payloadAsText)
    }

    private fun handleBinaryMessage(session: WebSocketSession, sink: FluxSink<WebSocketMessage>, message: WebSocketMessage) {
        val readableByteCount = message.payload.readableByteCount()
        val gzipInputStream = GZIPInputStream(message.payload.asInputStream())
        val readValue = objectMapper.readValue<JsonNode>(gzipInputStream)
        logger.info("onBinaryMessage length: {}, message: {}", readableByteCount, readValue)
        sendFloodMessages(session, sink, UUID.randomUUID().toString(), 1_000_000)
    }
}
