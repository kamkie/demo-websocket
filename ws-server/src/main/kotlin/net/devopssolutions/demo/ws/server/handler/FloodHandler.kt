package net.devopssolutions.demo.ws.server.handler

import mu.KLogging
import net.devopssolutions.demo.ws.rpc.RpcMessage
import net.devopssolutions.demo.ws.rpc.RpcMethod
import net.devopssolutions.demo.ws.rpc.RpcMethods
import net.devopssolutions.demo.ws.rpc.RpcType
import net.devopssolutions.demo.ws.server.component.toBinaryMessage
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers
import reactor.util.Loggers
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import java.util.logging.Level

@Component
@RpcMethod(RpcMethods.FLOOD)
class FloodHandler : RpcMethodHandler {
    companion object : KLogging()

    override fun handle(session: WebSocketSession, message: RpcMessage<Any, Any>): Flux<WebSocketMessage> {
        @Suppress("UNCHECKED_CAST")
        return Flux.range(0, message.params as Int)
                .doOnSubscribe { logger.info("preparing flood message: $message on session: ${session.id}") }
                .map { number -> createRpcMessage(number.toString(), 20) }
                .log(Loggers.getLogger("sendFlood"), Level.INFO, true, *excludeOnNextAndRequest)
                .toBinaryMessage(session)
                .subscribeOn(Schedulers.newParallel("foo", 8))
    }

    private fun createRpcMessage(id: String, size: Int): RpcMessage<Unit, String> = RpcMessage(
            id = id,
            created = LocalDateTime.now(ZoneOffset.UTC),
            method = RpcMethods.FLOOD.method,
            type = RpcType.RESPONSE,
            response = UUID.randomUUID().toString().repeat(size))

}
