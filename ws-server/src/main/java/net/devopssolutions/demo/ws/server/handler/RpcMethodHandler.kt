package net.devopssolutions.demo.ws.server.handler

import reactor.core.publisher.Mono
import java.security.Principal

interface RpcMethodHandler {

    fun handle(sessionId: String, id: String, params: Mono<Any>, user: Mono<Principal>)
}
