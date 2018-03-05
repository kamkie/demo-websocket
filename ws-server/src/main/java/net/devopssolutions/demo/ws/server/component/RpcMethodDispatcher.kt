package net.devopssolutions.demo.ws.server.component

import mu.KLogging
import net.devopssolutions.demo.ws.rpc.RpcMessage
import net.devopssolutions.demo.ws.rpc.RpcMethod
import net.devopssolutions.demo.ws.server.handler.RpcMethodHandler
import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Flux
import kotlin.reflect.full.findAnnotation

@Component
class RpcMethodDispatcher(
        handlers: List<RpcMethodHandler>
) {
    companion object : KLogging()

    private val handlerMap: Map<String?, RpcMethodHandler> = handlers
            .associateBy { it::class.findAnnotation<RpcMethod>()?.value?.method }

    fun dispatch(session: WebSocketSession, message: RpcMessage<Any, Any>): Flux<WebSocketMessage> {
        val handler = handlerMap[message.method]
        if (handler != null) {
            return handler.handle(session, message)
        }
        logger.warn { "handler ${message.method} not found" }
        return Flux.empty()
    }
}
