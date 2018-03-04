package net.devopssolutions.demo.ws.server.handler

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.IntNode
import mu.KLogging
import net.devopssolutions.demo.ws.rpc.*
import net.devopssolutions.demo.ws.server.component.WsBroadcaster
import net.jpountz.lz4.LZ4Factory
import org.nustaq.serialization.FSTConfiguration
import org.springframework.stereotype.Component
import java.nio.ByteBuffer
import java.security.Principal
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

@Component
@RpcMethod(RpcMethods.FLOOD)
open class FloodHandler(
        private val wsBroadcaster: WsBroadcaster,
        private val objectMapper: ObjectMapper
) : RpcMethodHandler {
    companion object : KLogging()

    private val compressorLZ4Factory = LZ4Factory.fastestInstance()
    private val serializationConfig = FSTConfiguration.createDefaultConfiguration()

    override fun handle(sessionId: String, id: String, params: Any, user: Principal) {
        logger.info("handling rpc message id: {}, method: {} params: {}", id, RpcMethods.FLOOD, params)

        (0..(params as IntNode).intValue()).forEach {
            wsBroadcaster.sendToIdAsync(sessionId) {
                val rpcMessage = createRpcMessage(id, 20)
                val byteBuffer = getByteBuffer(rpcMessage)
                byteBuffer
            }
        }
    }

    private fun getByteBuffer(value: RpcMessage<*, *>): ByteBuffer {
//        val payload = objectMapper.writeValueAsBytes(value)
//        val maxCompressedLength = Snappy.maxCompressedLength(payload.size)
//        val compressed = ByteArray(maxCompressedLength)
//        val compressedLength = Snappy.rawCompress(payload, 0, payload.size, compressed, 0)
//        return ByteBuffer.wrap(compressed, 0, compressedLength)

//        return ByteBuffer.wrap(Snappy.compress(objectMapper.writeValueAsBytes(value)))

//        val byteArrayOutputStream = ByteArrayOutputStream()
//        objectMapper.writeValue(DeflaterOutputStream(byteArrayOutputStream), value)
//        objectMapper.writeValue(GZIPOutputStream(byteArrayOutputStream), value)
//        return ByteBuffer.wrap(byteArrayOutputStream.toByteArray())

//        val payload = objectMapper.writeValueAsBytes(value)
        val payload = serializationConfig.asByteArray(value)
        val compressor = compressorLZ4Factory.fastCompressor()
        val maxCompressedLength = compressor.maxCompressedLength(payload.size)
        val compressed = ByteArray(maxCompressedLength)
        val compressedLength = compressor.compress(payload, 0, payload.size, compressed, 0, maxCompressedLength)
        return ByteBuffer.wrap(compressed, 0, compressedLength)

//        val payload = serializationConfig.asByteArray(value)
//        return ByteBuffer.wrap(payload)
    }

    private fun createRpcMessage(id: String, size: Int): RpcMessage<Unit, Any> = RpcMessage(
            id = id,
            created = LocalDateTime.now(ZoneOffset.UTC),
            method = RpcMethods.FLOOD.method,
            type = RpcType.RESPONSE,
            response = createPayload(size))

    private fun createPayload(size: Int): String {
        val sb = StringBuilder(size * 36 + 100)
        val uuid = UUID.randomUUID().toString()
        (0..size).forEach { sb.append(uuid) }

        return sb.toString()
    }

}
