package net.devopssolutions.demo.ws.server.component

import mu.KLogging
import net.devopssolutions.demo.ws.rpc.RpcMethod
import net.devopssolutions.demo.ws.rpc.RpcMethodHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.security.Principal

@Component
open class RpcMethodDispatcher {
    companion object : KLogging()

    lateinit var handlerMap: Map<String, RpcMethodHandler>
        private set
        get

    @Autowired
    private fun init(handlers: List<RpcMethodHandler>) {
        handlerMap = handlers.associateBy { it.javaClass.getAnnotation(RpcMethod::class.java).value.method }
    }

    @SuppressWarnings("unchecked")
    fun handle(sessionId: String, id: String, method: String, params: Any, user: Principal) {
        val rpcMethodHandler = handlerMap[method]
        if (rpcMethodHandler != null) {
            rpcMethodHandler.handle(sessionId, id, params, user)
        } else {
            logger.warn("no handler for method: {} ", method)
        }
    }
}
