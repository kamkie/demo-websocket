package net.devopssolutions.demo.ws.server.config;

import net.devopssolutions.demo.ws.server.component.WsServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;
import org.springframework.web.socket.server.standard.ServerEndpointRegistration;

import javax.servlet.ServletContext;
import javax.websocket.WebSocketContainer;

@Configuration
public class WebSocketConfig {

    @Bean
    public ServerEndpointRegistration chatEndpointRegistration(WsServer wsServer) {
        return new ServerEndpointRegistration("/ws", wsServer);
    }

    @Bean
    public ServerEndpointExporter endpointExporter() {
        return new ServerEndpointExporter();
    }

    @Autowired
    private void init(ServletContext servletContext) {
        WebSocketContainer serverContainer = (WebSocketContainer) servletContext.getAttribute("javax.websocket.server.ServerContainer");
        serverContainer.setDefaultMaxBinaryMessageBufferSize(100_000_000);
        serverContainer.setDefaultMaxTextMessageBufferSize(1_000_000);
        serverContainer.setDefaultMaxSessionIdleTimeout(10_000);
    }

}