package net.devopssolutions.demo.ws.server.handler

import mu.KLogging
import net.devopssolutions.demo.ws.rpc.RpcMethod
import net.devopssolutions.demo.ws.rpc.RpcMethodHandler
import net.devopssolutions.demo.ws.rpc.RpcMethods
import org.springframework.stereotype.Component
import java.security.Principal

@Component
@RpcMethod(RpcMethods.HELLO)
open class HelloWordHandler : RpcMethodHandler {
    companion object : KLogging()

    override fun handle(sessionId: String, id: String, params: Any, user: Principal) {
        logger.info("handling rpc message id: {}, method: {} params: {}", id, RpcMethods.HELLO, params)
    }
}
