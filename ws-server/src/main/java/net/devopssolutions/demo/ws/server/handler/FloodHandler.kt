package net.devopssolutions.demo.ws.server.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.IntNode
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
import java.util.stream.IntStream
import java.util.zip.GZIPOutputStream

@Component
@RpcMethod(RpcMethods.FLOOD)
class FloodHandler : RpcMethodHandler {
    private val log = org.slf4j.LoggerFactory.getLogger(FloodHandler::class.java)

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var wsBroadcaster: WsBroadcaster

    override fun handle(id: String, params: Any, user: Principal) {
        log.info("handling rpc message id: {}, method: {} params: {}", id, RpcMethods.FLOOD, params)

        IntStream.range(0, (params as IntNode).intValue())
                .parallel()
                .mapToObj { createRpcMessage(id, 20) }
                .map { getByteBuffer(it) }
                .forEach {
                    wsBroadcaster.broadcastQueue(it)
                }
    }

    private fun getByteBuffer(value: RpcMessage<*, *>): ByteBuffer {
        val byteArrayOutputStream = ByteArrayOutputStream()
        objectMapper.writeValue(GZIPOutputStream(byteArrayOutputStream), value)
        return ByteBuffer.wrap(byteArrayOutputStream.toByteArray())
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
