package net.devopssolutions.demo.ws.server.handler

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KLogging
import net.devopssolutions.demo.ws.rpc.RpcMessage
import net.devopssolutions.demo.ws.rpc.RpcMethod
import net.devopssolutions.demo.ws.rpc.RpcMethods
import net.devopssolutions.demo.ws.rpc.RpcType
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

@Component
@RpcMethod(RpcMethods.FLOOD)
open class FloodHandler(
//        private val wsBroadcaster: WsBroadcaster,
        private val objectMapper: ObjectMapper
) {
    companion object : KLogging()


    fun handle(id: String, params: Int): Flux<RpcMessage<Unit, String>> {
        logger.info("will send flood rpc message id: {}, method: {} params: {}", id, RpcMethods.FLOOD, params)

        return Flux.range(0, params)
                .map { number -> createRpcMessage(number.toString(), 20) }
    }

    private fun createRpcMessage(id: String, size: Int): RpcMessage<Unit, String> = RpcMessage(
            id = id,
            created = LocalDateTime.now(ZoneOffset.UTC),
            method = RpcMethods.FLOOD.method,
            type = RpcType.RESPONSE,
            response = createPayload(size))

    private fun createPayload(size: Int): String {
        val sb = StringBuilder(size * 36 + 100)
        val uuid = UUID.randomUUID().toString()
        (0..size).forEach { sb.append(uuid) }

        return sb.toString()
    }

}
