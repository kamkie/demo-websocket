package net.devopssolutions.demo.ws.server.component

import net.devopssolutions.demo.ws.rpc.RpcMethod
import net.devopssolutions.demo.ws.rpc.RpcMethodHandler
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.security.Principal

@Component
class RpcMethodDispatcher {

    val log = LoggerFactory.getLogger(javaClass)

    lateinit var handlerMap: Map<String, RpcMethodHandler>
        private set
        get

    @Autowired
    private fun init(handlers: List<RpcMethodHandler>) {
        handlerMap = handlers.associateBy { it.javaClass.getAnnotation(RpcMethod::class.java).value }
    }

    @SuppressWarnings("unchecked")
    fun handle(id: String, method: String, params: Any, user: Principal) {
        handlerMap[method]?.apply {
            handle(id, params, user)
        } ?: log.warn("no handler for method: {} ", method)
    }
}
