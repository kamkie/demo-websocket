package net.devopssolutions.demo.ws.client.component

import com.fasterxml.jackson.databind.ObjectMapper
import net.devopssolutions.demo.ws.rpc.RpcMessage
import net.devopssolutions.demo.ws.rpc.RpcMethods
import net.devopssolutions.demo.ws.rpc.RpcType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import java.util.zip.GZIPOutputStream
import javax.websocket.Session

@Component
class WsProducer {
    private val log = org.slf4j.LoggerFactory.getLogger(WsProducer::class.java)
    private val objectMapper = ObjectMapper()

    @Autowired
    private lateinit var wsConsumer: WsConsumer

    @Scheduled(fixedRate = 10000)
    private fun sendHello() {
        val session = wsConsumer.session.get()
        log.info("sending hello: {}", session != null)
        session?.apply {
            try {
                val message = RpcMessage<String, Unit>(
                        id = UUID.randomUUID().toString(),
                        created = LocalDateTime.now(ZoneOffset.UTC),
                        method = RpcMethods.HELLO,
                        type = RpcType.REQUEST,
                        params = "word")
                sendMessage(session, message)
            } catch (e: Exception) {
                log.warn("exception sending hello", e)
            }
        }
    }

    @Scheduled(fixedRate = 30000, initialDelay = 10000)
    private fun sendFlood() {
        val session = wsConsumer.session.get()
        log.info("sending hello: {}", session != null)
        session?.apply {
            try {
                val message = RpcMessage<Int, Unit>(
                        id = UUID.randomUUID().toString(),
                        created = LocalDateTime.now(ZoneOffset.UTC),
                        method = RpcMethods.FLOOD,
                        type = RpcType.REQUEST,
                        params = 1000000)
                sendMessage(session, message)
            } catch (e: Exception) {
                log.warn("exception sending hello", e)
            }
        }
    }

    private fun sendMessage(session: Session, message: RpcMessage<*, Unit>) {
        val sendStream = session.basicRemote.sendStream
        objectMapper.writeValue(GZIPOutputStream(sendStream), message)
    }

}
