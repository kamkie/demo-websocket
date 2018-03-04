package net.devopssolutions.demo.ws.client.component

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KLogging
import net.devopssolutions.demo.ws.rpc.RpcMessage
import net.devopssolutions.demo.ws.rpc.RpcMethods
import net.devopssolutions.demo.ws.rpc.RpcType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Flux
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import java.util.zip.GZIPOutputStream

@Component
open class WsProducer(
        private val objectMapper: ObjectMapper
) {
    companion object : KLogging()


    fun sendHello(session: WebSocketSession): Flux<WebSocketMessage> {
        return Flux.interval(Duration.ofSeconds(0), Duration.ofSeconds(30))
                .map { helloMessage(it) }
                .map { message -> buildMessage(session, message) }
    }

    private fun helloMessage(number: Long): RpcMessage<String, Unit> = RpcMessage(
            id = UUID.randomUUID().toString(),
            created = LocalDateTime.now(ZoneOffset.UTC),
            method = RpcMethods.HELLO.method,
            type = RpcType.REQUEST,
            params = "word $number")

    fun sendFlood(session: WebSocketSession): Flux<WebSocketMessage> {
        return Flux.interval(Duration.ofSeconds(10), Duration.ofSeconds(30))
                .map { floodMessage(it) }
                .map { message -> buildMessage(session, message) }
    }

    private fun floodMessage(number: Long): RpcMessage<Int, Unit> = RpcMessage(
            id = UUID.randomUUID().toString(),
            created = LocalDateTime.now(ZoneOffset.UTC),
            method = RpcMethods.FLOOD.method,
            type = RpcType.REQUEST,
            params = 1_000_000)

    private fun buildMessage(session: WebSocketSession, message: RpcMessage<*, Unit>): WebSocketMessage =
            session.binaryMessage {
                val buffer = it.allocateBuffer()
                objectMapper.writeValue(GZIPOutputStream(buffer.asOutputStream()), message)
                buffer
            }

}
