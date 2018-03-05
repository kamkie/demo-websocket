package net.devopssolutions.demo.ws.server.component

import mu.KLogging
import net.devopssolutions.demo.ws.server.handler.RootWebSocketHandler
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux
import java.nio.ByteBuffer
import java.security.Principal

@Component
class WsBroadcaster(
        private val rootWebSocketHandler: RootWebSocketHandler
) {
    companion object : KLogging()

    fun broadcastAsync(action: () -> ByteBuffer): Mono<Void> {
        return rootWebSocketHandler.sessions.values.toFlux()
                .map { (session, sink) ->
                    Mono.fromCallable(action)
                            .map { payload -> session.binaryMessage { it.wrap(payload) } }
                            .subscribe { sink.next(it) }
                }
                .then()
    }

    fun sendToAllAsync(action: (Principal) -> ByteBuffer): Mono<Void> {
        return rootWebSocketHandler.sessions.values.toFlux()
                .map { (session, sink) ->
                    session.handshakeInfo.principal
                            .map(action)
                            .map { payload -> session.binaryMessage { it.wrap(payload) } }
                            .subscribe { sink.next(it) }
                }
                .then()
    }

    fun sendToIdAsync(sessionId: String, action: () -> ByteBuffer) {
        rootWebSocketHandler.sessions[sessionId]?.apply {
            second.next(first.binaryMessage { it.wrap(action()) })
        } ?: logger.warn("session: $sessionId does not exist")
    }

}
