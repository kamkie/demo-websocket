//package net.devopssolutions.demo.ws.server.component
//
//import mu.KLogging
//import net.devopssolutions.demo.ws.rpc.RpcMethod
//import net.devopssolutions.demo.ws.server.handler.RpcMethodHandler
//import org.springframework.stereotype.Component
//import reactor.core.publisher.Mono
//import java.security.Principal
//import kotlin.reflect.full.findAnnotation
//
//@Component
//open class RpcMethodDispatcher(handlers: List<RpcMethodHandler>) {
//    companion object : KLogging()
//
//    private val handlerMap: Map<String?, RpcMethodHandler> = handlers
//            .associateBy { it::class.findAnnotation<RpcMethod>()?.value?.method }
//
//    @SuppressWarnings("unchecked")
//    fun handle(sessionId: String, id: String, method: String, params: Any, user: Mono<Principal>) {
//        val rpcMethodHandler = handlerMap[method]
//        if (rpcMethodHandler != null) {
//            rpcMethodHandler.handle(sessionId, id, params, user)
//        } else {
//            logger.warn("no handler for method: {} ", method)
//        }
//    }
//}
