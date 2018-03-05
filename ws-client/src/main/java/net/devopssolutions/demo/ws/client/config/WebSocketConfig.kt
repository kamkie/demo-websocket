package net.devopssolutions.demo.ws.client.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient


@Configuration
@EnableScheduling
open class WebSocketConfig {

    @Bean
    open fun objectMapper() = ObjectMapper().findAndRegisterModules()

    @Bean
    open fun webSocketClient() = ReactorNettyWebSocketClient()

}
