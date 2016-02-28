package net.devopssolutions.demo.ws.server.handler

import com.fasterxml.jackson.databind.ObjectMapper
import net.devopssolutions.demo.ws.rpc.*
import net.devopssolutions.demo.ws.server.component.WsBroadcaster
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.security.Principal
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import java.util.zip.GZIPOutputStream
import javax.annotation.PostConstruct

@Component
@RpcMethod(RpcMethods.FLOOD)
class FloodHandler : RpcMethodHandler {
    private val log = org.slf4j.LoggerFactory.getLogger(FloodHandler::class.java)

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var wsBroadcaster: WsBroadcaster

    private lateinit var payload: ByteBuffer

    @PostConstruct
    private fun init() {
        val rpcMessage = createRpcMessage(UUID.randomUUID().toString(), 100000)
        payload = ByteBuffer.wrap(getByteBuffer(rpcMessage));
    }


    override fun handle(id: String, params: Any, user: Principal) {
        log.info("handling rpc message id: {}, method: {} params: {}", id, RpcMethods.FLOOD, params)


        for (i in 0..Int.MAX_VALUE) {
            wsBroadcaster.broadcastQueue(payload)
        }

        //        Observable.range(0, (params as IntNode).intValue())
        //                .map { createRpcMessage(id, 100000) }
        //                .map { getByteBuffer(it) }
        //                .doOnError { log.warn("exception in flood handler", it) }
        //                .forEach {
        //                    for (i in 0..100) {
        //                        wsBroadcaster.broadcastQueue(it)
        //                    }
        //                }
    }

    private fun getByteBuffer(value: RpcMessage<*, *>): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        objectMapper.writeValue(GZIPOutputStream(byteArrayOutputStream), value)
        return byteArrayOutputStream.toByteArray()
    }

    private fun createRpcMessage(id: String, size: Int): RpcMessage<Unit, Any> = RpcMessage(
            id = id,
            created = LocalDateTime.now(ZoneOffset.UTC),
            method = RpcMethods.FLOOD,
            type = RpcType.RESPONSE,
            response = createPayload(size))

    private fun createPayload(size: Int): String {
        val sb = StringBuilder(10000)
        val uuid = UUID.randomUUID().toString()
        (0..size).forEach { sb.append(uuid) }

        return sb.toString()
    }

}
