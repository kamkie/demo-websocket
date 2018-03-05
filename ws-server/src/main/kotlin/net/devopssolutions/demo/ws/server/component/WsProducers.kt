package net.devopssolutions.demo.ws.server.component

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KLogging
import net.devopssolutions.demo.ws.rpc.RpcMessage
import net.devopssolutions.demo.ws.server.handler.excludeOnNextAndRequest
import net.jpountz.lz4.LZ4Factory
import org.nustaq.serialization.FSTConfiguration
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DataBufferFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.EmitterProcessor
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.util.Loggers
import java.util.logging.Level

private val compressorLZ4Factory = LZ4Factory.fastestInstance()
private val serializationConfig = FSTConfiguration.createDefaultConfiguration()
private val objectMapper = ObjectMapper().findAndRegisterModules()

val sch3 = Schedulers.newParallel("baz", 4)

fun <I, O> Flux<RpcMessage<I, O>>.toBinaryMessage(session: WebSocketSession): Flux<WebSocketMessage> {
    return this.flatMap { message ->
        Mono.fromCallable {
            session.binaryMessage { dataBufferFactory ->
                serializeToBuffer(message, dataBufferFactory)
            }
        }.subscribeOn(sch3)

    }
}

fun <I, O> Flux<RpcMessage<I, O>>.toTextMessage(session: WebSocketSession, objectMapper: ObjectMapper): Flux<WebSocketMessage> {
    return this.map { message ->
        session.textMessage(objectMapper.writeValueAsString(message))
    }
}

private fun serializeToBuffer(value: RpcMessage<*, *>, dataBufferFactory: DataBufferFactory): DataBuffer {
//    return dataBufferFactory.wrap(objectMapper.writeValueAsBytes(value))

    val payload = serializationConfig.asSharedByteArray(value, IntArray(1))
    val compressor = compressorLZ4Factory.fastCompressor()
    val compress = compressor.compress(payload)
    return dataBufferFactory.wrap(compress)
}

@Component
class WsProducers {
    companion object : KLogging()

    fun buildSender(session: WebSocketSession): Pair<Flux<WebSocketMessage>, FluxSink<WebSocketMessage>> {
        val emitterProcessor = EmitterProcessor.create<WebSocketMessage>(100_000)
        val sink = emitterProcessor.sink(FluxSink.OverflowStrategy.LATEST)

        val emitter = emitterProcessor
                .log(Loggers.getLogger("emitter"), Level.INFO, true, *excludeOnNextAndRequest)
        return Pair(emitter, sink)
    }

}
