package net.devopssolutions.demo.ws.client.component;

import lombok.extern.slf4j.Slf4j;
import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.container.jdk.client.JdkClientContainer;
import org.glassfish.tyrus.ext.client.java8.SessionBuilder;
import org.springframework.stereotype.Component;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import java.net.URI;

@Slf4j
@Component
public class WsConsumer {

    private Session session;

    public WsConsumer() throws Exception {
        log.info("going to open ws connection");
        ClientEndpointConfig clientEndpointConfig = ClientEndpointConfig.Builder.create()
                .configurator(new ClientEndpointConfig.Configurator())
                .build();
        ClientManager clientManager = ClientManager.createClient(JdkClientContainer.class.getName());
        this.session = new SessionBuilder(clientManager).uri(new URI("ws://localhost:8080/ws"))
                .onOpen(this::onOpen)
                .messageHandler(String.class, this::onMessage)
                .onError(this::onError)
                .onClose(this::onClose)
                .clientEndpointConfig(clientEndpointConfig)
                .connect();
    }

    private void onMessage(String message) {
        log.info("new message: {}", message);
    }

    private void onOpen(Session session, EndpointConfig endpointConfig) {
        log.info("opened ws connection");
    }

    private void onClose(Session session, CloseReason closeReason) {
        log.info("closing ws connection");
    }

    private void onError(Session session, Throwable throwable) {
        log.warn("error in ws session: "+ session, throwable);
    }
}
