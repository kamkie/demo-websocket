package net.devopssolutions.demo.ws.server.config

import net.devopssolutions.demo.ws.server.component.WsServer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.server.standard.ServerEndpointExporter
import org.springframework.web.socket.server.standard.ServerEndpointRegistration

import javax.servlet.ServletContext
import javax.websocket.WebSocketContainer

@Configuration
open class WebSocketConfig {

    @Bean
    open fun chatEndpointRegistration(wsServer: WsServer) = ServerEndpointRegistration("/ws", wsServer)

    @Bean
    open fun endpointExporter() = ServerEndpointExporter()

    @Autowired
    private fun init(servletContext: ServletContext) {
        val serverContainer = servletContext.getAttribute("javax.websocket.server.ServerContainer") as WebSocketContainer
        serverContainer.defaultMaxBinaryMessageBufferSize = 100000000
        serverContainer.defaultMaxTextMessageBufferSize = 1000000
        serverContainer.defaultMaxSessionIdleTimeout = 10000
    }

}