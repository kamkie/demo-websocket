package net.devopssolutions.demo.ws.server.config

import com.fasterxml.jackson.databind.ObjectMapper
import net.devopssolutions.demo.ws.server.handler.RootWebSocketHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.server.WebSocketService
import org.springframework.web.reactive.socket.server.support.HandshakeWebSocketService
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter
import org.springframework.web.reactive.socket.server.upgrade.ReactorNettyRequestUpgradeStrategy
import java.util.*


@Configuration
class WebSocketConfig {

    @Bean
    fun objectMapper(): ObjectMapper = ObjectMapper().findAndRegisterModules()

    @Bean
    fun handlerAdapter() = WebSocketHandlerAdapter(webSocketService())

    @Bean
    fun webSocketService(): WebSocketService = HandshakeWebSocketService(ReactorNettyRequestUpgradeStrategy())

    @Bean
    fun handlerMapping(rootHandler: RootWebSocketHandler): HandlerMapping {
        val map = HashMap<String, WebSocketHandler>()
        map["/ws"] = rootHandler

        val mapping = SimpleUrlHandlerMapping()
        mapping.urlMap = map
        mapping.order = -1 // before annotated controllers
        return mapping
    }

}

