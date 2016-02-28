package net.devopssolutions.demo.ws.server.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.IntNode
import net.devopssolutions.demo.ws.rpc.*
import net.devopssolutions.demo.ws.server.component.WsServer
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import rx.Observable
import rx.functions.Func1

import javax.websocket.Session
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.security.Principal
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.stream.Collectors
import java.util.zip.GZIPOutputStream

@Component
@RpcMethod(RpcMethods.FLOOD)
class FloodHandler : RpcMethodHandler {
    private val log = org.slf4j.LoggerFactory.getLogger(FloodHandler::class.java)

    @Autowired
    private lateinit var wsServer: WsServer

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    override fun handle(id: String, params: Any, user: Principal) {
        log.info("handling rpc message id: {}, method: {} params: {}", id, RpcMethods.FLOOD, params)

        val sessions = wsServer.allSessions

        Observable.range(0, (params as IntNode).intValue())
                .map { createRpcMessage(id, it) }
                .map { getByteBuffer(it) }
                .onErrorReturn { null }
                .filter { it != null }
                .forEach {
                    sessions.forEach { session ->
                        synchronized (session) {
                            session.basicRemote.sendBinary(it)
                        }
                    }
                }
    }

    private fun getByteBuffer(value: RpcMessage<*, *>): ByteBuffer? {
        val byteArrayOutputStream = ByteArrayOutputStream()
        objectMapper.writeValue(GZIPOutputStream(byteArrayOutputStream), value)
        return ByteBuffer.wrap(byteArrayOutputStream.toByteArray())
    }

    private fun createRpcMessage(id: String, response: Any): RpcMessage<*, *> = RpcMessage.builder<Any, Any>()
            .id(id)
            .method(RpcMethods.FLOOD)
            .type(RpcType.RESPONSE)
            .created(LocalDateTime.now(ZoneOffset.UTC))
            .response(response)
            .build()

}