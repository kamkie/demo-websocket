package net.devopssolutions.demo.ws.server.handler

import net.devopssolutions.demo.ws.rpc.RpcMethod
import net.devopssolutions.demo.ws.rpc.RpcMethodHandler
import net.devopssolutions.demo.ws.rpc.RpcMethods
import org.springframework.stereotype.Component
import java.security.Principal

@Component
@RpcMethod(RpcMethods.HELLO)
class HelloWordHandler : RpcMethodHandler {
    private val log = org.slf4j.LoggerFactory.getLogger(HelloWordHandler::class.java)

    override fun handle(sessionId: String, id: String, params: Any, user: Principal) {
        log.info("handling rpc message id: {}, method: {} params: {}", id, RpcMethods.HELLO, params)
    }
}
