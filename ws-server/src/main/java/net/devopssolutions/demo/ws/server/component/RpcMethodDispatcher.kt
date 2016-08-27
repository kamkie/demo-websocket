package net.devopssolutions.demo.ws.server.component

import net.devopssolutions.demo.ws.rpc.RpcMethod
import net.devopssolutions.demo.ws.rpc.RpcMethodHandler
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.security.Principal

@Component
open class RpcMethodDispatcher {

    val log = LoggerFactory.getLogger(javaClass)

    lateinit var handlerMap: Map<String, RpcMethodHandler>
        private set
        get

    @Autowired
    private fun init(handlers: List<RpcMethodHandler>) {
        handlerMap = handlers.associateBy { it.javaClass.getAnnotation(RpcMethod::class.java).value }
    }

    @SuppressWarnings("unchecked")
    fun handle(sessionId: String, id: String, method: String, params: Any, user: Principal) {
        val rpcMethodHandler = handlerMap[method]
        if (rpcMethodHandler != null) {
            rpcMethodHandler.handle(sessionId, id, params, user)
        } else {
            log.warn("no handler for method: {} ", method)
        }
    }
}
