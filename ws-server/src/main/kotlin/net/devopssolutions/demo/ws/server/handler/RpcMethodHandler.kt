package net.devopssolutions.demo.ws.server.handler

import net.devopssolutions.demo.ws.rpc.RpcMessage
import org.springframework.web.reactive.socket.WebSocketMessage
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Flux

interface RpcMethodHandler {

    fun handle(session: WebSocketSession, message: RpcMessage<Any, Any>): Flux<WebSocketMessage>
}
