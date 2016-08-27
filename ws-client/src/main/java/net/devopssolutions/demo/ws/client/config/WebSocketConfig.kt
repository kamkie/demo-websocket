package net.devopssolutions.demo.ws.server.config

import org.springframework.context.annotation.Configuration

@Configuration
open class WebSocketConfig {

//    @Bean
//    open fun createWebSocketContainer(): WebSocketClient  {
//        val container = ServletServerContainerFactoryBean()
//        container.setMaxTextMessageBufferSize(8192)
//        container.setMaxBinaryMessageBufferSize(8192)
//        return container
//    }

//    @Bean
//    open fun chatEndpointRegistration(wsServer: WsServer) = ServerEndpointRegistration("/ws", wsServer)
//
//    @Bean
//    open fun endpointExporter() = ServerEndpointExporter()
//
//    @Autowired
//    private fun init(servletContext: ServletContext) {
//        val serverContainer = servletContext.getAttribute("javax.websocket.server.ServerContainer") as WebSocketContainer
//        serverContainer.defaultMaxBinaryMessageBufferSize = 100000000
//        serverContainer.defaultMaxTextMessageBufferSize = 1000000
//        serverContainer.defaultMaxSessionIdleTimeout = 10000
//    }

}
