package net.devopssolutions.demo.ws.server.handler

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KLogging
import net.devopssolutions.demo.ws.rpc.RpcMessage
import net.devopssolutions.demo.ws.rpc.RpcMethod
import net.devopssolutions.demo.ws.rpc.RpcMethods
import net.devopssolutions.demo.ws.server.component.toTextMessage
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Flux
import reactor.util.Loggers
import java.util.logging.Level

@Component
@RpcMethod(RpcMethods.HELLO)
class HelloWordHandler(
        private val objectMapper: ObjectMapper
) : RpcMethodHandler {
    companion object : KLogging()

    override fun handle(session: WebSocketSession, message: RpcMessage<Any, Any>): Flux<WebSocketMessage> {
        return Flux.just(message.copy())
                .doOnSubscribe { logger.info("preparing hello message: $message on session: ${session.id}") }
                .log(Loggers.getLogger("sendHello"), Level.INFO, true)
                .toTextMessage(session, objectMapper)
    }
}
